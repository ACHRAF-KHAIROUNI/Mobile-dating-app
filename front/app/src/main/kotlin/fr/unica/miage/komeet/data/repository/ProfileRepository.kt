package fr.unica.miage.komeet.data.repository

import fr.unica.miage.komeet.data.remote.KoMeetRemoteSource
import fr.unica.miage.komeet.data.remote.dto.PaginatedFavoritesResponse
import fr.unica.miage.komeet.data.remote.dto.PaginatedProfilesResponse
import fr.unica.miage.komeet.data.remote.dto.PreferencesDto
import fr.unica.miage.komeet.data.remote.dto.ProfileDto
import fr.unica.miage.komeet.data.remote.dto.UpdatePreferencesRequest

/**
 * Repository gérant les profils, les favoris (Like/Pass façon Tinder) et les préférences.
 * Ce dépôt est pré-câblé pour faciliter la réalisation du TP par les étudiants.
 */
interface ProfileRepository {
    suspend fun getProfiles(
        page: Int = 1,
        pageSize: Int = 20,
        gender: String? = null,
        minAge: Int? = null,
        maxAge: Int? = null,
        department: String? = null,
        country: String? = null,
        search: String? = null,
        sort: String? = null,
        order: String? = null
    ): Result<PaginatedProfilesResponse>

    suspend fun getProfile(id: String): Result<ProfileDto>

    suspend fun getFavorites(page: Int = 1, pageSize: Int = 20): Result<PaginatedFavoritesResponse>

    suspend fun likeProfile(profileId: String): Result<Unit>

    suspend fun removeFavorite(profileId: String): Result<Unit>

    suspend fun getPreferences(): Result<PreferencesDto>

    suspend fun updatePreferences(minAge: Int, maxAge: Int, country: String): Result<PreferencesDto>
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
        country: String
    ): Result<PreferencesDto> {
        return try {
            val updated = remoteSource.updatePreferences(
                UpdatePreferencesRequest(minAge, maxAge, country)
            )
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
