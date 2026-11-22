package fr.unica.miage.komeet.api.dto

import fr.unica.miage.komeet.domain.model.UserPreferences
import kotlinx.serialization.Serializable

@Serializable
data class UserPreferencesDto(
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val country: String? = null,
    val region: String? = null,
    val updatedAt: String
)

@Serializable
data class UpdatePreferencesRequest(
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val country: String? = null,
    val region: String? = null
)

fun UserPreferences.toDto(): UserPreferencesDto = UserPreferencesDto(
    minAge = minAge,
    maxAge = maxAge,
    country = country,
    region = region,
    updatedAt = updatedAt.toString()
)
