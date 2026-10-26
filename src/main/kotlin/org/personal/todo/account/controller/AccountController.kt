package org.personal.todo.account.controller

import org.personal.todo.auth.dto.AuthResponse
import org.personal.todo.common.dto.ApiResponse
import org.personal.todo.account.dto.ChangePhoneRequest
import org.personal.todo.account.dto.ConfirmPhoneChangeRequest
import org.personal.todo.account.dto.AccountResponse
import jakarta.validation.Valid
import org.personal.todo.account.enums.Role
import org.personal.todo.common.exception.ApiException
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import org.personal.todo.account.AccountService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@Tags(Tag(name = "Common"), Tag(name = "Profile"))
@RestController
@RequestMapping("/api/profile")
class AccountController(
    private val accountService: AccountService,
) {
    @PostMapping("/me/change-phone/request")
    fun changePhone(
        @Valid @RequestBody body: ChangePhoneRequest,
        @AuthenticationPrincipal currentUserId: String,
    ): ApiResponse<AuthResponse.SetUpRequired> =
        accountService.changePhone(currentUserId, body.newPhone)

    @PostMapping("/me/change-phone/verify")
    fun verifyChangePhoneCode(
        @Valid @RequestBody body: ConfirmPhoneChangeRequest,
        @AuthenticationPrincipal currentUserId: String,
        authentication: Authentication,
    ): ApiResponse<AccountResponse> {
        val role = authentication.authorities.firstNotNullOfOrNull { Role.fromAuthority(it.authority) }
            ?: throw ApiException.Unauthorized("error.auth.invalid_token")
        return accountService.verifyChangePhoneCode(currentUserId, body.code, role)
    }
}
