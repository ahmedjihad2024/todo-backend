package org.personal.todo.auth.dto

import org.personal.todo.common.phone.E164PhoneDeserializer
import org.personal.todo.common.phone.ValidPhone
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:Schema(example = "+201095651645")
    @field:NotBlank(message = "{validation.cannot_be_blank}")
    @field:ValidPhone
    @field:JsonDeserialize(using = E164PhoneDeserializer::class)
    val phone: String,
)
