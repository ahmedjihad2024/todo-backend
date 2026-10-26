package org.personal.todo.todo

import org.personal.todo.account.repository.AccountRepository
import org.personal.todo.account.repository.UserProfileRepository
import org.personal.todo.auth.AuthService
import org.personal.todo.common.dto.ApiResponse
import org.personal.todo.common.exception.ApiException
import org.personal.todo.common.extentions.toUuid
import org.personal.todo.todo.dto.AddTodoRequest
import org.personal.todo.todo.dto.SearchTodoRequest
import org.personal.todo.todo.dto.TodoResponse
import org.personal.todo.todo.dto.UpdateTodoRequest
import org.springframework.stereotype.Service
import org.springframework.data.domain.Pageable
import java.util.UUID

@Service
class TodoService(
    private val todoRepository: TodoRepository,
    private val accountRepo: AccountRepository
) {

    fun getTodo( accountId: String, request: SearchTodoRequest, pageable: Pageable): ApiResponse<List<TodoResponse>> {
        val pattern: String? = request.q
            ?.replace("\\", "\\\\")
            ?.replace("%", "\\%")
            ?.replace("_", "\\_")

        val result = todoRepository.search(
            accountId = accountId.toUuid(),
            completed = request.completed,
            priority = request.priority,
            pattern = pattern,
            pageable = pageable,
            dueDate = request.dueDate
        ).map { it.toResponse() }

        return ApiResponse.paged(result)
    }


    fun addTodo(accountId: String, request: AddTodoRequest): ApiResponse<TodoResponse> {
        val result = todoRepository.save(
            Todo(
                account = accountRepo.getReferenceById(accountId.toUuid()),
                title = request.title,
                description = request.description,
                completed = request.completed ?: false,
                dueDate = request.dueDate,
                priority = request.priority,
            )
        )

        return ApiResponse.ok(result.toResponse())
    }

    fun getTodoById(accountId: String, noteId: String): ApiResponse<TodoResponse>{
        val result = todoRepository.findByAccountAndId(
            accountRepo.getReferenceById(accountId.toUuid()),
            noteId.toUuid()
        ) ?: throw ApiException.NotFound("{validation.todo_id.not_found}")

        return ApiResponse.ok(result.toResponse())
    }

    fun deleteTodoById(accountId: String, noteId: String) {
        todoRepository.deleteByIdAndAccountId(
            noteId.toUuid(),
            accountId.toUuid()
        )
    }

    fun updateTodoById(accountId: String, noteId: String, request: UpdateTodoRequest) : ApiResponse<TodoResponse> {
        val result = todoRepository.findByAccountAndId(
            accountRepo.getReferenceById(accountId.toUuid()),
            noteId.toUuid()
        ) ?: throw ApiException.NotFound("{validation.todo_id.not_found}")

        val updateResult = result.copy(
            title = request.title ?: result.title,
            description = request.description ?: result.description,
            priority = request.priority ?: result.priority,
            completed = request.completed ?: result.completed,
            dueDate = request.dueDate ?: result.dueDate,
        )

        return ApiResponse.ok(todoRepository.save(updateResult).toResponse())
    }
}








