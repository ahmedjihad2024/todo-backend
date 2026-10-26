package org.personal.todo.account.repository

import org.personal.todo.account.entities.Account
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface AccountRepository : JpaRepository<Account, UUID> {

    fun findByPhone(phone: String): Account?

    @Query("select a.phone as phone from Account a where a.id = :id")
    fun findPhoneById(@Param("id") id: UUID): UserAccountPhone?
}

interface UserAccountPhone {
    val phone: String?
}