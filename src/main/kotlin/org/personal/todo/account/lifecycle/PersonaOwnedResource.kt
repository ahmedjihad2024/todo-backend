package org.personal.todo.account.lifecycle

import org.personal.todo.account.enums.Role
import java.util.UUID

interface PersonaOwnedResource {
    val role: Role

    fun deactivate(accountId: UUID) {}

    fun reactivate(accountId: UUID) {}

    /** Capture external-resource references before the owning profile and cascading rows are deleted. */
    fun preparePurge(accountId: UUID): List<() -> Unit> = emptyList()
}
