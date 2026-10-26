package org.personal.todo.account.repository

import org.personal.todo.account.entities.UserProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface UserProfileRepository : JpaRepository<UserProfile, UUID> {

    fun findByAccountId(accountId: UUID): UserProfile?

    fun existsByAccountId(accountId: UUID): Boolean

    @Modifying
    @Query("delete from UserProfile p where p.account.id = :accountId")
    fun deleteByAccountId(@Param("accountId") accountId: UUID)

    /** Account IDs of expired soft-deleted personas, without loading the full profile entities. */
    @Query("""
        select p.account.id as accountId
        from UserProfile p
        where p.deleted = true and p.deletedAt < :cutoff
    """)
    fun findExpiredDeletedAccountIds(@Param("cutoff") cutoff: Instant): List<DeletedProfileAccount>

    @Query("""
        select p.account.id as accountId, p.firstName as firstName, p.lastName as lastName
        from UserProfile p
        where p.account.id = :accountId
    """)
    fun findNameByAccountId(@Param("accountId") accountId: UUID): UserProfileName?

    @Query("""
        select p.account.id as accountId, p.firstName as firstName, p.lastName as lastName
        from UserProfile p
        where p.account.id in :accountIds
    """)
    fun findNamesByAccountIds(@Param("accountIds") accountIds: Collection<UUID>): List<UserProfileName>
}

interface UserProfileName {
    val accountId: UUID
    val firstName: String?
    val lastName: String?
}

interface DeletedProfileAccount {
    val accountId: UUID
}