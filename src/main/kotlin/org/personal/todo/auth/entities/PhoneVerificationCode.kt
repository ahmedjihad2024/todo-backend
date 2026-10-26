package org.personal.todo.auth.entities

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.personal.todo.account.entities.Account
import org.personal.todo.account.enums.Role
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "phone_verification_codes",
    indexes = [
        Index(name = "idx_phone_verification_codes_expires_at", columnList = "expires_at")
    ]
)
class PhoneVerificationCode(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    val account: Account,

    @Column(nullable = false, length = 100)
    var code: String,

    // The persona the caller is logging in as, carried so verifyPhone can mint a token scoped
    // to just this role, without a separate verify endpoint per persona.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var role: Role,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(nullable = false)
    var expiresAt: Instant,
)