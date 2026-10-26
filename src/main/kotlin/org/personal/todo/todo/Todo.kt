package org.personal.todo.todo

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.personal.todo.account.entities.Account
import java.time.Instant
import java.util.UUID


@Entity
@Table(
    name = "todos",
    indexes = [
        Index(name = "idx_todos_account_created", columnList = "account_id, created_at"),
        // Honestly, for a _todo app, [idx_todos_account_created] alone is enough at the start.
        // Add the others when you see slow queries.
        // Index(name = "idx_todos_account_completed", columnList = "account_id, completed"),
        // Index(name = "idx_todos_account_priority", columnList = "account_id, priority"),
    ]
)
data class Todo (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "account_id", nullable = false)
    val account: Account,

    @Column(nullable = false, length = 400)
    var title: String,
    var description: String? = null,
    var completed: Boolean = false,
    var dueDate: Instant? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var priority: Priority,

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: Instant? = null,
)