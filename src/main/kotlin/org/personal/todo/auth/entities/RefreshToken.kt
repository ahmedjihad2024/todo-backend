package org.personal.todo.auth.entities

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.personal.todo.account.entities.Account
import org.personal.todo.account.enums.Role
import java.time.Instant
import java.util.UUID

// Compound index on (account_id, hashed_token) covers findByAccountIdAndHashedToken,
// deleteByAccountIdAndHashedToken, and deleteAllByAccountId (prefix match on account_id).
@Entity
@Table(
    name = "refresh_tokens",
    indexes = [
        Index(name = "idx_refresh_tokens_account_hashed_token", columnList = "account_id, hashed_token"),
        Index(name = "idx_refresh_tokens_expires_at", columnList = "expires_at"),
    ]
)
class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "account_id", nullable = false)
    val account: Account,

    @Column(name = "hashed_token", nullable = false, length = 255)
    val hashedToken: String,

    // The scoped role of this session, so refresh re-issues a token for the SAME persona
    // instead of silently widening back to all of the account's roles.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val role: Role,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(nullable = false)
    val expiresAt: Instant,
)