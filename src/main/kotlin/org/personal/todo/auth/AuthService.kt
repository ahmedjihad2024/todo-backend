package org.personal.todo.auth

import org.personal.todo.auth.dto.AuthResponse
import org.personal.todo.auth.dto.TokenResponse
import org.personal.todo.auth.entities.PhoneVerificationCode
import org.personal.todo.auth.entities.RefreshToken
import org.personal.todo.auth.sms.SmsSender
import org.personal.todo.auth.sms.VERIFICATION_CODE_TTL_MINUTES
import org.personal.todo.auth.sms.VerificationCodeGenerator
import org.personal.todo.auth.repository.PhoneVerificationCodeRepository
import org.personal.todo.auth.repository.RefreshTokenRepository
import org.personal.todo.common.exception.ApiException
import org.personal.todo.common.extentions.sha256
import org.personal.todo.common.extentions.tr
import org.personal.todo.common.extentions.toUuid
import org.personal.todo.security.jwt.JwtService
import org.personal.todo.security.jwt.TokenBlacklist
import org.personal.todo.account.AccountService
import org.personal.todo.account.entities.Account
import org.personal.todo.account.enums.Role
import org.personal.todo.account.repository.AccountRepository
import org.personal.todo.account.mapper.toResponse
import org.personal.todo.account.ProfileService

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class AuthService(
    private val accountRepository: AccountRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val tokenBlacklist: TokenBlacklist,
    private val phoneVerificationCodeRepository: PhoneVerificationCodeRepository,
    private val jwtService: JwtService,
    private val smsSender: SmsSender,
    private val verificationCodeGenerator: VerificationCodeGenerator,
    private val accountService: AccountService,
    private val profileService: ProfileService,
) {

    private val log = LoggerFactory.getLogger(javaClass)
    @Transactional
    fun register(
        firstName: String,
        lastName: String,
        phone: String,
        email: String?,
        role: Role,
    ): AuthResponse.SetUpRequired {
        val existing = accountRepository.findByPhone(phone)
        val user = if (existing == null) {
            // New account — identity only (name/email live on the persona profile).
            accountRepository.save(Account(phone = phone, roles = mutableSetOf(role)))
        } else {
            // Existing account — add the role only if it's new.
            if (existing.roles.add(role)) {
                accountRepository.save(existing)
            }
            existing
        }
        val userId = user.requireId()

        // Create/populate this persona's profile with its name + email (registered = false until verify).
        profileService.upsertProfile(userId, role, firstName, lastName, email)
        // If re-registering a persona that's still in its deletion grace, restore it (un-delete +
        // re-add role + reactivate its resources). No-op for a brand-new persona.
        accountService.restoreRole(userId, role)

        issueAndSendVerificationCode(user, role)
        return AuthResponse.SetUpRequired(
            phone = user.phone,
            message = "error.auth.verification_code_sent".tr(),
            registered = profileService.isRegistered(userId, role),   // per-persona, not account-level
        )
    }

    @Transactional
    fun login(phone: String, role: Role): AuthResponse.SetUpRequired {
        var user = accountRepository.findByPhone(phone)
            ?: return AuthResponse.SetUpRequired(
                phone = phone,
                message = "auth.need_register".tr(),
                registered = false,
            )

        // If this persona is within its deletion grace, restore it (re-adds the role, clears the
        // profile's deleted flag). No-op if the persona isn't deleted. Must run BEFORE the role check.
        val userId = user.requireId()
        accountService.restoreRole(userId, role)
        user = accountRepository.findById(userId).orElse(user)

        // Registration is per-persona: an account registered as PROVIDER is NOT registered as USER.
        // If this persona isn't set up, route the client to register instead of issuing a code that
        // would dead-end at verify with "account does not have that role".
        if (role !in user.roles || !profileService.isRegistered(userId, role)) {
            return AuthResponse.SetUpRequired(
                phone = phone,
                message = "auth.need_register".tr(),
                registered = false,
            )
        }

        issueAndSendVerificationCode(user, role)
        return AuthResponse.SetUpRequired(
            phone = user.phone,
            message = "error.auth.verification_code_sent".tr(),
            registered = true
        )
    }

    /**
     * Admin persona registration. The eligibility gate lives here, not in the controller: the
     * account must already carry [Role.ADMIN] (added by [AdminBootstrapper] at startup once the
     * account exists).
     *
     * firstName/lastName/email are persisted to the ADMIN persona profile.
     */
    @Transactional
    fun registerAdmin(firstName: String, lastName: String, phone: String, email: String?): AuthResponse.SetUpRequired {
        val adminRole = requireEligibleAdmin(phone)
        log.info("Admin auth: register accepted for {}", phone)
        return register(firstName, lastName, phone, email, adminRole)
    }

    /**
     * Admin persona login — same eligibility gate as [registerAdmin]. On success the shared OTP
     * flow continues exactly as for any other persona.
     */
    @Transactional
    fun loginAdmin(phone: String): AuthResponse.SetUpRequired {
        val adminRole = requireEligibleAdmin(phone)
        log.info("Admin auth: login accepted for {}", phone)
        return login(phone, adminRole)
    }

    /**
     * Admin endpoints must stay indistinguishable from a non-existent account for anyone not
     * entitled to this persona: an account without [Role.ADMIN] is a plain 404 that reveals
     * nothing about the endpoint.
     */
    private fun requireEligibleAdmin(phone: String): Role {
        val adminRole = Role.entries.firstOrNull { it.name == "ADMIN" }
            ?: throw ApiException.NotFound("error.account.not_found")
        val promoted = accountRepository.findByPhone(phone)?.roles?.contains(adminRole) == true
        if (!promoted) {
            log.warn("Admin auth: rejected attempt for {}", phone)
            throw ApiException.NotFound("error.account.not_found")
        }
        return adminRole
    }

    @Transactional
    fun verifyPhone(phone: String, code: String): AuthResponse.Authenticated {
        val user = accountRepository.findByPhone(phone)
            ?: throw ApiException.NotFound("error.account.not_found")
        val userId = user.requireId()
        val stored = phoneVerificationCodeRepository.findByAccountId(userId)
            ?: throw ApiException.BadRequest("error.auth.verification_code_invalid")
        if (stored.expiresAt.isBefore(Instant.now())) {
            phoneVerificationCodeRepository.delete(stored)
            throw ApiException.BadRequest("error.auth.verification_code_expired")
        }
        if (stored.code != code.sha256()) {
            throw ApiException.BadRequest("error.auth.verification_code_invalid")
        }
        phoneVerificationCodeRepository.delete(stored)
        // Never mint a token for a role the account doesn't actually hold.
        if (stored.role !in user.roles) {
            throw ApiException.BadRequest("error.account.role_not_present")
        }
        // This persona has now completed setup.
        profileService.markRegistered(userId, stored.role)
        val tokens = issueTokens(user, stored.role)
        return AuthResponse.Authenticated(
            user.toResponse(stored.role, profileService.getOrEmpty(userId, stored.role)),
            tokens.accessToken,
            tokens.refreshToken,
        )
    }

    @Transactional
    fun resendVerificationCode(phone: String): AuthResponse.SetUpRequired {
        val user = accountRepository.findByPhone(phone)
        // Privacy-preserving: only (re)send when an unverified-login attempt actually exists, but
        // always return the same generic message so we never leak which phone numbers are pending.
        // Reuse the persona from the pending code so the resend keeps the same login role.
        val pending = user?.let { phoneVerificationCodeRepository.findByAccountId(it.requireId()) }
        if (user != null && pending != null) {
            issueAndSendVerificationCode(user, pending.role)
        }
        return AuthResponse.SetUpRequired(
            phone = phone,
            message = "error.auth.verification_code_sent".tr()
        )
    }

    @Transactional
    fun refresh(refreshToken: String): TokenResponse {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw ApiException.Unauthorized("error.auth.invalid_refresh_token")
        }
        val userId = jwtService.getUserIdFromToken(refreshToken).toUuid()
        val hashed = refreshToken.sha256()

        val stored = refreshTokenRepository.findByAccountIdAndHashedToken(userId, hashed)
            ?: throw ApiException.Unauthorized("error.auth.refresh_token_revoked")

        refreshTokenRepository.delete(stored)

        val user = accountRepository.findById(userId).orElseThrow {
            ApiException.Unauthorized("error.auth.refresh_token_revoked")
        }
        // The session is scoped to one persona; if that role is gone (deleted/revoked), so is the session.
        if (stored.role !in user.roles) {
            throw ApiException.Unauthorized("error.auth.refresh_token_revoked")
        }
        return issueTokens(user, stored.role)
    }

    fun logout(refreshToken: String, accessToken: String?) {
        if (jwtService.validateRefreshToken(refreshToken)) {
            val userId = jwtService.getUserIdFromToken(refreshToken).toUuid()
            refreshTokenRepository.deleteByAccountIdAndHashedToken(userId, refreshToken.sha256())
        }
        if (accessToken != null && jwtService.validateAccessToken(accessToken)) {
            tokenBlacklist.revoke(
                jti = jwtService.getJti(accessToken),
                expiresAt = jwtService.getExpiry(accessToken),
            )
        }
    }

    private fun issueAndSendVerificationCode(user: Account, role: Role) {
        phoneVerificationCodeRepository.deleteByAccountId(user.requireId())
        val plain = verificationCodeGenerator.generate()
        phoneVerificationCodeRepository.save(
            PhoneVerificationCode(
                account = user,
                code = plain.sha256(),
                role = role,
                expiresAt = Instant.now().plus(VERIFICATION_CODE_TTL_MINUTES, ChronoUnit.MINUTES),
            )
        )
        smsSender.sendVerificationCode(user.phone, plain)
    }

    /** Mints a token scoped to a single [role] (not all of the account's roles) and remembers that
     *  scope on the refresh token so a later refresh stays scoped to the same persona. */
    private fun issueTokens(user: Account, role: Role): TokenResponse {
        val userId = user.requireId().toString()
        val accessToken = jwtService.generateAccessToken(userId, setOf(role))
        val refreshToken = jwtService.generateRefreshToken(userId)

        refreshTokenRepository.save(
            RefreshToken(
                account = user,
                hashedToken = refreshToken.sha256(),
                role = role,
                expiresAt = Instant.now().plus(jwtService.refreshTokenValidityMs, ChronoUnit.MILLIS),
            )
        )
        return TokenResponse(accessToken, refreshToken)
    }

    private fun Account.requireId(): UUID =
        id ?: throw IllegalStateException("Account must be persisted before its ID is used")

}
