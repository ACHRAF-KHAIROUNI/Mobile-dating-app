package fr.unica.miage.komeet.ui.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.unica.miage.komeet.data.local.SessionStorage
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
    val selectedGender: String? = null, // null = Tous, "female" = Filles, "male" = Garçons
    val selectedCountry: String? = null,
    val selectedRegion: String = "",
    val selectedDepartment: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lastActionMessage: String? = null
) {
    val currentProfile: ProfileDto?
        get() = if (currentIndex in profiles.indices) profiles[currentIndex] else null

    val nextProfile: ProfileDto?
        get() = if (currentIndex + 1 in profiles.indices) profiles[currentIndex + 1] else null
}

class DiscoveryViewModel(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val sessionStorage: SessionStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoveryUiState())
    val uiState: StateFlow<DiscoveryUiState> = _uiState.asStateFlow()

    init {
        initFiltersAndLoad()
    }

    private fun initFiltersAndLoad() {
        viewModelScope.launch {
            val savedGender = sessionStorage.getFilterGender()
            val savedRegion = sessionStorage.getFilterRegion() ?: ""
            val savedCountry = sessionStorage.getFilterCountry()

            _uiState.update {
                it.copy(
                    selectedGender = savedGender,
                    selectedDepartment = "",
                    selectedRegion = savedRegion,
                    selectedCountry = savedCountry
                )
            }
            loadProfiles()
        }
    }

    fun setGenderFilter(gender: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedGender = gender) }
            sessionStorage.saveFilterPreferences(
                gender = gender,
                department = "",
                region = _uiState.value.selectedRegion,
                country = _uiState.value.selectedCountry
            )
            loadProfiles()
        }
    }

    fun setCountryFilter(country: String?) {
        viewModelScope.launch {
            val effectiveCountry = if (country.isNullOrBlank() || country.equals("Tous", ignoreCase = true) || country.equals("Tous les pays", ignoreCase = true)) null else country
            _uiState.update { it.copy(selectedCountry = effectiveCountry, selectedRegion = "", selectedDepartment = "") }
            sessionStorage.saveFilterPreferences(
                gender = _uiState.value.selectedGender,
                department = "",
                region = "",
                country = effectiveCountry
            )
            loadProfiles()
        }
    }

    fun setRegionFilter(region: String) {
        viewModelScope.launch {
            val effectiveRegion = if (region == "Toutes les régions" || region == "Tous") "" else region
            _uiState.update { it.copy(selectedRegion = effectiveRegion, selectedDepartment = "") }
            sessionStorage.saveFilterPreferences(
                gender = _uiState.value.selectedGender,
                department = "",
                region = effectiveRegion,
                country = _uiState.value.selectedCountry
            )
            loadProfiles()
        }
    }

    fun loadProfiles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val prefs = profileRepository.getPreferences().getOrNull()

            val currentGender = _uiState.value.selectedGender ?: sessionStorage.getFilterGender()
            val currentCountry = _uiState.value.selectedCountry
                ?: sessionStorage.getFilterCountry()
                ?: prefs?.country?.takeIf { it.isNotBlank() && it != "Tous" }
            val currentRegion = _uiState.value.selectedRegion.ifBlank {
                sessionStorage.getFilterRegion() ?: prefs?.region ?: ""
            }

            val filterCountryParam = currentCountry?.trim()?.takeIf { it.isNotEmpty() && it != "Tous" }
            val filterRegionParam = currentRegion.trim().takeIf { it.isNotEmpty() && it != "Toutes les régions" && it != "Tous" }

            val result = profileRepository.getProfiles(
                page = 1,
                pageSize = 60,
                gender = currentGender,
                department = null,
                region = filterRegionParam,
                minAge = prefs?.minAge,
                maxAge = prefs?.maxAge,
                country = filterCountryParam
            )

            result.fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            profiles = response.items.shuffled(),
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
            try {
                authRepository.logout()
            } catch (_: Exception) {
            } finally {
                onLoggedOut()
            }
        }
    }
}
