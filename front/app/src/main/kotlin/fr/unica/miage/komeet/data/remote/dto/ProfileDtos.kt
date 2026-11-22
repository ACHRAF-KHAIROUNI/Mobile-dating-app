package fr.unica.miage.komeet.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LocationDto(
    val streetNumber: Int? = null,
    val streetName: String? = null,
    val city: String = "",
    val department: String? = null,
    val region: String? = department,
    val postcode: String? = null,
    val country: String = "France"
)

@Serializable
data class ProfileDto(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String? = null,
    val age: Int,
    val gender: String? = null,
    val phone: String? = null,
    val location: LocationDto,
    val country: String = "France",
    val city: String = "",
    val region: String? = location.region ?: location.department,
    val thumbnailUrl: String,
    val pictureUrl: String,
    val source: String = "randomuser",
    val sourceId: String? = null,
    val createdAt: String
)

@Serializable
data class PaginationDto(
    val page: Int = 1,
    val pageSize: Int = 20,
    val totalItems: Long = 0,
    val totalPages: Int = 1
) {
    // Alias pour la rétrocompatibilité si un composant utilise totalCount
    val totalCount: Long
        get() = totalItems
}

@Serializable
data class PaginatedProfilesResponse(
    val items: List<ProfileDto> = emptyList(),
    val pagination: PaginationDto = PaginationDto()
)

@Serializable
data class FavoriteDto(
    val profile: ProfileDto,
    val favoritedAt: String = "",
    val userId: String? = null,
    val createdAt: String? = null
)

@Serializable
data class PaginatedFavoritesResponse(
    val items: List<FavoriteDto> = emptyList(),
    val pagination: PaginationDto = PaginationDto()
)
