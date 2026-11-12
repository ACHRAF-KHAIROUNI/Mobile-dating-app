package fr.unica.miage.komeet.domain.repository

import fr.unica.miage.komeet.domain.model.ImportMetadata

interface ImportMetadataRepository {
    fun findById(id: String = "default"): ImportMetadata?
    fun save(metadata: ImportMetadata): ImportMetadata
}
