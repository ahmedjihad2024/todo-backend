package org.personal.todo.account

import org.personal.todo.auth.repository.PhoneVerificationCodeRepository
import org.personal.todo.auth.repository.RefreshTokenRepository
import org.personal.todo.account.entities.Account
import org.personal.todo.account.enums.Role
import org.personal.todo.account.lifecycle.PersonaRolePurged
import org.personal.todo.account.repository.PhoneChangeRequestRepository
import org.personal.todo.account.repository.AccountRepository

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

/**
 * Permanently removes soft-deleted personas whose grace period has elapsed.
 *
 * Deletion is per-persona: each persona's `deleted` / `deletedAt` lives on its profile. The purge
 * event lets each persona-owned resource capture external cleanup references before profile rows
 * are deleted. External resources are removed after the database transaction commits.
 */
@Component
class AccountPurgeJob(
    private val accountRepository: AccountRepository,
    private val events: ApplicationEventPublisher,
    private val profileService: ProfileService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /** Runs daily at 03:00 server time. */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    fun purgeExpired() {
        val cutoff = Instant.now().minus(Account.DELETED_RETENTION_DAYS, ChronoUnit.DAYS)
        val expired = profileService.findExpiredDeleted(cutoff)
        if (expired.isEmpty()) return
        log.info(
            "Purging {} persona(s) past the {}-day grace period",
            expired.size,
            Account.DELETED_RETENTION_DAYS,
        )
        expired.forEach { persona ->
            runCatching { purgePersona(persona.accountId, persona.role) }.onFailure { e ->
                log.error("Failed to purge persona {} of account {}", persona.role, persona.accountId, e)
            }
        }
    }

    @Transactional
    fun purgePersona(accountId: UUID, role: Role) {
        events.publishEvent(PersonaRolePurged(accountId, role))
        profileService.delete(accountId, role)

        if (!profileService.hasAnyProfile(accountId)) {
            accountRepository.deleteById(accountId)
        }
    }
}
