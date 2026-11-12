package fr.unica.miage.komeet.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PreferencesDto(
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val country: String? = null,
    val updatedAt: String = "",
    val userId: String? = null
)

@Serializable
data class UpdatePreferencesRequest(
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val country: String? = null
)
