package org.personal.todo.admin

import org.personal.todo.common.exception.ApiException
import org.personal.todo.account.dto.AccountResponse
import org.personal.todo.account.enums.Role
import org.personal.todo.account.entities.Account
import org.personal.todo.account.mapper.toResponse
import org.personal.todo.account.repository.AccountRepository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class AdminService(
    private val accountRepository: AccountRepository,
) {

//    fun listUsers(pageable: Pageable): Page<AccountResponse> =
//        accountRepository.findAll(pageable).map { it.toResponse() }
//
//    fun updateRoles(targetUserId: ObjectId, currentUserId: String, roleNames: Set<String>): Account {
//        if (targetUserId.toHexString() == currentUserId) {
//            throw ApiException.BadRequest("error.admin.cannot_modify_self")
//        }
//        val roles = roleNames.map { name ->
//            runCatching { Role.valueOf(name) }.getOrElse {
//                throw ApiException.BadRequest("error.admin.invalid_role")
//            }
//        }.toSet()
//        val user = accountRepository.findById(targetUserId).orElseThrow { ApiException.NotFound("Account") }
//        return accountRepository.save(user.copy(roles = roles))
//    }
}
