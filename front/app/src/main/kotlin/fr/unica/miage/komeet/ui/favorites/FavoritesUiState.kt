package fr.unica.miage.komeet.ui.favorites

import fr.unica.miage.komeet.data.remote.dto.FavoriteDto

data class FavoritesUiState(
    val favorites: List<FavoriteDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val actionMessage: String? = null
)

