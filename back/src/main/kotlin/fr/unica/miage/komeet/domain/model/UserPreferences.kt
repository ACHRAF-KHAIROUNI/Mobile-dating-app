package fr.unica.miage.komeet.domain.model

import java.time.Instant

data class UserPreferences(
    val userId: String,
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val country: String? = null,
    val region: String? = null,
    val updatedAt: Instant = Instant.now()
)
