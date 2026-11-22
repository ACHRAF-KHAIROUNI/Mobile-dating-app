package fr.unica.miage.komeet.ui.profile

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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeProfileRepository : ProfileRepository {
        val testProfile = ProfileDto(
            id = "p1",
            firstName = "Camille",
            lastName = "Cottin",
            age = 35,
            location = LocationDto(city = "Lyon", department = "69"),
            thumbnailUrl = "",
            pictureUrl = "",
            createdAt = "2026-01-01"
        )
        val favoritesList = mutableListOf<FavoriteDto>()
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

        override suspend fun getProfile(id: String): Result<ProfileDto> {
            return if (shouldFail) {
                Result.failure(Exception("Profil introuvable"))
            } else {
                Result.success(testProfile)
            }
        }

        override suspend fun getRegions(country: String?): Result<List<fr.unica.miage.komeet.data.remote.dto.RegionDto>> =
            Result.success(emptyList())

        override suspend fun getFavorites(page: Int, pageSize: Int): Result<PaginatedFavoritesResponse> =
            Result.success(PaginatedFavoritesResponse(items = favoritesList))

        override suspend fun likeProfile(profileId: String): Result<Unit> {
            favoritesList.add(FavoriteDto(profile = testProfile))
            return Result.success(Unit)
        }

        override suspend fun removeFavorite(profileId: String): Result<Unit> {
            favoritesList.removeAll { it.profile.id == profileId }
            return Result.success(Unit)
        }

        override suspend fun getPreferences(): Result<PreferencesDto> = Result.success(PreferencesDto())

        override suspend fun updatePreferences(minAge: Int, maxAge: Int, country: String, region: String?): Result<PreferencesDto> =
            Result.success(PreferencesDto())
    }

    private lateinit var fakeRepo: FakeProfileRepository
    private lateinit var viewModel: ProfileDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeProfileRepository()
        viewModel = ProfileDetailViewModel(fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadProfile_success_updatesState() = runTest {
        viewModel.loadProfile("p1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("p1", state.profile?.id)
        assertEquals("Camille", state.profile?.firstName)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun toggleFavorite_addsAndRemovesFavorite() = runTest {
        viewModel.loadProfile("p1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLiked)

        // Add
        viewModel.toggleFavorite()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isLiked)

        // Remove
        viewModel.toggleFavorite()
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLiked)
    }
}

