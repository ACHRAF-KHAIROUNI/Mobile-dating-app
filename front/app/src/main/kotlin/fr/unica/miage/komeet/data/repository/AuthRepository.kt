package fr.unica.miage.komeet.data.repository

import fr.unica.miage.komeet.data.local.SessionDataStore
import fr.unica.miage.komeet.data.remote.KoMeetRemoteSource
import fr.unica.miage.komeet.data.remote.dto.LoginRequest
import fr.unica.miage.komeet.data.remote.dto.RegisterRequest
import fr.unica.miage.komeet.data.remote.dto.UserDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    val currentUser: Flow<UserDto?>

    suspend fun login(email: String, password: String): Result<UserDto>
    suspend fun register(email: String, password: String, displayName: String): Result<UserDto>
    suspend fun logout(): Result<Unit>
}

class AuthRepositoryImpl(
    private val remoteSource: KoMeetRemoteSource,
    private val sessionDataStore: SessionDataStore
) : AuthRepository {

    override val isLoggedIn: Flow<Boolean> = sessionDataStore.accessTokenFlow.map { !it.isNullOrBlank() }
    override val currentUser: Flow<UserDto?> = sessionDataStore.currentUserFlow

    override suspend fun login(email: String, password: String): Result<UserDto> {
        return try {
            val response = remoteSource.login(LoginRequest(email = email.trim(), password = password))
            sessionDataStore.saveSession(response.accessToken, response.user)
            Result.success(response.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String, displayName: String): Result<UserDto> {
        return try {
            val response = remoteSource.register(
                RegisterRequest(
                    email = email.trim(),
                    password = password,
                    displayName = displayName.trim()
                )
            )
            sessionDataStore.saveSession(response.accessToken, response.user)
            Result.success(response.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            // Effacer la session locale immédiatement pour garantir la déconnexion locale
            sessionDataStore.clearSession()
            try {
                remoteSource.logout()
            } catch (_: Exception) {
                // L'appel au serveur est en best-effort pour un JWT sans état
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
