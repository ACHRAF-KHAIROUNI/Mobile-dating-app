package fr.unica.miage.komeet.infrastructure.randomuser

import fr.unica.miage.komeet.domain.model.Location
import fr.unica.miage.komeet.domain.model.Profile
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.UUID

interface RandomUserDataSource {
    suspend fun fetchProfiles(count: Int, seed: String? = null, nationality: String? = "fr"): List<Profile>
}

fun formatFrenchPhoneNumber(phone: String?): String? {
    if (phone.isNullOrBlank()) return null
    val digits = phone.filter { it.isDigit() }
    return when {
        digits.length == 10 -> digits.chunked(2).joinToString(" ")
        digits.length == 9 && !digits.startsWith("0") -> ("0$digits").chunked(2).joinToString(" ")
        digits.length == 11 && digits.startsWith("33") -> ("0" + digits.substring(2)).chunked(2).joinToString(" ")
        else -> phone
    }
}

class KtorRandomUserClient(
    private val client: HttpClient,
    private val apiUrl: String = "https://randomuser.me/api/"
) : RandomUserDataSource {

    private val logger = LoggerFactory.getLogger(KtorRandomUserClient::class.java)

    companion object {
        fun createDefault(apiUrl: String = "https://randomuser.me/api/"): KtorRandomUserClient {
            val httpClient = HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    })
                }
                install(HttpTimeout) {
                    requestTimeoutMillis = 15000
                    connectTimeoutMillis = 10000
                    socketTimeoutMillis = 15000
                }
            }
            return KtorRandomUserClient(httpClient, apiUrl)
        }
    }

    override suspend fun fetchProfiles(count: Int, seed: String?, nationality: String?): List<Profile> {
        logger.info("Fetching $count profiles from Random User Generator at $apiUrl (seed=$seed, nat=$nationality)...")
        try {
            val response: RandomUserApiResponse = client.get(apiUrl) {
                parameter("results", count)
                if (!seed.isNullOrBlank()) {
                    parameter("seed", seed)
                }
                if (!nationality.isNullOrBlank()) {
                    parameter("nat", nationality)
                }
            }.body()

            val profiles = response.results.map { item ->
                val sourceUuid = item.login?.uuid?.takeIf { it.isNotBlank() }
                val profileId = sourceUuid ?: UUID.nameUUIDFromBytes(
                    "${item.name.first}_${item.name.last}_${item.email}".toByteArray()
                ).toString()

                val rawPostcode = when (val elem = item.location.postcode) {
                    is JsonPrimitive -> elem.content
                    else -> null
                }
                val formattedPostcode = rawPostcode?.let {
                    if (it.all { ch -> ch.isDigit() } && it.length < 5) it.padStart(5, '0') else it
                }

                val location = Location(
                    streetNumber = item.location.street?.number,
                    streetName = item.location.street?.name,
                    city = item.location.city,
                    department = item.location.state,
                    postcode = formattedPostcode,
                    country = item.location.country
                )

                Profile(
                    id = profileId,
                    firstName = item.name.first,
                    lastName = item.name.last,
                    email = item.email,
                    age = item.dob.age,
                    gender = item.gender,
                    phone = formatFrenchPhoneNumber(item.phone ?: item.cell),
                    location = location,
                    thumbnailUrl = item.picture.thumbnail,
                    pictureUrl = item.picture.large,
                    source = "randomuser.me",
                    sourceId = sourceUuid,
                    createdAt = Instant.now()
                )
            }
            logger.info("Successfully retrieved ${profiles.size} profiles from Random User Generator")
            return profiles
        } catch (e: HttpRequestTimeoutException) {
            logger.error("Timeout while contacting Random User Generator: ${e.message}")
            throw e
        } catch (e: Exception) {
            logger.error("Failed to fetch profiles from Random User Generator: ${e.message}", e)
            throw e
        }
    }
}
