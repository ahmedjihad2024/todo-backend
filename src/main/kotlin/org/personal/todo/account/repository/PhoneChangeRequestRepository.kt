package org.personal.todo.account.repository

import org.personal.todo.account.entities.PhoneChangeRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.UUID

interface PhoneChangeRequestRepository : JpaRepository<PhoneChangeRequest, UUID> {
    fun findByAccountId(accountId: UUID): PhoneChangeRequest?
    fun deleteByAccountId(accountId: UUID)

    @Modifying
    @Query("delete from PhoneChangeRequest r where r.expiresAt < :now")
    fun deleteExpired(now: Instant): Int
}
