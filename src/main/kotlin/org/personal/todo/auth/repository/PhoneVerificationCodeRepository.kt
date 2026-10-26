package org.personal.todo.auth.repository

import org.personal.todo.auth.entities.PhoneVerificationCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface PhoneVerificationCodeRepository : JpaRepository<PhoneVerificationCode, UUID> {

    fun findByAccountId(accountId: UUID): PhoneVerificationCode?

    @Modifying
    @Query("delete from PhoneVerificationCode c where c.account.id = :accountId")
    fun deleteByAccountId(@Param("accountId") accountId: UUID): Int

    @Modifying
    @Query("delete from PhoneVerificationCode c where c.expiresAt < :now")
    fun deleteExpired(@Param("now") now: Instant): Int
}