package fr.unica.miage.komeet.domain.repository

import fr.unica.miage.komeet.domain.model.Profile

data class ProfileSearchParams(
    val query: String? = null,
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val gender: String? = null,
    val department: String? = null,
    val country: String? = null,
    val region: String? = null,
    val sort: String? = "name",
    val order: String? = "asc",
    val page: Int = 1,
    val pageSize: Int = 20
)

data class PaginatedResult<T>(
    val items: List<T>,
    val totalItems: Long,
    val page: Int,
    val pageSize: Int
) {
    val totalPages: Int
        get() = if (pageSize <= 0) 1 else kotlin.math.ceil(totalItems.toDouble() / pageSize).toInt().coerceAtLeast(1)
}

interface ProfileRepository {
    fun findById(id: String): Profile?
    fun findAll(params: ProfileSearchParams): PaginatedResult<Profile>
    fun saveAll(profiles: List<Profile>): Int
    fun count(): Long
    fun deleteAll(): Unit
}
