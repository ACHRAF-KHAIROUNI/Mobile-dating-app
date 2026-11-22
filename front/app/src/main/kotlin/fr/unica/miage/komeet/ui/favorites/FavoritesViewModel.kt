package fr.unica.miage.komeet.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.unica.miage.komeet.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = profileRepository.getFavorites(page = 1, pageSize = 50)
            result.fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            favorites = response.items,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Impossible de charger les favoris"
                        )
                    }
                }
            )
        }
    }

    fun removeFavorite(profileId: String) {
        viewModelScope.launch {
            val result = profileRepository.removeFavorite(profileId)
            result.fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            favorites = state.favorites.filter { it.profile.id != profileId },
                            actionMessage = "Profil retiré des favoris"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
            )
        }
    }

    fun clearActionMessage() {
        _uiState.update { it.copy(actionMessage = null) }
    }
}

