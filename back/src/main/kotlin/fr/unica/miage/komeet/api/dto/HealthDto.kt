package fr.unica.miage.komeet.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class HealthDto(
    val status: String,
    val version: String = "1.0.0",
    val database: String,
    val profilesCount: Long,
    val timestamp: String
)
