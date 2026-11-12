package fr.unica.miage.komeet.preferences

import fr.unica.miage.komeet.TestContext
import fr.unica.miage.komeet.api.dto.AuthResponse
import fr.unica.miage.komeet.api.dto.ErrorEnvelope
import fr.unica.miage.komeet.api.dto.RegisterRequest
import fr.unica.miage.komeet.api.dto.UpdatePreferencesRequest
import fr.unica.miage.komeet.api.dto.UserPreferencesDto
import fr.unica.miage.komeet.setupTestApp
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class PreferencesRoutesTest {

    @Test
    fun `get preferences returns documented defaults when none saved yet`() = testApplication {
        val context = TestContext.create()
        val client = setupTestApp(context)

        val reg = client.post("/api/v1/auth/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(RegisterRequest("pref1@example.com", "Password123", "Pref User 1"))
        }
        val token = reg.body<AuthResponse>().accessToken

        val getRes = client.get("/api/v1/preferences") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, getRes.status)
        val prefs: UserPreferencesDto = getRes.body()
        assertEquals(18, prefs.minAge)
        assertEquals(99, prefs.maxAge)
        assertNull(prefs.country)
    }

    @Test
    fun `put preferences updates user preferences`() = testApplication {
        val context = TestContext.create()
        val client = setupTestApp(context)

        val reg = client.post("/api/v1/auth/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(RegisterRequest("pref2@example.com", "Password123", "Pref User 2"))
        }
        val token = reg.body<AuthResponse>().accessToken

        val putRes = client.put("/api/v1/preferences") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(UpdatePreferencesRequest(minAge = 25, maxAge = 40, country = "France"))
        }
        assertEquals(HttpStatusCode.OK, putRes.status)
        val updated: UserPreferencesDto = putRes.body()
        assertEquals(25, updated.minAge)
        assertEquals(40, updated.maxAge)
        assertEquals("France", updated.country)

        // Verify subsequent GET
        val getRes = client.get("/api/v1/preferences") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        val fetched: UserPreferencesDto = getRes.body()
        assertEquals(25, fetched.minAge)
        assertEquals("France", fetched.country)
    }

    @Test
    fun `put preferences rejects invalid age values`() = testApplication {
        val context = TestContext.create()
        val client = setupTestApp(context)

        val reg = client.post("/api/v1/auth/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(RegisterRequest("pref3@example.com", "Password123", "Pref User 3"))
        }
        val token = reg.body<AuthResponse>().accessToken

        val badRes = client.put("/api/v1/preferences") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(UpdatePreferencesRequest(minAge = 50, maxAge = 20))
        }
        assertEquals(HttpStatusCode.BadRequest, badRes.status)
        val error: ErrorEnvelope = badRes.body()
        assertEquals("VALIDATION_ERROR", error.error.code)
    }

    @Test
    fun `strict isolation of preferences between two distinct users`() = testApplication {
        val context = TestContext.create()
        val client = setupTestApp(context)

        val regA = client.post("/api/v1/auth/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(RegisterRequest("userA@pref.com", "Password123", "User A"))
        }
        val tokenA = regA.body<AuthResponse>().accessToken

        val regB = client.post("/api/v1/auth/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(RegisterRequest("userB@pref.com", "Password123", "User B"))
        }
        val tokenB = regB.body<AuthResponse>().accessToken

        // User A updates preferences
        client.put("/api/v1/preferences") {
            header(HttpHeaders.Authorization, "Bearer $tokenA")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(UpdatePreferencesRequest(minAge = 21, maxAge = 35, country = "Spain"))
        }

        // User B preferences remain default
        val prefsB: UserPreferencesDto = client.get("/api/v1/preferences") {
            header(HttpHeaders.Authorization, "Bearer $tokenB")
        }.body()
        assertEquals(18, prefsB.minAge)
        assertEquals(99, prefsB.maxAge)
        assertNull(prefsB.country)
    }
}
