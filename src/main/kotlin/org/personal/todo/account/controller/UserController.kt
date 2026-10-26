package org.personal.todo.account.controller

import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import jakarta.validation.Valid
import org.personal.todo.account.AccountService
import org.personal.todo.account.dto.AccountResponse
import org.personal.todo.account.dto.UpdateRequest
import org.personal.todo.account.enums.Role
import org.personal.todo.common.dto.ApiResponse
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

/**
 * Buyer (USER persona) endpoints under /api/user/profile. Deleting here removes only the
 * USER persona via the per-role deletion flow (see [org.personal.todo.account.AccountService.deleteMe]); the account
 * and any other persona (e.g. provider) stay intact. Gated to accounts holding the USER role.
 */
@Tags(Tag(name = "User"), Tag(name = "Profile"))
@RestController
@RequestMapping("/api/user/profile")
@PreAuthorize("hasRole('USER')")
class UserController(
    private val accountService: AccountService,
) {
    @GetMapping("/me")
    fun me(
        @AuthenticationPrincipal currentUserId: String,
    ): ApiResponse<AccountResponse> =
        accountService.me(currentUserId, role = Role.USER)

    @DeleteMapping("/me")
    fun deleteMe(
        @AuthenticationPrincipal currentUserId: String,
    ): ApiResponse<Unit> =
        accountService.deleteMe(currentUserId, role = Role.USER)

    @PatchMapping("/me")
    fun updateProfile(
        @Valid @RequestBody body: UpdateRequest,
        @AuthenticationPrincipal currentUserId: String,
    ): ApiResponse<AccountResponse> =
        accountService.updateProfile(
            currentUserId, body.firstName, body.lastName, body.email, role = Role.USER,
        )

    @PostMapping("/me/avatar", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadAvatar(
        @RequestParam("file") file: MultipartFile,
        @AuthenticationPrincipal currentUserId: String,
    ): ApiResponse<AccountResponse> =
        accountService.uploadAvatar(currentUserId, Role.USER, file)
}