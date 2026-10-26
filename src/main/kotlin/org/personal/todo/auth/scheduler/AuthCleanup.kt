package org.personal.todo.auth.scheduler

import org.personal.todo.auth.repository.PhoneVerificationCodeRepository
import org.personal.todo.auth.repository.RefreshTokenRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Component
class AuthCleanup(
    private val verificationCodeRepo: PhoneVerificationCodeRepository,
    private val refreshTokenRepo: RefreshTokenRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // كل 10 دقايق: أكواد التحقق (عمرها قصير)
    @Scheduled(fixedRate = 600_000)
    @Transactional
    fun cleanVerificationCodes() {
        val deleted = verificationCodeRepo.deleteExpired(Instant.now())
        if (deleted > 0) log.info("Deleted {} expired verification codes", deleted)
    }

    // كل يوم 3 الفجر: الـ refresh tokens (عمرها طويل)
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    fun cleanRefreshTokens() {
        val deleted = refreshTokenRepo.deleteExpired(Instant.now())
        if (deleted > 0) log.info("Deleted {} expired refresh tokens", deleted)
    }
}