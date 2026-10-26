package org.personal.todo.todo.dto

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.Length
import org.personal.todo.todo.Priority
import java.time.Instant

data class SearchTodoRequest(
    @field:Size(min = 1, max = 400, message = "{validation.todo_title.size}")
    val q: String? = null,

    @field:Parameter(example = "2026-10-20T14:30:00Z", description = "ISO-8601 UTC")
    val dueDate: Instant? = null,

    @field:Parameter(example = "HIGH", description = "LOW, MEDIUM or HIGH")
    var priority: Priority? = null,

    @field:Parameter(example = "false", description = "true or false")
    var completed: Boolean? = null,
)
