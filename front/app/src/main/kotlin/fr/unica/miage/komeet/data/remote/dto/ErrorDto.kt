package fr.unica.miage.komeet.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val error: ErrorDetail
)

@Serializable
data class ErrorDetail(
    val code: String,
    val message: String,
    val details: List<String> = emptyList()
)
