package org.personal.todo.auth.controller

import jakarta.validation.Valid
import org.personal.todo.auth.dto.AuthResponse
import org.personal.todo.auth.dto.LoginRequest
import org.personal.todo.common.dto.ApiResponse
import org.personal.todo.account.enums.Role
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import org.personal.todo.auth.AuthService
import org.personal.todo.auth.dto.UserRegisterRequest
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Buyer (USER persona) registration and login under /api/user/auth, mirroring
 * [ProviderAuthController]. Phone verification, token refresh, and logout are persona-agnostic and
 * stay shared in [org.personal.todo.auth.AuthController] under /api/auth.
 */
@Tags(Tag(name = "User"), Tag(name = "Auth"))
@RestController
@RequestMapping("api/user/auth")
class UserAuthController(
    private val authService: AuthService,
) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody body: UserRegisterRequest): ApiResponse<AuthResponse.SetUpRequired> =
        ApiResponse.ok(
            authService.register(
                body.firstName, body.lastName, body.phone, body.email, Role.USER,
            )
        )

    @PostMapping("/login")
    fun login(@Valid @RequestBody body: LoginRequest): ApiResponse<AuthResponse.SetUpRequired> =
        ApiResponse.ok(authService.login(body.phone, Role.USER))
}
    