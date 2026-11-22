package fr.unica.miage.komeet.ui.discovery

import fr.unica.miage.komeet.data.local.SessionStorage
import fr.unica.miage.komeet.data.remote.dto.LocationDto
import fr.unica.miage.komeet.data.remote.dto.PaginatedFavoritesResponse
import fr.unica.miage.komeet.data.remote.dto.PaginatedProfilesResponse
import fr.unica.miage.komeet.data.remote.dto.PaginationDto
import fr.unica.miage.komeet.data.remote.dto.PreferencesDto
import fr.unica.miage.komeet.data.remote.dto.ProfileDto
import fr.unica.miage.komeet.data.remote.dto.UserDto
import fr.unica.miage.komeet.data.repository.AuthRepository
import fr.unica.miage.komeet.data.repository.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiscoveryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeAuthRepository : AuthRepository {
        override val isLoggedIn: Flow<Boolean> = flowOf(true)
        override val currentUser: Flow<UserDto?> = flowOf(null)
        var logoutCalled = false

        override suspend fun login(email: String, password: String): Result<UserDto> =
            Result.success(UserDto("1", email, "User"))

        override suspend fun register(email: String, password: String, displayName: String): Result<UserDto> =
            Result.success(UserDto("1", email, displayName))

        override suspend fun logout(): Result<Unit> {
            logoutCalled = true
            return Result.success(Unit)
        }
    }

    private class FakeSessionStorage : SessionStorage {
        var token: String? = "fake_token"
        var gender: String? = null
        var department: String? = null
        var region: String? = null
        var country: String? = null

        override val accessTokenFlow: Flow<String?> = flowOf(token)
        override val currentUserFlow: Flow<UserDto?> = flowOf(null)
        override val filterGenderFlow: Flow<String?> = flowOf(gender)
        override val filterDepartmentFlow: Flow<String?> = flowOf(department)
        override val filterRegionFlow: Flow<String?> = flowOf(region)
        override val filterCountryFlow: Flow<String?> = flowOf(country)

        override suspend fun saveSession(token: String, user: UserDto) { this.token = token }
        override suspend fun clearSession() { this.token = null }
        override suspend fun saveFilterPreferences(
            gender: String?,
            department: String?,
            region: String?,
            country: String?
        ) {
            this.gender = gender
            this.department = department
            this.region = region
            this.country = country
        }
        override suspend fun getAccessToken(): String? = token
        override suspend fun getFilterGender(): String? = gender
        override suspend fun getFilterDepartment(): String? = department
        override suspend fun getFilterRegion(): String? = region
        override suspend fun getFilterCountry(): String? = country
    }

    private class FakeProfileRepository : ProfileRepository {
        var profiles = listOf(
            ProfileDto(
                id = "p1",
                firstName = "Amélie",
                lastName = "Poulain",
                age = 25,
                gender = "female",
                location = LocationDto(city = "Paris", department = "75", region = "Île-de-France", country = "France"),
                thumbnailUrl = "",
                pictureUrl = "",
                createdAt = "2026-01-01"
            ),
            ProfileDto(
                id = "p2",
                firstName = "Jean",
                lastName = "Dujardin",
                age = 30,
                gender = "male",
                location = LocationDto(city = "Nice", department = "06", region = "Provence-Alpes-Côte d'Azur", country = "France"),
                thumbnailUrl = "",
                pictureUrl = "",
                createdAt = "2026-01-01"
            )
        )
        val likedProfiles = mutableListOf<String>()

        override suspend fun getProfiles(
            page: Int,
            pageSize: Int,
            gender: String?,
            minAge: Int?,
            maxAge: Int?,
            department: String?,
            country: String?,
            region: String?,
            search: String?,
            sort: String?,
            order: String?
        ): Result<PaginatedProfilesResponse> {
            val filtered = profiles.filter { p ->
                (gender == null || p.gender == gender) &&
                (department == null || p.location.department == department) &&
                (region == null || p.location.region.equals(region, ignoreCase = true)) &&
                (country == null || p.location.country.equals(country, ignoreCase = true))
            }
            return Result.success(
                PaginatedProfilesResponse(
                    items = filtered,
                    pagination = PaginationDto(page = 1, pageSize = pageSize, totalItems = filtered.size.toLong())
                )
            )
        }

        override suspend fun getProfile(id: String): Result<ProfileDto> {
            val found = profiles.find { it.id == id }
            return if (found != null) Result.success(found) else Result.failure(Exception("Not found"))
        }

        override suspend fun getRegions(country: String?): Result<List<fr.unica.miage.komeet.data.remote.dto.RegionDto>> =
            Result.success(emptyList())

        override suspend fun getFavorites(page: Int, pageSize: Int): Result<PaginatedFavoritesResponse> =
            Result.success(PaginatedFavoritesResponse())

        override suspend fun likeProfile(profileId: String): Result<Unit> {
            likedProfiles.add(profileId)
            return Result.success(Unit)
        }

        override suspend fun removeFavorite(profileId: String): Result<Unit> {
            likedProfiles.remove(profileId)
            return Result.success(Unit)
        }

        override suspend fun getPreferences(): Result<PreferencesDto> =
            Result.success(PreferencesDto(minAge = 18, maxAge = 50, country = "France"))

        override suspend fun updatePreferences(minAge: Int, maxAge: Int, country: String, region: String?): Result<PreferencesDto> =
            Result.success(PreferencesDto(minAge = minAge, maxAge = maxAge, country = country, region = region))
    }

    private lateinit var fakeProfileRepo: FakeProfileRepository
    private lateinit var fakeAuthRepo: FakeAuthRepository
    private lateinit var fakeSessionStorage: FakeSessionStorage
    private lateinit var viewModel: DiscoveryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeProfileRepo = FakeProfileRepository()
        fakeAuthRepo = FakeAuthRepository()
        fakeSessionStorage = FakeSessionStorage()
        viewModel = DiscoveryViewModel(fakeProfileRepo, fakeAuthRepo, fakeSessionStorage)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadProfiles_populatesProfilesList() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.profiles.size)
        assertTrue(state.profiles.map { it.id }.containsAll(listOf("p1", "p2")))
        assertNotNull(state.currentProfile)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun setGenderFilter_filtersProfiles() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setGenderFilter("female")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("female", state.selectedGender)
        assertEquals(1, state.profiles.size)
        assertEquals("p1", state.profiles[0].id)
    }

    @Test
    fun passCurrentProfile_advancesToNext() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val expectedNext = viewModel.uiState.value.nextProfile
        viewModel.passCurrentProfile()

        val state = viewModel.uiState.value
        assertEquals(1, state.currentIndex)
        assertEquals(expectedNext?.id, state.currentProfile?.id)
        assertNull(state.nextProfile)
    }

    @Test
    fun likeCurrentProfile_callsRepositoryAndAdvances() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val currentId = viewModel.uiState.value.currentProfile?.id
        viewModel.likeCurrentProfile()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.currentIndex)
        assertTrue(fakeProfileRepo.likedProfiles.contains(currentId))
        assertNotNull(state.lastActionMessage)
    }

    @Test
    fun logout_callsAuthRepository() = runTest {
        var callbackCalled = false
        viewModel.logout { callbackCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeAuthRepo.logoutCalled)
        assertTrue(callbackCalled)
    }
}
