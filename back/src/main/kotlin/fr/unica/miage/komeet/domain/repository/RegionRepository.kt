package fr.unica.miage.komeet.domain.repository

import fr.unica.miage.komeet.domain.model.Region

interface RegionRepository {
    fun findAll(country: String? = null): List<Region>
    fun findById(id: String): Region?
    fun saveAll(regions: List<Region>): Int
    fun count(): Long
}
