package fr.unica.miage.komeet.api.dto

import fr.unica.miage.komeet.domain.model.User
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val displayName: String,
    val region: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class UpdateUserRequest(
    val displayName: String? = null,
    val region: String? = null
)

fun User.toDto(): UserDto = UserDto(
    id = id,
    email = email,
    displayName = displayName,
    region = region,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)
