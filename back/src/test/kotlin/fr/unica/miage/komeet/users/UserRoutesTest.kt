package fr.unica.miage.komeet.users

import fr.unica.miage.komeet.TestContext
import fr.unica.miage.komeet.api.dto.AuthResponse
import fr.unica.miage.komeet.api.dto.ErrorEnvelope
import fr.unica.miage.komeet.api.dto.MessageResponse
import fr.unica.miage.komeet.api.dto.RegisterRequest
import fr.unica.miage.komeet.api.dto.UpdateUserRequest
import fr.unica.miage.komeet.api.dto.UserDto
import fr.unica.miage.komeet.sampleProfile
import fr.unica.miage.komeet.setupTestApp
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class UserRoutesTest {

    @Test
    fun `protected routes reject requests without token`() = testApplication {
        val context = TestContext.create()
        val client = setupTestApp(context)

        val response = client.get("/api/v1/users/me")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val error: ErrorEnvelope = response.body()
        assertEquals("UNAUTHORIZED", error.error.code)
    }

    @Test
    fun `protected routes reject invalid token`() = testApplication {
        val context = TestContext.create()
        val client = setupTestApp(context)

        val response = client.get("/api/v1/users/me") {
            header(HttpHeaders.Authorization, "Bearer invalid-or-forged-token")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val error: ErrorEnvelope = response.body()
        assertEquals("UNAUTHORIZED", error.error.code)
    }

    @Test
    fun `get me returns authenticated user profile without password hash`() = testApplication {
        val context = TestContext.create()
        val client = setupTestApp(context)

        val regResponse = client.post("/api/v1/auth/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(RegisterRequest("alice@test.com", "Password123!", "Alice"))
        }
        val auth: AuthResponse = regResponse.body()

        val meResponse = client.get("/api/v1/users/me") {
            header(HttpHeaders.Authorization, "Bearer ${auth.accessToken}")
        }
        assertEquals(HttpStatusCode.OK, meResponse.status)
        val me: UserDto = meResponse.body()
        assertEquals(auth.user.id, me.id)
        assertEquals("alice@test.com", me.email)
        assertEquals("Alice", me.displayName)

        val rawText = meResponse.bodyAsText()
        assertFalse(rawText.contains("passwordHash"))
    }

    @Test
    fun `patch me updates display name`() = testApplication {
        val context = TestContext.create()
        val client = setupTestApp(context)

        val regResponse = client.post("/api/v1/auth/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(RegisterRequest("bob@test.com", "Password123!", "Bob Initial"))
        }
        val auth: AuthResponse = regResponse.body()

        val patchResponse = client.patch("/api/v1/users/me") {
            header(HttpHeaders.Authorization, "Bearer ${auth.accessToken}")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(UpdateUserRequest(displayName = "Bob Updated"))
        }

        assertEquals(HttpStatusCode.OK, patchResponse.status)
        val updated: UserDto = patchResponse.body()
        assertEquals("Bob Updated", updated.displayName)
    }

    @Test
    fun `delete me deletes user and cleans associated data`() = testApplication {
        val context = TestContext.create()
        val client = setupTestApp(context)

        val profile = sampleProfile("p1", "Profile", "One")
        context.profileRepository.saveAll(listOf(profile))

        val regResponse = client.post("/api/v1/auth/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(RegisterRequest("charlie@test.com", "Password123!", "Charlie"))
        }
        val auth: AuthResponse = regResponse.body()

        // Add a favorite for Charlie
        client.post("/api/v1/favorites/p1") {
            header(HttpHeaders.Authorization, "Bearer ${auth.accessToken}")
        }

        // Delete account
        val deleteResponse = client.delete("/api/v1/users/me") {
            header(HttpHeaders.Authorization, "Bearer ${auth.accessToken}")
        }
        assertEquals(HttpStatusCode.OK, deleteResponse.status)
        val msg: MessageResponse = deleteResponse.body()
        assertEquals("Compte utilisateur supprimé avec succès", msg.message)

        // Verify user is gone
        assertNull(context.userRepository.findById(auth.user.id))
        assertEquals(0, context.favoriteRepository.findByUserId(auth.user.id).totalItems)
    }
}
