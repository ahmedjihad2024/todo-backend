package org.personal.todo.account.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

data class UpdateRequest(
    @field:Size(min = 2, max = 50, message = "{validation.name.size}")
    val firstName: String? = null,
    val lastName: String? = null,

    // Optional, unverified contact email. When provided it updates the current one;
    // omit/null to leave it unchanged.
    @field:Email(message = "{validation.invalid_email}")
    val email: String? = null,
)
