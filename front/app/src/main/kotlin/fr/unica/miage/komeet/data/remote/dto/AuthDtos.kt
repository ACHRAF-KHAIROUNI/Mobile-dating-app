package fr.unica.miage.komeet.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val user: UserDto,
    val tokenType: String = "Bearer",
    val expiresIn: Long = 86400
)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val displayName: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class UpdateUserRequest(
    val displayName: String? = null
)

@Serializable
data class MessageResponse(
    val message: String = "",
    val profileId: String? = null
)
