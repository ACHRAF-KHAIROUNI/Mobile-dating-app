package fr.unica.miage.komeet.auth

import fr.unica.miage.komeet.TestContext
import fr.unica.miage.komeet.api.dto.AuthResponse
import fr.unica.miage.komeet.api.dto.ErrorEnvelope
import fr.unica.miage.komeet.api.dto.LoginRequest
import fr.unica.miage.komeet.api.dto.MessageResponse
import fr.unica.miage.komeet.api.dto.RegisterRequest
import fr.unica.miage.komeet.setupTestApp
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AuthRoutesTest {

    @Test
    fun `register successfully creates a user and returns JWT token`() = testApplication {
        val context = TestContext.create()
        val client = setupTestApp(context)

        val response = client.post("/api/v1/auth/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(RegisterRequest("alice@example.com", "Password123!", "Alice Dupont"))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body: AuthResponse = response.body()
        assertNotNull(body.accessToken)
        assertEquals("Bearer", body.tokenType)
        assertEquals("alice@example.com", body.user.email)
        assertEquals("Alice Dupont", body.user.displayName)

        // Ensure passwordHash is never present
        val rawJson = response.bodyAsText()
        assertFalse(rawJson.contains("passwordHash"))
    }

    @Test
    fun `register rejects invalid email format`() = testApplication {
        val context = TestContext.create()
        val client = setupTestApp(context)

        val response = client.post("/api/v1/auth/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(RegisterRequest("invalid-email", "Password123!", "Bob"))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val error: ErrorEnvelope = response.body()
        assertEquals("VALIDATION_ERROR", error.error.code)
    }

    @Test
    fun `register rejects duplicate email`() = testApplication {
        val context = TestContext.create()
        val client = setupTestApp(context)

        val registerBody = RegisterRequest("duplicate@example.com", "Password123!", "Charlie")
        client.post("/api/v1/auth/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(registerBody)
        }

        val secondResponse = client.post("/api/v1/auth/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(registerBody)
        }

        assertEquals(HttpStatusCode.Conflict, secondResponse.status)
        val error: ErrorEnvelope = secondResponse.body()
        assertEquals("EMAIL_ALREADY_EXISTS", error.error.code)
    }

    @Test
    fun `login succeeds with valid credentials`() = testApplication {
        val context = TestContext.create()
        val client = setupTestApp(context)

        client.post("/api/v1/auth/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(RegisterRequest("david@example.com", "SecretPass123", "David"))
        }

        val loginResponse = client.post("/api/v1/auth/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(LoginRequest("david@example.com", "SecretPass123"))
        }

        assertEquals(HttpStatusCode.OK, loginResponse.status)
        val auth: AuthResponse = loginResponse.body()
        assertNotNull(auth.accessToken)
        assertEquals("david@example.com", auth.user.email)
    }

    @Test
    fun `login fails with incorrect password without revealing email existence`() = testApplication {
        val context = TestContext.create()
        val client = setupTestApp(context)

        client.post("/api/v1/auth/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(RegisterRequest("eve@example.com", "RealPassword", "Eve"))
        }

        val wrongPassResponse = client.post("/api/v1/auth/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(LoginRequest("eve@example.com", "WrongPassword"))
        }
        assertEquals(HttpStatusCode.Unauthorized, wrongPassResponse.status)
        val error1: ErrorEnvelope = wrongPassResponse.body()
        assertEquals("INVALID_CREDENTIALS", error1.error.code)

        val nonExistentUserResponse = client.post("/api/v1/auth/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(LoginRequest("nonexistent@example.com", "AnyPass"))
        }
        assertEquals(HttpStatusCode.Unauthorized, nonExistentUserResponse.status)
        val error2: ErrorEnvelope = nonExistentUserResponse.body()
        assertEquals("INVALID_CREDENTIALS", error2.error.code)
    }

    @Test
    fun `logout endpoint responds successfully`() = testApplication {
        val context = TestContext.create()
        val client = setupTestApp(context)

        val response = client.post("/api/v1/auth/logout")
        assertEquals(HttpStatusCode.OK, response.status)
        val body: MessageResponse = response.body()
        assertTrue(body.message.isNotBlank())
    }
}
