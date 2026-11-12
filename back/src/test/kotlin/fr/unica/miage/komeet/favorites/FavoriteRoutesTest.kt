package fr.unica.miage.komeet.favorites

import fr.unica.miage.komeet.TestContext
import fr.unica.miage.komeet.api.dto.AuthResponse
import fr.unica.miage.komeet.api.dto.ErrorEnvelope
import fr.unica.miage.komeet.api.dto.FavoriteDto
import fr.unica.miage.komeet.api.dto.MessageResponse
import fr.unica.miage.komeet.api.dto.PageResponse
import fr.unica.miage.komeet.api.dto.RegisterRequest
import fr.unica.miage.komeet.sampleProfile
import fr.unica.miage.komeet.setupTestApp
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FavoriteRoutesTest {

    @Test
    fun `add favorite creates favorite and repeated call does not duplicate`() = testApplication {
        val context = TestContext.create()
        val profile = sampleProfile("prof-1", "Marie", "Curie")
        context.profileRepository.saveAll(listOf(profile))
        val client = setupTestApp(context)

        val reg = client.post("/api/v1/auth/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(RegisterRequest("user1@example.com", "Password123", "User One"))
        }
        val token = reg.body<AuthResponse>().accessToken

        // First add
        val addRes1 = client.post("/api/v1/favorites/prof-1") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.Created, addRes1.status)
        val fav1: FavoriteDto = addRes1.body()
        assertEquals("prof-1", fav1.profile.id)

        // Second add (idempotent, should not duplicate)
        val addRes2 = client.post("/api/v1/favorites/prof-1") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.Created, addRes2.status)

        // Verify count is still 1
        val getRes = client.get("/api/v1/favorites") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, getRes.status)
        val list: PageResponse<FavoriteDto> = getRes.body()
        assertEquals(1, list.items.size)
        assertEquals(1L, list.pagination.totalItems)
    }

    @Test
    fun `delete favorite removes favorite and second delete returns 404`() = testApplication {
        val context = TestContext.create()
        val profile = sampleProfile("prof-2", "Albert", "Einstein")
        context.profileRepository.saveAll(listOf(profile))
        val client = setupTestApp(context)

        val reg = client.post("/api/v1/auth/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(RegisterRequest("user2@example.com", "Password123", "User Two"))
        }
        val token = reg.body<AuthResponse>().accessToken

        client.post("/api/v1/favorites/prof-2") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        // Delete
        val delRes = client.delete("/api/v1/favorites/prof-2") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, delRes.status)
        val msg: MessageResponse = delRes.body()
        assertTrue(msg.message.isNotBlank())

        // Delete again should return 404 FAVORITE_NOT_FOUND
        val delAgainRes = client.delete("/api/v1/favorites/prof-2") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.NotFound, delAgainRes.status)
        val error: ErrorEnvelope = delAgainRes.body()
        assertEquals("FAVORITE_NOT_FOUND", error.error.code)
    }

    @Test
    fun `add favorite for non-existent profile returns 404`() = testApplication {
        val context = TestContext.create()
        val client = setupTestApp(context)

        val reg = client.post("/api/v1/auth/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(RegisterRequest("user3@example.com", "Password123", "User Three"))
        }
        val token = reg.body<AuthResponse>().accessToken

        val res = client.post("/api/v1/favorites/non-existent-profile") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.NotFound, res.status)
        val error: ErrorEnvelope = res.body()
        assertEquals("PROFILE_NOT_FOUND", error.error.code)
    }

    @Test
    fun `strict isolation of favorites between two distinct users`() = testApplication {
        val context = TestContext.create()
        val p1 = sampleProfile("p1", "Profile", "One")
        val p2 = sampleProfile("p2", "Profile", "Two")
        context.profileRepository.saveAll(listOf(p1, p2))
        val client = setupTestApp(context)

        val reg1 = client.post("/api/v1/auth/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(RegisterRequest("userA@example.com", "Password123", "User A"))
        }
        val tokenA = reg1.body<AuthResponse>().accessToken

        val reg2 = client.post("/api/v1/auth/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(RegisterRequest("userB@example.com", "Password123", "User B"))
        }
        val tokenB = reg2.body<AuthResponse>().accessToken

        // User A adds p1
        client.post("/api/v1/favorites/p1") {
            header(HttpHeaders.Authorization, "Bearer $tokenA")
        }

        // User B adds p2
        client.post("/api/v1/favorites/p2") {
            header(HttpHeaders.Authorization, "Bearer $tokenB")
        }

        // User A checks favorites
        val favsA: PageResponse<FavoriteDto> = client.get("/api/v1/favorites") {
            header(HttpHeaders.Authorization, "Bearer $tokenA")
        }.body()
        assertEquals(1, favsA.items.size)
        assertEquals("p1", favsA.items.first().profile.id)

        // User B checks favorites
        val favsB: PageResponse<FavoriteDto> = client.get("/api/v1/favorites") {
            header(HttpHeaders.Authorization, "Bearer $tokenB")
        }.body()
        assertEquals(1, favsB.items.size)
        assertEquals("p2", favsB.items.first().profile.id)

        // User A cannot delete User B's favorite
        val delOtherRes = client.delete("/api/v1/favorites/p2") {
            header(HttpHeaders.Authorization, "Bearer $tokenA")
        }
        assertEquals(HttpStatusCode.NotFound, delOtherRes.status)

        // User B still has p2
        val favsBAfter: PageResponse<FavoriteDto> = client.get("/api/v1/favorites") {
            header(HttpHeaders.Authorization, "Bearer $tokenB")
        }.body()
        assertEquals(1, favsBAfter.items.size)
    }
}
