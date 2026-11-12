package fr.unica.miage.komeet.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Destinations de l'application déclarées pour Navigation 3 (Nav3).
 * Toutes les routes sont des clés typées et sérialisables.
 */
sealed interface Screen : NavKey {

    /** Écran de connexion (fourni) */
    @Serializable
    data object Login : Screen

    /** Écran d'inscription (fourni) */
    @Serializable
    data object Register : Screen

    /** Écran principal de découverte des profils / Tinder swiper (à compléter par les étudiants) */
    @Serializable
    data object Discovery : Screen

    /** Écran de gestion des favoris (à réaliser par les étudiants) */
    @Serializable
    data object Favorites : Screen

    /** Écran de détail d'un profil sélectionné (à réaliser par les étudiants) */
    @Serializable
    data class ProfileDetail(val profileId: String) : Screen

    /** Écran des préférences utilisateur et de recherche (à réaliser par les étudiants) */
    @Serializable
    data object Preferences : Screen
}
