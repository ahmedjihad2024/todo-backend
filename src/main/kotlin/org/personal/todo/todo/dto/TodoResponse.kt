package org.personal.todo.todo.dto

import org.personal.todo.todo.Priority
import java.io.Serializable
import java.time.Instant
import java.util.UUID

data class TodoResponse(
    val id: UUID,
    var priority: Priority,
    val title: String,
    var description: String? = null,
    var dueDate: Instant? = null,
    var completed: Boolean,
    val createdAt: Instant,
): Serializable
