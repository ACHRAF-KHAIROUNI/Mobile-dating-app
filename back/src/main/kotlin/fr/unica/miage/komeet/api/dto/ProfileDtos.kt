package fr.unica.miage.komeet.api.dto

import fr.unica.miage.komeet.domain.model.Location
import fr.unica.miage.komeet.domain.model.Profile
import kotlinx.serialization.Serializable

@Serializable
data class LocationDto(
    val streetNumber: Int? = null,
    val streetName: String? = null,
    val city: String,
    val department: String? = null,
    val postcode: String? = null,
    val country: String = "France"
)

@Serializable
data class ProfileDto(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String? = null,
    val age: Int,
    val gender: String? = null,
    val phone: String? = null,
    val location: LocationDto,
    val country: String,
    val city: String,
    val thumbnailUrl: String,
    val pictureUrl: String,
    val source: String,
    val createdAt: String
)

fun Location.toDto(): LocationDto = LocationDto(
    streetNumber = streetNumber,
    streetName = streetName,
    city = city,
    department = department,
    postcode = postcode,
    country = country
)

fun Profile.toDto(): ProfileDto = ProfileDto(
    id = id,
    firstName = firstName,
    lastName = lastName,
    email = email,
    age = age,
    gender = gender,
    phone = phone,
    location = location.toDto(),
    country = country,
    city = city,
    thumbnailUrl = thumbnailUrl,
    pictureUrl = pictureUrl,
    source = source,
    createdAt = createdAt.toString()
)
