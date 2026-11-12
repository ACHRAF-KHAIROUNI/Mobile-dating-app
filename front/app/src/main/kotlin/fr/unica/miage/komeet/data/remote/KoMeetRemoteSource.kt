package fr.unica.miage.komeet.data.remote

import fr.unica.miage.komeet.data.remote.dto.AuthResponse
import fr.unica.miage.komeet.data.remote.dto.ErrorResponse
import fr.unica.miage.komeet.data.remote.dto.FavoriteDto
import fr.unica.miage.komeet.data.remote.dto.LoginRequest
import fr.unica.miage.komeet.data.remote.dto.MessageResponse
import fr.unica.miage.komeet.data.remote.dto.PaginatedFavoritesResponse
import fr.unica.miage.komeet.data.remote.dto.PaginatedProfilesResponse
import fr.unica.miage.komeet.data.remote.dto.PreferencesDto
import fr.unica.miage.komeet.data.remote.dto.ProfileDto
import fr.unica.miage.komeet.data.remote.dto.RegisterRequest
import fr.unica.miage.komeet.data.remote.dto.UpdatePreferencesRequest
import fr.unica.miage.komeet.data.remote.dto.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

class ApiException(
    val code: String,
    override val message: String,
    val statusCode: Int = 0
) : Exception(message)

class KoMeetRemoteSource(private val client: HttpClient) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private suspend inline fun <reified T> handleResponse(response: HttpResponse): T {
        if (response.status.isSuccess()) {
            return response.body<T>()
        } else {
            val errorBody = response.bodyAsText()
            try {
                val errorResponse = json.decodeFromString<ErrorResponse>(errorBody)
                throw ApiException(
                    code = errorResponse.error.code,
                    message = errorResponse.error.message,
                    statusCode = response.status.value
                )
            } catch (e: ApiException) {
                throw e
            } catch (e: Exception) {
                throw ApiException(
                    code = "HTTP_${response.status.value}",
                    message = if (errorBody.isNotBlank()) errorBody else "Erreur serveur HTTP ${response.status.value}",
                    statusCode = response.status.value
                )
            }
        }
    }

    // --- AUTH ---
    suspend fun register(request: RegisterRequest): AuthResponse {
        val response = client.post("auth/register") {
            setBody(request)
        }
        return handleResponse(response)
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        val response = client.post("auth/login") {
            setBody(request)
        }
        return handleResponse(response)
    }

    suspend fun logout(): MessageResponse {
        val response = client.post("auth/logout")
        return handleResponse(response)
    }

    suspend fun getMe(): UserDto {
        val response = client.get("users/me")
        return handleResponse(response)
    }

    // --- PROFILES (À utiliser pour l'application Tinder par les étudiants) ---
    suspend fun getProfiles(
        page: Int = 1,
        pageSize: Int = 20,
        gender: String? = null,
        minAge: Int? = null,
        maxAge: Int? = null,
        department: String? = null,
        country: String? = null,
        search: String? = null,
        sort: String? = null,
        order: String? = null
    ): PaginatedProfilesResponse {
        val response = client.get("profiles") {
            parameter("page", page)
            parameter("pageSize", pageSize)
            gender?.let { parameter("gender", it) }
            minAge?.let { parameter("minAge", it) }
            maxAge?.let { parameter("maxAge", it) }
            department?.let { parameter("department", it) }
            country?.let { parameter("country", it) }
            search?.let { parameter("search", it) }
            sort?.let { parameter("sort", it) }
            order?.let { parameter("order", it) }
        }
        return handleResponse(response)
    }

    suspend fun getProfileById(id: String): ProfileDto {
        val response = client.get("profiles/$id")
        return handleResponse(response)
    }

    // --- FAVORITES ---
    suspend fun getFavorites(page: Int = 1, pageSize: Int = 20): PaginatedFavoritesResponse {
        val response = client.get("favorites") {
            parameter("page", page)
            parameter("pageSize", pageSize)
        }
        return handleResponse(response)
    }

    suspend fun addFavorite(profileId: String): FavoriteDto {
        val response = client.post("favorites/$profileId")
        return handleResponse(response)
    }

    suspend fun removeFavorite(profileId: String): MessageResponse {
        val response = client.delete("favorites/$profileId")
        return handleResponse(response)
    }

    // --- PREFERENCES ---
    suspend fun getPreferences(): PreferencesDto {
        val response = client.get("preferences")
        return handleResponse(response)
    }

    suspend fun updatePreferences(request: UpdatePreferencesRequest): PreferencesDto {
        val response = client.put("preferences") {
            setBody(request)
        }
        return handleResponse(response)
    }
}
