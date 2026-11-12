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

class SessionDataStore(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_USER_DATA = stringPreferencesKey("user_data")
    }

    val accessTokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_ACCESS_TOKEN]
    }

    val currentUserFlow: Flow<UserDto?> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_DATA]?.let { userJson ->
            try {
                json.decodeFromString<UserDto>(userJson)
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun saveSession(token: String, user: UserDto) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN] = token
            preferences[KEY_USER_DATA] = json.encodeToString(user)
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_ACCESS_TOKEN)
            preferences.remove(KEY_USER_DATA)
        }
    }

    suspend fun getAccessToken(): String? {
        return accessTokenFlow.firstOrNull()
    }
}
