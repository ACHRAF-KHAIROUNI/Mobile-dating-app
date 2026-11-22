package fr.unica.miage.komeet.api.dto

import fr.unica.miage.komeet.domain.model.Region
import kotlinx.serialization.Serializable

@Serializable
data class RegionDto(
    val id: String,
    val name: String,
    val country: String,
    val code: String? = null
)

fun Region.toDto(): RegionDto = RegionDto(
    id = id,
    name = name,
    country = country,
    code = code
)
