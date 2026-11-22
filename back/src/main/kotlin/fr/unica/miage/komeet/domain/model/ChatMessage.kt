package fr.unica.miage.komeet.domain.model

import java.time.Instant

data class ChatMessage(
    val id: String,
    val userId: String,
    val profileId: String,
    val sender: String, // "user" or "profile"
    val content: String,
    val createdAt: Instant
)
