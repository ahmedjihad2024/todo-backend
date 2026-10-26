package org.personal.todo.auth.dto

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
)
