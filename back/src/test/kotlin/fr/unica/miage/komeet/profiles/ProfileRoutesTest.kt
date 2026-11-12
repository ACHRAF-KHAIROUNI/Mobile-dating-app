package fr.unica.miage.komeet.profiles

import fr.unica.miage.komeet.TestContext
import fr.unica.miage.komeet.api.dto.ErrorEnvelope
import fr.unica.miage.komeet.api.dto.PageResponse
import fr.unica.miage.komeet.api.dto.ProfileDto
import fr.unica.miage.komeet.sampleProfile
import fr.unica.miage.komeet.setupTestApp
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ProfileRoutesTest {

    @Test
    fun `get profiles returns empty list when database is empty`() = testApplication {
        val context = TestContext.create()
        val client = setupTestApp(context)

        val response = client.get("/api/v1/profiles")
        assertEquals(HttpStatusCode.OK, response.status)
        val body: PageResponse<ProfileDto> = response.body()
        assertEquals(0, body.items.size)
        assertEquals(0L, body.pagination.totalItems)
        assertEquals(1, body.pagination.page)
    }

    @Test
    fun `get profiles returns paginated items when populated`() = testApplication {
        val context = TestContext.create()
        val profiles = (1..25).map { i ->
            sampleProfile(
                id = "p-$i",
                firstName = "User$i",
                lastName = "Nom$i",
                age = 20 + i,
                country = if (i % 2 == 0) "France" else "Canada"
            )
        }
        context.profileRepository.saveAll(profiles)
        val client = setupTestApp(context)

        // Page 1
        val resPage1 = client.get("/api/v1/profiles?page=1&pageSize=10")
        assertEquals(HttpStatusCode.OK, resPage1.status)
        val page1: PageResponse<ProfileDto> = resPage1.body()
        assertEquals(10, page1.items.size)
        assertEquals(25L, page1.pagination.totalItems)
        assertEquals(3, page1.pagination.totalPages)

        // Page 3
        val resPage3 = client.get("/api/v1/profiles?page=3&pageSize=10")
        assertEquals(HttpStatusCode.OK, resPage3.status)
        val page3: PageResponse<ProfileDto> = resPage3.body()
        assertEquals(5, page3.items.size)
    }

    @Test
    fun `get profiles filters by search, age range, country, gender, and sort`() = testApplication {
        val context = TestContext.create()
        val profiles = listOf(
            sampleProfile("1", "Alice", "Durand", age = 22, gender = "female", country = "France", city = "Paris"),
            sampleProfile("2", "Bob", "Martin", age = 35, gender = "male", country = "France", city = "Lyon"),
            sampleProfile("3", "Charlie", "Smith", age = 45, gender = "male", country = "Canada", city = "Montreal"),
            sampleProfile("4", "David", "Durand", age = 28, gender = "male", country = "Belgium", city = "Bruxelles")
        )
        context.profileRepository.saveAll(profiles)
        val client = setupTestApp(context)

        // Filter by country France
        val franceRes = client.get("/api/v1/profiles?country=France")
        val francePage: PageResponse<ProfileDto> = franceRes.body()
        assertEquals(2, francePage.items.size)
        assertTrue(francePage.items.all { it.country == "France" })

        // Filter by gender female
        val femaleRes = client.get("/api/v1/profiles?gender=female")
        val femalePage: PageResponse<ProfileDto> = femaleRes.body()
        assertEquals(1, femalePage.items.size)
        assertEquals("Alice", femalePage.items.first().firstName)
        assertEquals("female", femalePage.items.first().gender)

        // Filter by age range
        val ageRes = client.get("/api/v1/profiles?minAge=25&maxAge=40")
        val agePage: PageResponse<ProfileDto> = ageRes.body()
        assertEquals(2, agePage.items.size)
        assertTrue(agePage.items.all { it.age in 25..40 })

        // Search text
        val searchRes = client.get("/api/v1/profiles?search=Durand")
        val searchPage: PageResponse<ProfileDto> = searchRes.body()
        assertEquals(2, searchPage.items.size)

        // Sort by age desc
        val sortRes = client.get("/api/v1/profiles?sort=age&order=desc")
        val sortPage: PageResponse<ProfileDto> = sortRes.body()
        assertEquals(45, sortPage.items.first().age)
    }

    @Test
    fun `get profile by id returns profile with gender when exists and 404 when not found`() = testApplication {
        val context = TestContext.create()
        val profile = sampleProfile("prof-123", "Sophie", "Lambert", gender = "female")
        context.profileRepository.saveAll(listOf(profile))
        val client = setupTestApp(context)

        val successRes = client.get("/api/v1/profiles/prof-123")
        assertEquals(HttpStatusCode.OK, successRes.status)
        val fetched: ProfileDto = successRes.body()
        assertEquals("prof-123", fetched.id)
        assertEquals("Sophie", fetched.firstName)
        assertEquals("female", fetched.gender)
        assertEquals("06 12 34 56 78", fetched.phone)
        assertEquals(12, fetched.location.streetNumber)
        assertEquals("Avenue Jean Médecin", fetched.location.streetName)
        assertEquals("Nice", fetched.location.city)
        assertEquals("Alpes-Maritimes", fetched.location.department)
        assertEquals("06000", fetched.location.postcode)
        assertEquals("France", fetched.location.country)

        val notFoundRes = client.get("/api/v1/profiles/non-existent-id")
        assertEquals(HttpStatusCode.NotFound, notFoundRes.status)
        val error: ErrorEnvelope = notFoundRes.body()
        assertEquals("PROFILE_NOT_FOUND", error.error.code)
    }

    @Test
    fun `get profiles rejects invalid query parameters`() = testApplication {
        val context = TestContext.create()
        val client = setupTestApp(context)

        // minAge > maxAge
        val badRangeRes = client.get("/api/v1/profiles?minAge=50&maxAge=20")
        assertEquals(HttpStatusCode.BadRequest, badRangeRes.status)
        val err1: ErrorEnvelope = badRangeRes.body()
        assertEquals("VALIDATION_ERROR", err1.error.code)

        // invalid sort
        val badSortRes = client.get("/api/v1/profiles?sort=invalid_column")
        assertEquals(HttpStatusCode.BadRequest, badSortRes.status)
        val err2: ErrorEnvelope = badSortRes.body()
        assertEquals("VALIDATION_ERROR", err2.error.code)
    }
}
