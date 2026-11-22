package fr.unica.miage.komeet.ui.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.unica.miage.komeet.data.local.SessionStorage
import fr.unica.miage.komeet.data.repository.AuthRepository
import fr.unica.miage.komeet.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PreferencesViewModel(
    private val profileRepository: ProfileRepository,
    private val sessionStorage: SessionStorage,
    private val authRepository: AuthRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreferencesUiState())
    val uiState: StateFlow<PreferencesUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    fun loadPreferences() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = profileRepository.getPreferences()
            val savedGender = sessionStorage.getFilterGender()
            val savedDept = sessionStorage.getFilterDepartment() ?: ""
            val savedRegion = sessionStorage.getFilterRegion() ?: ""
            val savedCountry = sessionStorage.getFilterCountry()

            result.fold(
                onSuccess = { prefs ->
                    val finalCountry = prefs.country?.ifBlank { null } ?: savedCountry ?: "Maroc"
                    val finalRegion = prefs.region?.ifBlank { null } ?: savedRegion

                    _uiState.update {
                        it.copy(
                            minAge = prefs.minAge ?: 18,
                            maxAge = prefs.maxAge ?: 60,
                            country = finalCountry,
                            region = finalRegion,
                            gender = savedGender,
                            department = savedDept,
                            isLoading = false
                        )
                    }
                    loadRegionsForCountry(finalCountry)
                },
                onFailure = { error ->
                    val fallbackCountry = savedCountry ?: "Maroc"
                    _uiState.update {
                        it.copy(
                            country = fallbackCountry,
                            region = savedRegion,
                            gender = savedGender,
                            department = savedDept,
                            isLoading = false,
                            errorMessage = error.message ?: "Impossible de charger les préférences"
                        )
                    }
                    loadRegionsForCountry(fallbackCountry)
                }
            )
        }
    }

    fun loadRegionsForCountry(country: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRegions = true) }
            val filterCountry = if (country.isNullOrBlank() || country == "Tous") null else country
            val result = profileRepository.getRegions(filterCountry)
            result.fold(
                onSuccess = { regions ->
                    _uiState.update {
                        it.copy(
                            availableRegions = regions,
                            isLoadingRegions = false
                        )
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoadingRegions = false) }
                }
            )
        }
    }

    fun onAgeRangeChange(min: Int, max: Int) {
        _uiState.update { it.copy(minAge = min, maxAge = max) }
    }

    fun onCountryChange(country: String) {
        val effective = if (country == "Tous") "" else country
        _uiState.update { it.copy(country = effective, region = "", department = "") }
        loadRegionsForCountry(effective)
    }

    fun onRegionChange(region: String) {
        _uiState.update { it.copy(region = region, department = "") }
    }

    fun onGenderChange(gender: String?) {
        _uiState.update { it.copy(gender = gender) }
    }

    fun onDepartmentChange(department: String) {
        _uiState.update { it.copy(department = department) }
    }

    fun savePreferences() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            val state = _uiState.value

            val effectiveCountry = if (state.country == "Tous") "" else state.country

            // Sauvegarde locale du genre, département, région et pays
            sessionStorage.saveFilterPreferences(
                gender = state.gender,
                department = state.department.trim().takeIf { it.isNotEmpty() },
                region = state.region.trim().takeIf { it.isNotEmpty() },
                country = effectiveCountry.trim().takeIf { it.isNotEmpty() }
            )

            // Sauvegarde distante (API backend)
            val result = profileRepository.updatePreferences(
                minAge = state.minAge,
                maxAge = state.maxAge,
                country = effectiveCountry,
                region = state.region.trim().takeIf { it.isNotEmpty() }
            )

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            successMessage = "Préférences de région et filtres enregistrés avec succès !"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "Erreur lors de la sauvegarde"
                        )
                    }
                }
            )
        }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            try {
                authRepository?.logout() ?: sessionStorage.clearSession()
            } catch (_: Exception) {
            } finally {
                onLoggedOut()
            }
        }
    }
}
