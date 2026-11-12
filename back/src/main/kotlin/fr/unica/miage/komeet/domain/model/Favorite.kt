package fr.unica.miage.komeet.domain.model

import java.time.Instant

data class Favorite(
    val userId: String,
    val profile: Profile,
    val createdAt: Instant
)
