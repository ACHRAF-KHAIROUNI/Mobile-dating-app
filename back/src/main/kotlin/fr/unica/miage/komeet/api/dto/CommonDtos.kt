package fr.unica.miage.komeet.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorEnvelope(
    val error: ErrorDetails
)

@Serializable
data class ErrorDetails(
    val code: String,
    val message: String,
    val details: List<String> = emptyList()
)

@Serializable
data class PaginationMeta(
    val page: Int,
    val pageSize: Int,
    val totalItems: Long,
    val totalPages: Int
)

@Serializable
data class PageResponse<T>(
    val items: List<T>,
    val pagination: PaginationMeta
)

@Serializable
data class MessageResponse(
    val message: String
)
