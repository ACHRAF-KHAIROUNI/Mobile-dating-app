package fr.unica.miage.komeet.infrastructure.randomuser

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class RandomUserApiResponse(
    val results: List<RandomUserResult> = emptyList(),
    val info: RandomUserInfo? = null
)

@Serializable
data class RandomUserInfo(
    val seed: String? = null,
    val results: Int = 0,
    val page: Int = 1,
    val version: String? = null
)

@Serializable
data class RandomUserResult(
    val gender: String? = null,
    val name: RandomUserName,
    val location: RandomUserLocation,
    val email: String? = null,
    val login: RandomUserLogin? = null,
    val dob: RandomUserDob,
    val phone: String? = null,
    val cell: String? = null,
    val picture: RandomUserPicture
)

@Serializable
data class RandomUserName(
    val title: String? = null,
    val first: String,
    val last: String
)

@Serializable
data class RandomUserStreet(
    val number: Int? = null,
    val name: String? = null
)

@Serializable
data class RandomUserLocation(
    val street: RandomUserStreet? = null,
    val city: String,
    val state: String? = null,
    val country: String,
    val postcode: JsonElement? = null
)

@Serializable
data class RandomUserLogin(
    val uuid: String? = null,
    val username: String? = null
)

@Serializable
data class RandomUserDob(
    val date: String? = null,
    val age: Int
)

@Serializable
data class RandomUserPicture(
    val large: String,
    val medium: String? = null,
    val thumbnail: String
)
