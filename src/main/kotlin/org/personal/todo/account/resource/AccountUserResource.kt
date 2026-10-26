package org.personal.todo.account.resource

import org.personal.todo.account.ProfileData
import org.personal.todo.account.ProfileService
import org.personal.todo.account.enums.Role
import org.personal.todo.account.lifecycle.PersonaOwnedResource
import org.personal.todo.common.storage.FileCategory
import org.personal.todo.common.storage.FileStorageService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AccountUserResource(
    private val profileService: ProfileService,
    private val fileStorageService: FileStorageService,
) : PersonaOwnedResource {
    override val role = Role.USER

    override fun preparePurge(accountId: UUID): List<() -> Unit> {
        val avatarFilename = (profileService.get(accountId, role) as? ProfileData.UserProfileData)
            ?.avatarFilename
            ?: return emptyList()

        return listOf {
            fileStorageService.delete(FileCategory.AVATAR, avatarFilename)
        }
    }
}
