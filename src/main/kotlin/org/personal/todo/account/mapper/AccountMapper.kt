package org.personal.todo.account.mapper

import org.personal.todo.common.extentions.toMediaUrl
import org.personal.todo.common.storage.FileCategory
import org.personal.todo.account.dto.AccountResponse
import org.personal.todo.account.entities.Account
import org.personal.todo.account.entities.UserProfile
import org.personal.todo.account.enums.Role
import org.personal.todo.account.ProfileData
import org.personal.todo.common.exception.ApiException


fun Account.toResponse(role: Role, profile: ProfileData): AccountResponse {
    val id = id.toString()
    return when (role) {
        Role.USER if profile is ProfileData.UserProfileData -> {
            AccountResponse.UserResponse(
                id = id, firstName = profile.firstName, lastName = profile.lastName,
                phone = phone, email = profile.email, registered = profile.registered,
                avatar = profile.avatarFilename.toMediaUrl(FileCategory.AVATAR),
            )
        }
        else -> {
            throw ApiException.NotFound("error.account.not_found")
        }
    }
}

fun UserProfile.toProfileData() = ProfileData.UserProfileData(
    firstName, lastName, email, avatarFilename, registered,
)