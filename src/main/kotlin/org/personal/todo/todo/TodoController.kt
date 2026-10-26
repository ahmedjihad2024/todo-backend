package org.personal.todo.todo

import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import jakarta.validation.Valid
import org.personal.todo.common.dto.ApiResponse
import org.personal.todo.common.extentions.toUuid
import org.personal.todo.todo.dto.AddTodoRequest
import org.personal.todo.todo.dto.SearchTodoRequest
import org.personal.todo.todo.dto.TodoResponse
import org.personal.todo.todo.dto.UpdateTodoRequest
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController


@Tags(Tag(name = "User"), Tag(name = "Todo"))
@RestController
@RequestMapping("/api/todo")
class TodoController(
    private val todoService: TodoService
) {

    @GetMapping
    fun getTodo(
        @Valid @ParameterObject request: SearchTodoRequest,

        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: Pageable,

        @AuthenticationPrincipal currentUserId: String,
    ): ApiResponse<List<TodoResponse>> = todoService.getTodo(
        currentUserId,
        request,
        pageable
    )

    @PostMapping
    fun addTodo(
        @Valid @RequestBody request: AddTodoRequest,
        @AuthenticationPrincipal currentUserId: String,
    ): ApiResponse<TodoResponse> = todoService.addTodo(
        currentUserId,
        request,
    )

    @GetMapping("/{id}")
    fun getTodoById(
        @PathVariable id: String,
        @AuthenticationPrincipal currentUserId: String,
    ) : ApiResponse<TodoResponse> = todoService.getTodoById(currentUserId, id)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteTodoById(
        @PathVariable id: String,
        @AuthenticationPrincipal currentUserId: String,
    ) = todoService.deleteTodoById(currentUserId, id)

    @PutMapping("/{id}")
    fun updateTodoById(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateTodoRequest,
        @AuthenticationPrincipal currentUserId: String,
    ): ApiResponse<TodoResponse> = todoService.updateTodoById(
        currentUserId, id, request
    )
}