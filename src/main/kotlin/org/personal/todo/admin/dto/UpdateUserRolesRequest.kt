package org.personal.todo.admin.dto

import jakarta.validation.constraints.NotEmpty

data class UpdateUserRolesRequest(
    @field:NotEmpty(message = "{error.admin.roles_required}")
    val roles: Set<String>,
)
