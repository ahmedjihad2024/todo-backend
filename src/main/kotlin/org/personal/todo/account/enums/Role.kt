package org.personal.todo.account.enums

enum class Role {
    USER;

    fun authority(): String = "ROLE_$name"

    companion object {
        fun fromAuthority(authority: String?): Role? =
            entries.firstOrNull { it.authority() == authority }
    }
}