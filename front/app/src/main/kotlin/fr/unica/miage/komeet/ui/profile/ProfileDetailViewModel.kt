package fr.unica.miage.komeet.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.unica.miage.komeet.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileDetailViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileDetailUiState())
    val uiState: StateFlow<ProfileDetailUiState> = _uiState.asStateFlow()

    fun loadProfile(profileId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = profileRepository.getProfile(profileId)
            result.fold(
                onSuccess = { profile ->
                    // Check if already in favorites
                    val favs = profileRepository.getFavorites(pageSize = 100).getOrNull()?.items.orEmpty()
                    val isFav = favs.any { it.profile.id == profile.id }
                    _uiState.update {
                        it.copy(
                            profile = profile,
                            isLiked = isFav,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Impossible de charger le profil"
                        )
                    }
                }
            )
        }
    }

    fun toggleFavorite() {
        val profile = _uiState.value.profile ?: return
        val currentlyLiked = _uiState.value.isLiked
        viewModelScope.launch {
            if (currentlyLiked) {
                val result = profileRepository.removeFavorite(profile.id)
                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isLiked = false,
                                actionMessage = "Retiré des favoris"
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(errorMessage = error.message) }
                    }
                )
            } else {
                val result = profileRepository.likeProfile(profile.id)
                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isLiked = true,
                                actionMessage = "Ajouté aux favoris ❤️"
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(errorMessage = error.message) }
                    }
                )
            }
        }
    }

    fun clearActionMessage() {
        _uiState.update { it.copy(actionMessage = null) }
    }
}

