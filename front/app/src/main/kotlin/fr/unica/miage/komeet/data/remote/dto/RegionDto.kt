package fr.unica.miage.komeet.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegionDto(
    val id: String,
    val name: String,
    val country: String,
    val code: String? = null
)
