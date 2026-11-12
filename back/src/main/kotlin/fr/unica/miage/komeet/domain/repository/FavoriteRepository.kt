package fr.unica.miage.komeet.domain.repository

import fr.unica.miage.komeet.domain.model.Favorite

interface FavoriteRepository {
    fun findByUserId(userId: String, page: Int = 1, pageSize: Int = 20): PaginatedResult<Favorite>
    fun exists(userId: String, profileId: String): Boolean
    fun add(userId: String, profileId: String): Boolean
    fun remove(userId: String, profileId: String): Boolean
    fun removeAllByUserId(userId: String): Int
}
