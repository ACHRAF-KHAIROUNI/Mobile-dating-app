package fr.unica.miage.komeet.application.favorites

import fr.unica.miage.komeet.api.dto.FavoriteDto
import fr.unica.miage.komeet.api.dto.PageResponse
import fr.unica.miage.komeet.api.dto.PaginationMeta
import fr.unica.miage.komeet.api.dto.toDto
import fr.unica.miage.komeet.domain.error.FavoriteNotFoundException
import fr.unica.miage.komeet.domain.error.ProfileNotFoundException
import fr.unica.miage.komeet.domain.error.ValidationException
import fr.unica.miage.komeet.domain.repository.FavoriteRepository
import fr.unica.miage.komeet.domain.repository.ProfileRepository

class FavoriteService(
    private val favoriteRepository: FavoriteRepository,
    private val profileRepository: ProfileRepository
) {
    fun listFavorites(userId: String, page: Int = 1, pageSize: Int = 20): PageResponse<FavoriteDto> {
        val safePage = page.coerceAtLeast(1)
        val safePageSize = pageSize.coerceIn(1, 100)

        if (page < 1) {
            throw ValidationException("Le numéro de page doit être supérieur ou égal à 1")
        }
        if (pageSize < 1 || pageSize > 100) {
            throw ValidationException("La taille de page doit être comprise entre 1 et 100")
        }

        val result = favoriteRepository.findByUserId(userId, safePage, safePageSize)
        return PageResponse(
            items = result.items.map { it.toDto() },
            pagination = PaginationMeta(
                page = result.page,
                pageSize = result.pageSize,
                totalItems = result.totalItems,
                totalPages = result.totalPages
            )
        )
    }

    fun addFavorite(userId: String, profileId: String): FavoriteDto {
        val profile = profileRepository.findById(profileId)
            ?: throw ProfileNotFoundException(profileId)

        favoriteRepository.add(userId, profileId)

        // Return the favorite details
        val userFavorites = favoriteRepository.findByUserId(userId, 1, 100)
        val created = userFavorites.items.find { it.profile.id == profileId }

        return created?.toDto() ?: FavoriteDto(
            profile = profile.toDto(),
            favoritedAt = java.time.Instant.now().toString()
        )
    }

    fun removeFavorite(userId: String, profileId: String) {
        val profile = profileRepository.findById(profileId)
            ?: throw ProfileNotFoundException(profileId)

        val removed = favoriteRepository.remove(userId, profileId)
        if (!removed) {
            throw FavoriteNotFoundException(profileId)
        }
    }
}
