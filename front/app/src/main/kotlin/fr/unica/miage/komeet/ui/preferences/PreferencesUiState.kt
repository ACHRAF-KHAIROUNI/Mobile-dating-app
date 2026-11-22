package fr.unica.miage.komeet.ui.preferences

import fr.unica.miage.komeet.data.remote.dto.RegionDto

data class PreferencesUiState(
    val minAge: Int = 18,
    val maxAge: Int = 60,
    val country: String = "Maroc",
    val region: String = "", // Région du partenaire recherché
    val gender: String? = null, // null = Tous, "female" = Fille, "male" = Garçon
    val department: String = "",
    val availableRegions: List<RegionDto> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingRegions: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
