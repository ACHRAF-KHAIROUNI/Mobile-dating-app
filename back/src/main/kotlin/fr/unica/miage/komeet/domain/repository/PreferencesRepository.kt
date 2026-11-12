package fr.unica.miage.komeet.domain.repository

import fr.unica.miage.komeet.domain.model.UserPreferences

interface PreferencesRepository {
    fun findByUserId(userId: String): UserPreferences?
    fun save(preferences: UserPreferences): UserPreferences
    fun deleteByUserId(userId: String): Boolean
}
