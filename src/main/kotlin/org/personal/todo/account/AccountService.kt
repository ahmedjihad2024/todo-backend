package org.personal.todo.account

import org.personal.todo.auth.dto.AuthResponse
import org.personal.todo.auth.repository.RefreshTokenRepository
import org.personal.todo.auth.sms.SmsSender
import org.personal.todo.auth.sms.VERIFICATION_CODE_TTL_MINUTES
import org.personal.todo.auth.sms.VerificationCodeGenerator
import org.personal.todo.common.dto.ApiResponse
import org.personal.todo.common.extentions.sha256
import org.personal.todo.common.exception.ApiException
import org.personal.todo.common.extentions.tr
import org.personal.todo.common.storage.FileCategory
import org.personal.todo.common.storage.FileStorageService
import org.personal.todo.account.dto.AccountResponse
import org.personal.todo.account.entities.Account
import org.personal.todo.account.entities.PhoneChangeRequest
import org.personal.todo.account.enums.Role
import org.personal.todo.account.lifecycle.PersonaRoleDeactivated
import org.personal.todo.account.lifecycle.PersonaRoleRestored
import org.personal.todo.account.mapper.toResponse
import org.personal.todo.account.repository.PhoneChangeRequestRepository
import org.personal.todo.account.repository.AccountRepository
import org.personal.todo.common.extentions.toUuid

