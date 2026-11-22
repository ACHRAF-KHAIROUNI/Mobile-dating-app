package fr.unica.miage.komeet.ui.favorites

import fr.unica.miage.komeet.data.remote.dto.FavoriteDto
import fr.unica.miage.komeet.data.remote.dto.LocationDto
import fr.unica.miage.komeet.data.remote.dto.PaginatedFavoritesResponse
import fr.unica.miage.komeet.data.remote.dto.PaginatedProfilesResponse
import fr.unica.miage.komeet.data.remote.dto.PreferencesDto
import fr.unica.miage.komeet.data.remote.dto.ProfileDto
import fr.unica.miage.komeet.data.repository.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeProfileRepository : ProfileRepository {
        val testProfile = ProfileDto(
            id = "p1",
            firstName = "Sophie",
            lastName = "Marceau",
            age = 28,
            location = LocationDto(city = "Paris", department = "75"),
            thumbnailUrl = "",
            pictureUrl = "",
            createdAt = "2026-01-01"
        )

        var favoritesList = mutableListOf(FavoriteDto(profile = testProfile))
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

        override suspend fun getProfile(id: String): Result<ProfileDto> = Result.success(testProfile)

        override suspend fun getRegions(country: String?): Result<List<fr.unica.miage.komeet.data.remote.dto.RegionDto>> =
            Result.success(emptyList())

        override suspend fun getFavorites(page: Int, pageSize: Int): Result<PaginatedFavoritesResponse> {
            return if (shouldFail) {
                Result.failure(Exception("Erreur réseau"))
            } else {
                Result.success(PaginatedFavoritesResponse(items = favoritesList.toList()))
            }
        }

        override suspend fun likeProfile(profileId: String): Result<Unit> = Result.success(Unit)

        override suspend fun removeFavorite(profileId: String): Result<Unit> {
            return if (shouldFail) {
                Result.failure(Exception("Erreur suppression"))
            } else {
                favoritesList.removeAll { it.profile.id == profileId }
                Result.success(Unit)
            }
        }

        override suspend fun getPreferences(): Result<PreferencesDto> = Result.success(PreferencesDto())

        override suspend fun updatePreferences(minAge: Int, maxAge: Int, country: String, region: String?): Result<PreferencesDto> =
            Result.success(PreferencesDto())
    }

    private lateinit var fakeRepo: FakeProfileRepository
    private lateinit var viewModel: FavoritesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeProfileRepository()
        viewModel = FavoritesViewModel(fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadFavorites_success_updatesState() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.favorites.size)
        assertEquals("p1", state.favorites[0].profile.id)
        assertFalse(state.isLoading)
    }

    @Test
    fun loadFavorites_failure_setsErrorMessage() = runTest {
        fakeRepo.shouldFail = true
        viewModel.loadFavorites()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
        assertFalse(state.isLoading)
    }

    @Test
    fun removeFavorite_success_removesFromList() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.removeFavorite("p1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.favorites.isEmpty())
        assertNotNull(state.actionMessage)
    }
}

