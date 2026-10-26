package org.personal.todo.auth.repository

import org.personal.todo.auth.entities.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {

    fun findByAccountIdAndHashedToken(accountId: UUID, hashedToken: String): RefreshToken?

    @Modifying
    @Query("delete from RefreshToken t where t.account.id = :accountId and t.hashedToken = :hashedToken")
    fun deleteByAccountIdAndHashedToken(
        @Param("accountId") accountId: UUID,
        @Param("hashedToken") hashedToken: String,
    ): Int

    @Modifying
    @Query("delete from RefreshToken t where t.account.id = :accountId")
    fun deleteAllByAccountId(@Param("accountId") accountId: UUID): Int

    @Modifying
    @Query("delete from RefreshToken t where t.expiresAt < :now")
    fun deleteExpired(@Param("now") now: Instant): Int
}