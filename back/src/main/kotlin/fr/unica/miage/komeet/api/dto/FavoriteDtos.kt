package fr.unica.miage.komeet.api.dto

import fr.unica.miage.komeet.domain.model.Favorite
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteDto(
    val profile: ProfileDto,
    val favoritedAt: String
)

@Serializable
data class FavoriteActionResponse(
    val message: String,
    val profileId: String
)

fun Favorite.toDto(): FavoriteDto = FavoriteDto(
    profile = profile.toDto(),
    favoritedAt = createdAt.toString()
)
