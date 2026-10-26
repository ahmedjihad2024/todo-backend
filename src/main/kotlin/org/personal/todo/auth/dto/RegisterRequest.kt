package org.personal.todo.auth.dto

import org.personal.todo.common.phone.E164PhoneDeserializer
import org.personal.todo.common.phone.ValidPhone
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ProviderRegisterRequest(
    @field:NotBlank(message = "{validation.cannot_be_blank}")
    @field:Size(min = 2, max = 25, message = "{validation.name.size}")
    val firstName: String,
    @field:NotBlank(message = "{validation.cannot_be_blank}")
    @field:Size(min = 2, max = 25, message = "{validation.name.size}")
    val lastName: String,

    @field:NotBlank(message = "{validation.cannot_be_blank}")
    @field:ValidPhone
    @field:JsonDeserialize(using = E164PhoneDeserializer::class)
    val phone: String,

    // Optional contact email. When present it must look like an email; it is not verified.
    @field:Email(message = "{validation.invalid_email}")
    val email: String? = null,
)


data class UserRegisterRequest(
    @field:NotBlank(message = "{validation.cannot_be_blank}")
    @field:Size(min = 2, max = 25, message = "{validation.name.size}")
    val firstName: String,
    @field:NotBlank(message = "{validation.cannot_be_blank}")
    @field:Size(min = 2, max = 25, message = "{validation.name.size}")
    val lastName: String,

    @field:NotBlank(message = "{validation.cannot_be_blank}")
    @field:ValidPhone
    @field:JsonDeserialize(using = E164PhoneDeserializer::class)
    val phone: String,

    // Optional contact email. When present it must look like an email; it is not verified.
    @field:Email(message = "{validation.invalid_email}")
    val email: String? = null,
)
