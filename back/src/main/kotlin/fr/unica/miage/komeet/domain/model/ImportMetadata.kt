package fr.unica.miage.komeet.domain.model

import java.time.Instant

data class ImportMetadata(
    val id: String,
    val lastImportAt: Instant,
    val profilesCount: Int,
    val source: String
)
