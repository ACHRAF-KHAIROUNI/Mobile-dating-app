package fr.unica.miage.komeet.ui.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.unica.miage.komeet.data.remote.dto.ProfileDto
import fr.unica.miage.komeet.data.repository.AuthRepository
import fr.unica.miage.komeet.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DiscoveryUiState(
    val profiles: List<ProfileDto> = emptyList(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lastActionMessage: String? = null
) {
    val currentProfile: ProfileDto?
        get() = if (currentIndex in profiles.indices) profiles[currentIndex] else null
}

class DiscoveryViewModel(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoveryUiState())
    val uiState: StateFlow<DiscoveryUiState> = _uiState.asStateFlow()

    init {
        loadProfiles()
    }

    fun loadProfiles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = profileRepository.getProfiles(page = 1, pageSize = 30)
            result.fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            profiles = response.items,
                            currentIndex = 0,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Impossible de charger les profils"
                        )
                    }
                }
            )
        }
    }

    fun likeCurrentProfile() {
        val profile = _uiState.value.currentProfile ?: return
        viewModelScope.launch {
            val result = profileRepository.likeProfile(profile.id)
            result.fold(
                onSuccess = {
                    advanceToNextProfile(actionMessage = "❤️ ${profile.firstName} ajouté aux favoris !")
                },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
            )
        }
    }

    fun passCurrentProfile() {
        val profile = _uiState.value.currentProfile ?: return
        advanceToNextProfile(actionMessage = "❌ Profil passé")
    }

    private fun advanceToNextProfile(actionMessage: String) {
        _uiState.update {
            it.copy(
                currentIndex = it.currentIndex + 1,
                lastActionMessage = actionMessage
            )
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLoggedOut()
        }
    }
}
