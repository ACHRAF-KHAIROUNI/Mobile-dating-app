package fr.unica.miage.komeet.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import fr.unica.miage.komeet.ui.auth.LoginScreen
import fr.unica.miage.komeet.ui.auth.LoginViewModel
import fr.unica.miage.komeet.ui.auth.RegisterScreen
import fr.unica.miage.komeet.ui.auth.RegisterViewModel
import fr.unica.miage.komeet.ui.discovery.DiscoveryScreen
import fr.unica.miage.komeet.ui.discovery.DiscoveryViewModel
import fr.unica.miage.komeet.ui.favorites.FavoritesScreen
import fr.unica.miage.komeet.ui.preferences.PreferencesScreen
import fr.unica.miage.komeet.ui.profile.ProfileDetailScreen
import org.koin.androidx.compose.koinViewModel

/**
 * Hôte de navigation principal utilisant Navigation 3 (Nav3).
 * Toutes les transitions sont orchestrées via NavDisplay et le backStack observable.
 */
@Composable
fun KoMeetNavHost(
    initialScreen: Screen = Screen.Login,
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(initialScreen)

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        entryProvider = entryProvider {
            // Écran de Connexion (fourni)
            entry<Screen.Login> {
                val viewModel: LoginViewModel = koinViewModel()
                LoginScreen(
                    viewModel = viewModel,
                    onNavigateToRegister = { backStack.add(Screen.Register) },
                    onLoginSuccess = {
                        backStack.clear()
                        backStack.add(Screen.Discovery)
                    }
                )
            }

            // Écran d'Inscription (fourni)
            entry<Screen.Register> {
                val viewModel: RegisterViewModel = koinViewModel()
                RegisterScreen(
                    viewModel = viewModel,
                    onNavigateToLogin = {
                        if (backStack.size > 1) {
                            backStack.removeAt(backStack.size - 1)
                        } else {
                            backStack.add(Screen.Login)
                        }
                    },
                    onRegisterSuccess = {
                        backStack.clear()
                        backStack.add(Screen.Discovery)
                    }
                )
            }

            // Écran Découverte / Tinder swiper (à compléter par les étudiants)
            entry<Screen.Discovery> {
                val viewModel: DiscoveryViewModel = koinViewModel()
                DiscoveryScreen(
                    viewModel = viewModel,
                    onNavigateToFavorites = { backStack.add(Screen.Favorites) },
                    onNavigateToPreferences = { backStack.add(Screen.Preferences) },
                    onNavigateToDetail = { profileId -> backStack.add(Screen.ProfileDetail(profileId)) },
                    onLogout = {
                        backStack.clear()
                        backStack.add(Screen.Login)
                    }
                )
            }

            // Écran Favoris (Jalon 3 étudiant)
            entry<Screen.Favorites> {
                FavoritesScreen(
                    onBack = {
                        if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                    }
                )
            }

            // Écran Détail du Profil (Jalon 4 étudiant)
            entry<Screen.ProfileDetail> { key ->
                ProfileDetailScreen(
                    profileId = key.profileId,
                    onBack = {
                        if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                    }
                )
            }

            // Écran Préférences de recherche (Jalon 4 étudiant)
            entry<Screen.Preferences> {
                PreferencesScreen(
                    onBack = {
                        if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                    }
                )
            }
        }
    )
}
