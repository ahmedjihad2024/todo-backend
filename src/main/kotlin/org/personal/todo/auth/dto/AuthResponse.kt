package org.personal.todo.auth.dto

import org.personal.todo.account.dto.AccountResponse
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = AuthResponse.Authenticated::class, name = "AUTHENTICATED"),
    JsonSubTypes.Type(value = AuthResponse.SetUpRequired::class, name = "SET_UP_REQUIRED"),
)
sealed class AuthResponse {

    data class Authenticated(
        val user: AccountResponse,
        val accessToken: String,
        val refreshToken: String,
    ) : AuthResponse()

    data class SetUpRequired(
        val phone: String,
        val message: String,
        val registered: Boolean = false,
    ) : AuthResponse()
}
