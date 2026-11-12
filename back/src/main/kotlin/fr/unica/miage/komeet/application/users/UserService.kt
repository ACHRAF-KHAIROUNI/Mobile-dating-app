package fr.unica.miage.komeet.application.users

import fr.unica.miage.komeet.api.dto.UpdateUserRequest
import fr.unica.miage.komeet.api.dto.UserDto
import fr.unica.miage.komeet.api.dto.toDto
import fr.unica.miage.komeet.domain.error.UserNotFoundException
import fr.unica.miage.komeet.domain.error.ValidationException
import fr.unica.miage.komeet.domain.repository.FavoriteRepository
import fr.unica.miage.komeet.domain.repository.PreferencesRepository
import fr.unica.miage.komeet.domain.repository.UserRepository
import java.time.Instant

class UserService(
    private val userRepository: UserRepository,
    private val favoriteRepository: FavoriteRepository,
    private val preferencesRepository: PreferencesRepository
) {
    fun getMe(userId: String): UserDto {
        val user = userRepository.findById(userId)
            ?: throw UserNotFoundException(userId)
        return user.toDto()
    }

    fun updateMe(userId: String, request: UpdateUserRequest): UserDto {
        val existing = userRepository.findById(userId)
            ?: throw UserNotFoundException(userId)

        val newDisplayName = request.displayName?.trim() ?: existing.displayName
        if (newDisplayName.isBlank()) {
            throw ValidationException("Le nom d'affichage ne peut pas être vide")
        }

        val updated = existing.copy(
            displayName = newDisplayName,
            updatedAt = Instant.now()
        )

        userRepository.update(updated)
        return updated.toDto()
    }

    fun deleteMe(userId: String) {
        val existing = userRepository.findById(userId)
            ?: throw UserNotFoundException(userId)

        // Clean user's personal data (favorites and preferences)
        favoriteRepository.removeAllByUserId(userId)
        preferencesRepository.deleteByUserId(userId)
        userRepository.delete(existing.id)
    }
}
