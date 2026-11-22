package fr.unica.miage.komeet.application.preferences

import fr.unica.miage.komeet.api.dto.UpdatePreferencesRequest
import fr.unica.miage.komeet.api.dto.UserPreferencesDto
import fr.unica.miage.komeet.api.dto.toDto
import fr.unica.miage.komeet.domain.error.ValidationException
import fr.unica.miage.komeet.domain.model.UserPreferences
import fr.unica.miage.komeet.domain.repository.PreferencesRepository
import java.time.Instant

class PreferencesService(
    private val preferencesRepository: PreferencesRepository
) {
    companion object {
        const val DEFAULT_MIN_AGE = 18
        const val DEFAULT_MAX_AGE = 99
    }

    fun getPreferences(userId: String): UserPreferencesDto {
        val existing = preferencesRepository.findByUserId(userId)
        return if (existing != null) {
            existing.toDto()
        } else {
            // Documented defaults when no preferences have been saved yet
            UserPreferencesDto(
                minAge = DEFAULT_MIN_AGE,
                maxAge = DEFAULT_MAX_AGE,
                country = null,
                region = null,
                updatedAt = Instant.EPOCH.toString()
            )
        }
    }

    fun updatePreferences(userId: String, request: UpdatePreferencesRequest): UserPreferencesDto {
        val validationErrors = mutableListOf<String>()

        if (request.minAge != null && request.minAge < 0) {
            validationErrors.add("Le paramètre 'minAge' ne peut pas être négatif")
        }
        if (request.maxAge != null && request.maxAge < 0) {
            validationErrors.add("Le paramètre 'maxAge' ne peut pas être négatif")
        }
        if (request.minAge != null && request.maxAge != null && request.minAge > request.maxAge) {
            validationErrors.add("Le paramètre 'minAge' ne peut pas être supérieur à 'maxAge'")
        }

        if (validationErrors.isNotEmpty()) {
            throw ValidationException("Préférences invalides", validationErrors)
        }

        val updated = UserPreferences(
            userId = userId,
            minAge = request.minAge,
            maxAge = request.maxAge,
            country = request.country?.trim()?.ifBlank { null },
            region = request.region?.trim()?.ifBlank { null },
            updatedAt = Instant.now()
        )

        preferencesRepository.save(updated)
        return updated.toDto()
    }
}
