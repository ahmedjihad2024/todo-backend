package org.personal.todo.account.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = AccountResponse.UserResponse::class, name = "USER"),
    JsonSubTypes.Type(value = AccountResponse.AdminResponse::class, name = "ADMIN"),
)
sealed class AccountResponse {

    data class UserResponse(
        val id: String,
        val firstName: String?,
        val lastName: String?,
        val phone: String,
        val email: String?,
        val registered: Boolean,
        val avatar: String? = null,
    ) : AccountResponse()

    data class AdminResponse(
        val id: String,
        val firstName: String?,
        val lastName: String?,
        val phone: String,
        val email: String?,
        val registered: Boolean,
        val avatar: String? = null,
    ) : AccountResponse()
}