import org.springframework.stereotype.Service
import org.springframework.context.ApplicationEventPublisher
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val phoneChangeRequestRepository: PhoneChangeRequestRepository,
    private val smsSender: SmsSender,
    private val fileStorageService: FileStorageService,
    private val verificationCodeGenerator: VerificationCodeGenerator,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val profileService: ProfileService,
    private val events: ApplicationEventPublisher,
) {
    fun me(id: String, role: Role): ApiResponse<AccountResponse> {
        val user = accountRepository.findById(id.toUuid())
            .orElseThrow { ApiException.NotFound("error.account.not_found") }
        return ApiResponse.ok(user.toResponse(role, profileService.getOrEmpty(user.requireId(), role)))
    }

    @Transactional
    fun deleteMe(id: String, role: Role): ApiResponse<Unit> {
        val account = getAccount(id)
        validateRolePresent(account, role)
        softDeletePersona(account, role)
        return ApiResponse.ok(Unit)
    }

    private fun getAccount(id: String): Account =
        accountRepository.findById(id.toUuid())
            .orElseThrow { ApiException.NotFound("error.account.not_found") }

    private fun validateRolePresent(account: Account, role: Role) {
        if (role !in account.roles) {
            throw ApiException.BadRequest("error.account.role_not_present")
        }
    }

    private fun softDeletePersona(account: Account, role: Role) {
        val accountId = account.requireId()
        profileService.markDeleted(accountId, role, Instant.now())
        account.roles.remove(role)
        accountRepository.save(account)
        refreshTokenRepository.deleteAllByAccountId(accountId)
        events.publishEvent(PersonaRoleDeactivated(accountId, role))
    }

    /**
     * Restores a persona that was soft-deleted but is still within its grace period: clears the
     * profile's deleted flag and re-adds the [role]. A no-op if the persona isn't deleted. Call this
     * when an account re-authenticates (or re-registers) as that role.
     */
    @Transactional
    fun restoreRole(accountId: UUID, role: Role) {
        val account = accountRepository.findById(accountId).orElse(null) ?: return
        if (!profileService.isDeleted(accountId, role)) return

        profileService.clearDeleted(accountId, role)
        account.roles.add(role)
        accountRepository.save(account)
        events.publishEvent(PersonaRoleRestored(accountId, role))
    }

    @Transactional
    fun uploadAvatar(id: String, role: Role, file: MultipartFile): ApiResponse<AccountResponse> {
        val user = accountRepository.findById(id.toUuid())
            .orElseThrow { ApiException.NotFound("error.account.not_found") }
        val newFilename = fileStorageService.store(FileCategory.AVATAR, file)
        // Avatar is per-persona — write it to this role's profile and get the previous file back.
        val userId = user.requireId()
        val previousFilename = profileService.updateAvatar(userId, role, newFilename)
        // Drop the old file only after the pointer is safely persisted, to avoid orphaning the live avatar.
        previousFilename?.let { fileStorageService.delete(FileCategory.AVATAR, it) }
        return ApiResponse.ok(user.toResponse(role, profileService.getOrEmpty(userId, role)))
    }

    /**
     * Updates the caller's PERSONA profile (each field applied only when non-null).
     * Name, email and avatar are per-persona, so this writes to the [role]'s profile, not the account.
     */
    @Transactional
    fun updateProfile(
        id: String,
        firstName: String?,
        lastName: String?,
        email: String?,
        role: Role,
    ): ApiResponse<AccountResponse> {
        val user = accountRepository.findById(id.toUuid())
            .orElseThrow { ApiException.NotFound("error.account.not_found") }
        val profile = profileService.upsertProfile(user.requireId(), role, firstName, lastName, email)
        return ApiResponse.ok(user.toResponse(role, profile))
    }

    /** Phone is the verified identifier, so changing it requires SMS confirmation of the new number. */
    @Transactional
    fun changePhone(id: String, newPhone: String): ApiResponse<AuthResponse.SetUpRequired> {
        val user = accountRepository.findById(id.toUuid())
            .orElseThrow { ApiException.NotFound("error.account.not_found") }
        if (newPhone == user.phone) {
            throw ApiException.BadRequest("error.account.phone_same_as_current")
        }
        val taken = accountRepository.findByPhone(newPhone)
        if (taken != null && taken.id != user.id) {
            throw ApiException.Conflict("error.auth.phone_already_exists")
        }
        val userId = user.requireId()
        phoneChangeRequestRepository.deleteByAccountId(userId)
        val plain = verificationCodeGenerator.generate()
        phoneChangeRequestRepository.save(
            PhoneChangeRequest(
                account = user,
                newPhone = newPhone,
                code = plain.sha256(),
                expiresAt = Instant.now().plus(VERIFICATION_CODE_TTL_MINUTES, ChronoUnit.MINUTES),
            ),
        )
        smsSender.sendVerificationCode(newPhone, plain)
        return ApiResponse.ok(
            AuthResponse.SetUpRequired(
                phone = newPhone,
                message = "error.account.phone_change_code_sent".tr(),
            ),
        )
    }

    @Transactional
    fun verifyChangePhoneCode(id: String, code: String, role: Role): ApiResponse<AccountResponse> {
        val user = accountRepository.findById(id.toUuid())
            .orElseThrow { ApiException.NotFound("error.account.not_found") }

        val userId = user.requireId()
        val pending = phoneChangeRequestRepository.findByAccountId(userId)
            ?: throw ApiException.BadRequest("error.account.no_pending_phone_change")

        if (pending.expiresAt.isBefore(Instant.now())) {
            phoneChangeRequestRepository.delete(pending)
            throw ApiException.BadRequest("error.account.phone_change_code_expired")
        }

        if (pending.code != code.sha256()) {
            throw ApiException.BadRequest("error.account.phone_change_code_invalid")
        }

        val taken = accountRepository.findByPhone(pending.newPhone)
        if (taken != null && taken.id != user.id) {
            phoneChangeRequestRepository.delete(pending)
            throw ApiException.Conflict("error.auth.phone_already_exists")
        }

        user.phone = pending.newPhone
        phoneChangeRequestRepository.delete(pending)

        return ApiResponse.ok(user.toResponse(role, profileService.getOrEmpty(user.requireId(), role)))
    }

    private fun Account.requireId() =
        id ?: throw IllegalStateException("Account must be persisted before its ID is used")
}
