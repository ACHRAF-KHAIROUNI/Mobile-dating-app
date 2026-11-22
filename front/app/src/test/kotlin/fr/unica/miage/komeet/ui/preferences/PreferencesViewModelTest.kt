package fr.unica.miage.komeet.ui.preferences

import fr.unica.miage.komeet.data.local.SessionStorage
import fr.unica.miage.komeet.data.remote.dto.PaginatedFavoritesResponse
import fr.unica.miage.komeet.data.remote.dto.PaginatedProfilesResponse
import fr.unica.miage.komeet.data.remote.dto.PreferencesDto
import fr.unica.miage.komeet.data.remote.dto.ProfileDto
import fr.unica.miage.komeet.data.remote.dto.UserDto
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
class PreferencesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

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
        var prefs = PreferencesDto(minAge = 22, maxAge = 40, country = "France", region = null)
        var shouldFail = false

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
        ): Result<PaginatedProfilesResponse> = Result.success(PaginatedProfilesResponse())

        override suspend fun getProfile(id: String): Result<ProfileDto> =
            Result.failure(Exception("Not implemented"))

        override suspend fun getRegions(country: String?): Result<List<fr.unica.miage.komeet.data.remote.dto.RegionDto>> =
            Result.success(emptyList())

        override suspend fun getFavorites(page: Int, pageSize: Int): Result<PaginatedFavoritesResponse> =
            Result.success(PaginatedFavoritesResponse())

        override suspend fun likeProfile(profileId: String): Result<Unit> = Result.success(Unit)

        override suspend fun removeFavorite(profileId: String): Result<Unit> = Result.success(Unit)

        override suspend fun getPreferences(): Result<PreferencesDto> {
            return if (shouldFail) {
                Result.failure(Exception("Erreur chargement"))
            } else {
                Result.success(prefs)
            }
        }

        override suspend fun updatePreferences(minAge: Int, maxAge: Int, country: String, region: String?): Result<PreferencesDto> {
            return if (shouldFail) {
                Result.failure(Exception("Erreur sauvegarde"))
            } else {
                prefs = PreferencesDto(minAge = minAge, maxAge = maxAge, country = country, region = region)
                Result.success(prefs)
            }
        }
    }

    private lateinit var fakeRepo: FakeProfileRepository
    private lateinit var fakeSessionStorage: FakeSessionStorage
    private lateinit var viewModel: PreferencesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeProfileRepository()
        fakeSessionStorage = FakeSessionStorage()
        viewModel = PreferencesViewModel(fakeRepo, fakeSessionStorage)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadPreferences_success_updatesState() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(22, state.minAge)
        assertEquals(40, state.maxAge)
        assertEquals("France", state.country)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun savePreferences_success_updatesRepositoryAndState() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAgeRangeChange(25, 45)
        viewModel.onCountryChange("Canada")
        viewModel.onGenderChange("female")
        viewModel.onDepartmentChange("06")
        viewModel.savePreferences()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(25, fakeRepo.prefs.minAge)
        assertEquals(45, fakeRepo.prefs.maxAge)
        assertEquals("Canada", fakeRepo.prefs.country)
        assertEquals("female", fakeSessionStorage.gender)
        assertEquals("06", fakeSessionStorage.department)
        assertNotNull(state.successMessage)
        assertFalse(state.isSaving)
    }

    @Test
    fun savePreferences_failure_setsErrorMessage() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        fakeRepo.shouldFail = true
        viewModel.savePreferences()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
        assertFalse(state.isSaving)
    }

    @Test
    fun logout_clearsSessionAndInvokesCallback() = runTest {
        var callbackInvoked = false
        viewModel.logout { callbackInvoked = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(callbackInvoked)
        assertNull(fakeSessionStorage.token)
    }
}
