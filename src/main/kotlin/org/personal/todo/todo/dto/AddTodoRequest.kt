package org.personal.todo.todo.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.personal.todo.todo.Priority
import java.time.Instant

data class AddTodoRequest(
    @field:NotBlank
    @field:Size(min = 1, max = 400, message = "{validation.todo_title.size}")
    @field:Schema(example = "Buy milk", description = "Todo title")
    val title: String,

    @field:Size(max = 2000)
    @field:Schema(example = "2 liters", description = "Optional details")
    val description: String? = null,

    @field:Schema(example = "false", description = "true or false")
    val completed: Boolean?,

    @field:Schema(example = "2026-10-20T14:30:00Z", description = "ISO-8601 UTC")
    val dueDate: Instant? = null,

    @field:Schema(example = "MEDIUM", description = "LOW, MEDIUM or HIGH")
    val priority: Priority,
)