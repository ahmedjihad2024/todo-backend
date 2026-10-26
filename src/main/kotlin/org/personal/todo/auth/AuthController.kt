package org.personal.todo.auth

import org.personal.todo.auth.dto.AuthResponse
import org.personal.todo.auth.dto.LoginRequest
import org.personal.todo.auth.dto.RefreshRequest
import org.personal.todo.auth.dto.ProviderRegisterRequest
import org.personal.todo.auth.dto.ResendVerificationRequest
import org.personal.todo.auth.dto.TokenResponse
import org.personal.todo.auth.dto.VerifyPhoneRequest
import org.personal.todo.common.dto.ApiResponse
import jakarta.validation.Valid
import org.personal.todo.account.dto.AccountResponse
import org.springframework.http.HttpStatus
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import org.springframework.web.bind.annotation.RestController

@Tags(Tag(name = "Common"), Tag(name = "Auth"))
@RestController
@RequestMapping("api/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/verify-phone")
    fun verifyPhone(@Valid @RequestBody body: VerifyPhoneRequest): ApiResponse<AuthResponse.Authenticated> =
        ApiResponse.ok(authService.verifyPhone(body.phone, body.code))

    @PostMapping("/verify-phone/resend")
    fun resendVerification(@Valid @RequestBody body: ResendVerificationRequest): ApiResponse<AuthResponse.SetUpRequired> =
        ApiResponse.ok(authService.resendVerificationCode(body.phone))

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody body: RefreshRequest): ApiResponse<TokenResponse> =
        ApiResponse.ok(authService.refresh(body.refreshToken))

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(
        @Valid @RequestBody body: RefreshRequest,
        @RequestHeader(HttpHeaders.AUTHORIZATION, required = false) authHeader: String?,
    ) {
        val accessToken = authHeader?.takeIf { it.startsWith("Bearer ") }?.removePrefix("Bearer ")
        authService.logout(body.refreshToken, accessToken)
    }
}
