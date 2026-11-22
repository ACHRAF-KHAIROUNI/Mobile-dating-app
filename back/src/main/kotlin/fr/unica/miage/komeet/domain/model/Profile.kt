package fr.unica.miage.komeet.domain.model

import java.time.Instant

data class Location(
    val streetNumber: Int? = null,
    val streetName: String? = null,
    val city: String,
    val department: String? = null,
    val postcode: String? = null,
    val country: String = "France",
    val region: String? = department
)

data class Profile(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String?,
    val age: Int,
    val gender: String? = null,
    val phone: String? = null,
    val location: Location,
    val thumbnailUrl: String,
    val pictureUrl: String,
    val source: String,
    val sourceId: String?,
    val createdAt: Instant
) {
    val country: String get() = location.country
    val city: String get() = location.city
    val region: String? get() = location.region ?: location.department
}
