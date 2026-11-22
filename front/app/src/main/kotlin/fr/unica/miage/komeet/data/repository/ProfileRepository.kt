package fr.unica.miage.komeet.data.repository

import fr.unica.miage.komeet.data.remote.KoMeetRemoteSource
import fr.unica.miage.komeet.data.remote.dto.PaginatedFavoritesResponse
import fr.unica.miage.komeet.data.remote.dto.PaginatedProfilesResponse
import fr.unica.miage.komeet.data.remote.dto.PreferencesDto
import fr.unica.miage.komeet.data.remote.dto.ProfileDto
import fr.unica.miage.komeet.data.remote.dto.RegionDto
import fr.unica.miage.komeet.data.remote.dto.UpdatePreferencesRequest

interface ProfileRepository {
    suspend fun getProfiles(
        page: Int = 1,
        pageSize: Int = 20,
        gender: String? = null,
        minAge: Int? = null,
        maxAge: Int? = null,
        department: String? = null,
        country: String? = null,
        region: String? = null,
        search: String? = null,
        sort: String? = null,
        order: String? = null
    ): Result<PaginatedProfilesResponse>

    suspend fun getProfile(id: String): Result<ProfileDto>
    suspend fun getRegions(country: String? = null): Result<List<RegionDto>>
    suspend fun getFavorites(page: Int = 1, pageSize: Int = 20): Result<PaginatedFavoritesResponse>
    suspend fun likeProfile(profileId: String): Result<Unit>
    suspend fun removeFavorite(profileId: String): Result<Unit>
    suspend fun getPreferences(): Result<PreferencesDto>
    suspend fun updatePreferences(minAge: Int, maxAge: Int, country: String, region: String? = null): Result<PreferencesDto>
}

class ProfileRepositoryImpl(
    private val remoteSource: KoMeetRemoteSource
) : ProfileRepository {

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
        return try {
            val response = remoteSource.getProfiles(
                page = page,
                pageSize = pageSize,
                gender = gender,
                minAge = minAge,
                maxAge = maxAge,
                department = department,
                country = country,
                region = region,
                search = search,
                sort = sort,
                order = order
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProfile(id: String): Result<ProfileDto> {
        return try {
            val profile = remoteSource.getProfileById(id)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRegions(country: String?): Result<List<RegionDto>> {
        return try {
            val regions = remoteSource.getRegions(country)
            Result.success(regions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFavorites(page: Int, pageSize: Int): Result<PaginatedFavoritesResponse> {
        return try {
            val response = remoteSource.getFavorites(page, pageSize)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun likeProfile(profileId: String): Result<Unit> {
        return try {
            remoteSource.addFavorite(profileId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFavorite(profileId: String): Result<Unit> {
        return try {
            remoteSource.removeFavorite(profileId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPreferences(): Result<PreferencesDto> {
        return try {
            val prefs = remoteSource.getPreferences()
            Result.success(prefs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePreferences(
        minAge: Int,
        maxAge: Int,
        country: String,
        region: String?
    ): Result<PreferencesDto> {
        return try {
            val updated = remoteSource.updatePreferences(
                UpdatePreferencesRequest(
                    minAge = minAge,
                    maxAge = maxAge,
                    country = country,
                    region = region
                )
            )
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
