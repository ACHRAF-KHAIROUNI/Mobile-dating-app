package fr.unica.miage.komeet.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import fr.unica.miage.komeet.data.remote.dto.UserDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "komeet_session")

interface SessionStorage {
    val accessTokenFlow: Flow<String?>
    val currentUserFlow: Flow<UserDto?>
    val filterGenderFlow: Flow<String?>
    val filterDepartmentFlow: Flow<String?>
    val filterRegionFlow: Flow<String?>
    val filterCountryFlow: Flow<String?>

    suspend fun saveSession(token: String, user: UserDto)
    suspend fun clearSession()
    suspend fun saveFilterPreferences(
        gender: String?,
        department: String?,
        region: String? = null,
        country: String? = null
    )
    suspend fun getAccessToken(): String?
    suspend fun getFilterGender(): String?
    suspend fun getFilterDepartment(): String?
    suspend fun getFilterRegion(): String?
    suspend fun getFilterCountry(): String?
}

class SessionDataStore(private val context: Context) : SessionStorage {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_USER_DATA = stringPreferencesKey("user_data")
        private val KEY_FILTER_GENDER = stringPreferencesKey("filter_gender")
        private val KEY_FILTER_DEPARTMENT = stringPreferencesKey("filter_department")
        private val KEY_FILTER_REGION = stringPreferencesKey("filter_region")
        private val KEY_FILTER_COUNTRY = stringPreferencesKey("filter_country")
    }

    override val accessTokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_ACCESS_TOKEN]
    }

    override val currentUserFlow: Flow<UserDto?> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_DATA]?.let { userJson ->
            try {
                json.decodeFromString<UserDto>(userJson)
            } catch (e: Exception) {
                null
            }
        }
    }

    override val filterGenderFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_FILTER_GENDER]
    }

    override val filterDepartmentFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_FILTER_DEPARTMENT]
    }

    override val filterRegionFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_FILTER_REGION]
    }

    override val filterCountryFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_FILTER_COUNTRY]
    }

    override suspend fun saveSession(token: String, user: UserDto) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN] = token
            preferences[KEY_USER_DATA] = json.encodeToString(user)
        }
    }

    override suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_ACCESS_TOKEN)
            preferences.remove(KEY_USER_DATA)
        }
    }

    override suspend fun saveFilterPreferences(
        gender: String?,
        department: String?,
        region: String?,
        country: String?
    ) {
        context.dataStore.edit { preferences ->
            if (!gender.isNullOrBlank()) {
                preferences[KEY_FILTER_GENDER] = gender
            } else {
                preferences.remove(KEY_FILTER_GENDER)
            }
            if (!department.isNullOrBlank()) {
                preferences[KEY_FILTER_DEPARTMENT] = department
            } else {
                preferences.remove(KEY_FILTER_DEPARTMENT)
            }
            if (!region.isNullOrBlank()) {
                preferences[KEY_FILTER_REGION] = region
            } else {
                preferences.remove(KEY_FILTER_REGION)
            }
            if (!country.isNullOrBlank()) {
                preferences[KEY_FILTER_COUNTRY] = country
            } else {
                preferences.remove(KEY_FILTER_COUNTRY)
            }
        }
    }

    override suspend fun getAccessToken(): String? {
        return accessTokenFlow.firstOrNull()
    }

    override suspend fun getFilterGender(): String? {
        return filterGenderFlow.firstOrNull()
    }

    override suspend fun getFilterDepartment(): String? {
        return filterDepartmentFlow.firstOrNull()
    }

    override suspend fun getFilterRegion(): String? {
        return filterRegionFlow.firstOrNull()
    }

    override suspend fun getFilterCountry(): String? {
        return filterCountryFlow.firstOrNull()
    }
}
