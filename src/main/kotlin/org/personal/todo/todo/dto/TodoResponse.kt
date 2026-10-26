package org.personal.todo.todo.dto

import jakarta.persistence.Column
import org.hibernate.annotations.CreationTimestamp
import org.personal.todo.todo.Priority
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
)
