package org.personal.todo.auth.dto

import org.personal.todo.common.phone.E164PhoneDeserializer
import org.personal.todo.common.phone.ValidPhone
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class VerifyPhoneRequest(
    @field:Schema(example = "11111")
    @field:NotBlank(message = "{validation.cannot_be_blank}")
    @field:ValidPhone
    @field:JsonDeserialize(using = E164PhoneDeserializer::class)
    val phone: String,

    @field:Schema(example = "+201095651645")
    @field:NotBlank(message = "{validation.cannot_be_blank}")
    @field:Pattern(regexp = "^\\d{5}\$", message = "{validation.verification_code.pattern}")
    val code: String,
)
