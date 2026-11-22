package fr.unica.miage.komeet.ui.profile

import fr.unica.miage.komeet.data.remote.dto.ProfileDto

data class ProfileDetailUiState(
    val profile: ProfileDto? = null,
    val isLoading: Boolean = false,
    val isLiked: Boolean = false,
    val errorMessage: String? = null,
    val actionMessage: String? = null
)

