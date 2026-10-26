package org.personal.todo.todo

import org.personal.todo.account.entities.Account
import org.springframework.data.domain.Page
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Modifying
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

interface TodoRepository: JpaRepository<Todo, UUID> {
    @Query("""
    SELECT t FROM Todo t
    WHERE t.account.id = :accountId
      AND (:completed IS NULL OR t.completed = :completed)
      AND (:priority IS NULL OR t.priority = :priority)
      AND (CAST(:dueDate AS java.time.Instant) IS NULL OR t.dueDate = :dueDate)
      AND (:pattern IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', CAST(:pattern AS STRING), '%')) ESCAPE '\')
""")
    fun search(
        accountId: UUID,
        completed: Boolean?,
        priority: Priority?,
        pattern: String?,
        dueDate: Instant?,
        pageable: Pageable
    ): Page<Todo>


    fun findByAccountAndId(account: Account, id: UUID): Todo?

    @Modifying
    @Transactional
    @Query("DELETE FROM Todo t WHERE t.id = :id AND t.account.id = :accountId")
    fun deleteByIdAndAccountId(id: UUID, accountId: UUID): Int

}