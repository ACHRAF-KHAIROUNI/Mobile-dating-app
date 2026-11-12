package fr.unica.miage.komeet.data.remote.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DtoSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun testDeserializeAuthResponse() {
        val payload = """
            {
              "accessToken": "eyJhbGciOi...",
              "tokenType": "Bearer",
              "expiresIn": 86400,
              "user": {
                "id": "8e5f8f3b-4179-4d6b-bd85-e10db35a1a12",
                "email": "jean.dupont@example.com",
                "displayName": "Jean Dupont",
                "createdAt": "2026-10-17T15:00:00Z",
                "updatedAt": "2026-10-17T15:00:00Z"
              }
            }
        """.trimIndent()

        val response = json.decodeFromString<AuthResponse>(payload)
        assertEquals("eyJhbGciOi...", response.accessToken)
        assertEquals("Jean Dupont", response.user.displayName)
    }

    @Test
    fun testDeserializeProfilesResponseWithPagination() {
        val payload = """
            {
              "items": [
                {
                  "id": "e350b3e6-fa99-4d0f-b4b1-8b0bb05fbc31",
                  "firstName": "Sophie",
                  "lastName": "Lambert",
                  "email": "sophie.lambert@example.com",
                  "age": 28,
                  "gender": "female",
                  "phone": "06 12 34 56 78",
                  "location": {
                    "streetNumber": 12,
                    "streetName": "Avenue Jean Médecin",
                    "city": "Nice",
                    "department": "Alpes-Maritimes",
                    "postcode": "06000",
                    "country": "France"
                  },
                  "country": "France",
                  "city": "Nice",
                  "thumbnailUrl": "https://randomuser.me/api/portraits/thumb/women/44.jpg",
                  "pictureUrl": "https://randomuser.me/api/portraits/women/44.jpg",
                  "source": "randomuser.me",
                  "createdAt": "2026-10-17T15:00:00Z"
                }
              ],
              "pagination": {
                "page": 1,
                "pageSize": 20,
                "totalItems": 50,
                "totalPages": 3
              }
            }
        """.trimIndent()

        val response = json.decodeFromString<PaginatedProfilesResponse>(payload)
        assertEquals(1, response.items.size)
        assertEquals(50L, response.pagination.totalItems)
        assertEquals(50L, response.pagination.totalCount)
        assertEquals("Sophie", response.items.first().firstName)
    }

    @Test
    fun testDeserializeFavoriteItemAndPaginatedFavorites() {
        val favoritePayload = """
            {
              "profile": {
                "id": "e350b3e6-fa99-4d0f-b4b1-8b0bb05fbc31",
                "firstName": "Sophie",
                "lastName": "Lambert",
                "age": 28,
                "gender": "female",
                "location": {
                  "city": "Nice",
                  "country": "France"
                },
                "country": "France",
                "city": "Nice",
                "thumbnailUrl": "https://randomuser.me/thumb.jpg",
                "pictureUrl": "https://randomuser.me/pic.jpg",
                "source": "randomuser.me",
                "createdAt": "2026-10-17T15:00:00Z"
              },
              "favoritedAt": "2026-10-17T15:10:00Z"
            }
        """.trimIndent()

        val fav = json.decodeFromString<FavoriteDto>(favoritePayload)
        assertEquals("Sophie", fav.profile.firstName)
        assertEquals("2026-10-17T15:10:00Z", fav.favoritedAt)

        val paginatedFavPayload = """
            {
              "items": [$favoritePayload],
              "pagination": {
                "page": 1,
                "pageSize": 20,
                "totalItems": 1,
                "totalPages": 1
              }
            }
        """.trimIndent()

        val paginated = json.decodeFromString<PaginatedFavoritesResponse>(paginatedFavPayload)
        assertEquals(1, paginated.items.size)
        assertEquals(1L, paginated.pagination.totalItems)
    }

    @Test
    fun testDeserializeMessageResponse() {
        val logoutPayload = """
            {
              "message": "Déconnexion réussie. Le jeton d'accès peut être supprimé localement."
            }
        """.trimIndent()

        val response = json.decodeFromString<MessageResponse>(logoutPayload)
        assertEquals("Déconnexion réussie. Le jeton d'accès peut être supprimé localement.", response.message)

        val emptyPayload = "{}"
        val emptyResponse = json.decodeFromString<MessageResponse>(emptyPayload)
        assertEquals("", emptyResponse.message)
    }

    @Test
    fun testDeserializeUserPreferences() {
        val payload = """
            {
              "minAge": 20,
              "maxAge": 45,
              "country": "France",
              "updatedAt": "2026-10-17T15:00:00Z"
            }
        """.trimIndent()

        val prefs = json.decodeFromString<PreferencesDto>(payload)
        assertEquals(20, prefs.minAge)
        assertEquals(45, prefs.maxAge)
        assertEquals("France", prefs.country)
    }

    @Test
    fun testDeserializeErrorEnvelope() {
        val payload = """
            {
              "error": {
                "code": "VALIDATION_ERROR",
                "message": "La requête contient des données invalides",
                "details": ["Email invalide"]
              }
            }
        """.trimIndent()

        val error = json.decodeFromString<ErrorResponse>(payload)
        assertEquals("VALIDATION_ERROR", error.error.code)
        assertEquals(1, error.error.details.size)
    }
}
