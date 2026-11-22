package fr.unica.miage.komeet.domain.model

data class Region(
    val id: String,
    val name: String,
    val country: String,
    val code: String? = null
)
