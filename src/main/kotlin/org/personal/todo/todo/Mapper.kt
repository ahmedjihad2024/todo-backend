package org.personal.todo.todo

import org.personal.todo.todo.dto.TodoResponse


fun Todo.toResponse() = TodoResponse(
    id = id!!,
    priority = priority,
    title = title,
    description = description,
    dueDate = dueDate,
    completed = completed,
    createdAt = createdAt!!
)