package org.personal.todo.account.entities

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "phone_change_requests",
    indexes = [
        Index(name = "idx_phone_change_requests_expires_at", columnList = "expires_at")
    ]
)
class PhoneChangeRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    val account: Account,

    @Column(nullable = false, length = 20)
    var newPhone: String,

    @Column(nullable = false, length = 100)
    var code: String,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(nullable = false)
    var expiresAt: Instant,
)