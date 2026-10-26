package org.personal.todo.admin

import org.personal.todo.admin.dto.UpdateUserRolesRequest
import org.personal.todo.common.dto.ApiResponse
import org.personal.todo.account.dto.AccountResponse
import org.personal.todo.account.mapper.toResponse
import jakarta.validation.Valid

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tags(Tag(name = "Admin"), Tag(name = "Admin Users"))
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
class AdminController(
    private val adminService: AdminService,
) {

//    @GetMapping("/users")
//    fun listUsers(
//        @PageableDefault(size = 20, sort = ["name"], direction = Sort.Direction.ASC)
//        pageable: Pageable,
//    ): ApiResponse<List<AccountResponse>> =
//        ApiResponse.paged(adminService.listUsers(pageable))
//
//    @PatchMapping("/users/{id}/roles")
//    fun updateRoles(
//        @PathVariable id: ObjectId,
//        @Valid @RequestBody body: UpdateUserRolesRequest,
//        @AuthenticationPrincipal currentUserId: String,
//    ): ApiResponse<AccountResponse> =
//        ApiResponse.ok(adminService.updateRoles(id, currentUserId, body.roles).toResponse())
}
