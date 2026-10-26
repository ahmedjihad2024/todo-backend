package org.personal.todo.account.entities

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Index
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.util.UUID
import org.personal.todo.account.enums.Role

@Entity
@Table(
    name = "accounts",
    indexes = [
        Index(name = "idx_accounts_phone",columnList = "phone")
    ]
)
data class Account(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(
        unique = true,
        nullable = false,
        length = 20
    )
    var phone: String,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "account_roles", joinColumns = [JoinColumn(name = "account_id")])
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val roles: MutableSet<Role> = mutableSetOf(Role.USER),

    @Version
    val version: Long? = null,
) {
    companion object {
        /** Grace period a soft-deleted persona is kept before it is purged. */
        const val DELETED_RETENTION_DAYS = 30L

        /** Name shown to other users in place of a deleted account. */
        const val DELETED_DISPLAY_NAME = "Deleted account"
    }
}
