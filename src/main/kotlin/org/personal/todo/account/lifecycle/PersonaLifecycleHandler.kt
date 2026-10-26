package org.personal.todo.account.lifecycle

import org.personal.todo.account.enums.Role
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class PersonaLifecycleHandler(
    private val resources: List<PersonaOwnedResource>,
) {
    @EventListener
    fun onDeactivated(event: PersonaRoleDeactivated) =
        forRole(event.role) { it.deactivate(event.accountId) }

    @EventListener
    fun onRestored(event: PersonaRoleRestored) =
        forRole(event.role) { it.reactivate(event.accountId) }

    @EventListener
    fun onPurged(event: PersonaRolePurged) {
        val cleanups = resources
            .filter { it.role == event.role }
            .flatMap { it.preparePurge(event.accountId) }

        if (cleanups.isEmpty()) return
        check(TransactionSynchronizationManager.isSynchronizationActive()) {
            "Persona purge cleanup must be prepared inside an active transaction"
        }
        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    cleanups.forEach { it() }
                }
            },
        )
    }

    private fun forRole(role: Role, action: (PersonaOwnedResource) -> Unit) {
        resources.filter { it.role == role }.forEach(action)
    }
}
