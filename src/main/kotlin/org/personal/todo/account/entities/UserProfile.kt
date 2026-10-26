package org.personal.todo.account.entities

import jakarta.persistence.*
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "user_profiles")
class UserProfile(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    var account: Account,

    @Column(length = 25)
    var firstName: String? = null,

    @Column(length = 25)
    var lastName: String? = null,

    @Column(length = 100, unique = true)
    var email: String? = null,

    @Column
    var avatarFilename: String? = null,

    @Column(nullable = false)
    var registered: Boolean = false,

    @Column(nullable = false)
    var deleted: Boolean = false,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column
    var deletedAt: Instant? = null,
)