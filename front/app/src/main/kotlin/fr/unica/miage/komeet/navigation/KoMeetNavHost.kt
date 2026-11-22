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
import fr.unica.miage.komeet.ui.chat.ChatScreen
import fr.unica.miage.komeet.ui.chat.ChatViewModel
import fr.unica.miage.komeet.ui.chat.ConversationsScreen
import fr.unica.miage.komeet.ui.chat.ConversationsViewModel
import fr.unica.miage.komeet.ui.discovery.DiscoveryScreen
import fr.unica.miage.komeet.ui.discovery.DiscoveryViewModel
import fr.unica.miage.komeet.ui.favorites.FavoritesScreen
import fr.unica.miage.komeet.ui.favorites.FavoritesViewModel
import fr.unica.miage.komeet.ui.preferences.PreferencesScreen
import fr.unica.miage.komeet.ui.preferences.PreferencesViewModel
import fr.unica.miage.komeet.ui.profile.ProfileDetailScreen
import fr.unica.miage.komeet.ui.profile.ProfileDetailViewModel
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

            // Écran Découverte / Tinder swiper
            entry<Screen.Discovery> {
                val viewModel: DiscoveryViewModel = koinViewModel()
                DiscoveryScreen(
                    viewModel = viewModel,
                    onNavigateToFavorites = { backStack.add(Screen.Favorites) },
                    onNavigateToConversations = { backStack.add(Screen.Conversations) },
                    onNavigateToPreferences = { backStack.add(Screen.Preferences) },
                    onNavigateToDetail = { profileId -> backStack.add(Screen.ProfileDetail(profileId)) },
                    onNavigateToChat = { profileId -> backStack.add(Screen.Chat(profileId)) },
                    onLogout = {
                        while (backStack.isNotEmpty()) {
                            backStack.removeAt(backStack.size - 1)
                        }
                        backStack.add(Screen.Login)
                    }
                )
            }

            // Écran Favoris (Jalon 3)
            entry<Screen.Favorites> {
                val viewModel: FavoritesViewModel = koinViewModel()
                FavoritesScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { profileId -> backStack.add(Screen.ProfileDetail(profileId)) },
                    onNavigateToChat = { profileId -> backStack.add(Screen.Chat(profileId)) },
                    onNavigateToConversations = { backStack.add(Screen.Conversations) },
                    onBack = {
                        if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                    }
                )
            }

            // Écran Détail du Profil (Jalon 4)
            entry<Screen.ProfileDetail> { key ->
                val viewModel: ProfileDetailViewModel = koinViewModel()
                ProfileDetailScreen(
                    profileId = key.profileId,
                    viewModel = viewModel,
                    onNavigateToChat = { profileId -> backStack.add(Screen.Chat(profileId)) },
                    onBack = {
                        if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                    }
                )
            }

            // Écran Liste des Discussions / Conversations
            entry<Screen.Conversations> {
                val viewModel: ConversationsViewModel = koinViewModel()
                ConversationsScreen(
                    viewModel = viewModel,
                    onNavigateToChat = { profileId -> backStack.add(Screen.Chat(profileId)) },
                    onNavigateToDiscovery = {
                        while (backStack.size > 1) {
                            backStack.removeAt(backStack.size - 1)
                        }
                    },
                    onBack = {
                        if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                    }
                )
            }

            // Écran Chat IA Persona (Gemini Chatbot)
            entry<Screen.Chat> { key ->
                val viewModel: ChatViewModel = koinViewModel()
                ChatScreen(
                    profileId = key.profileId,
                    viewModel = viewModel,
                    onBack = {
                        if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                    }
                )
            }

            // Écran Préférences de recherche (Jalon 4)
            entry<Screen.Preferences> {
                val viewModel: PreferencesViewModel = koinViewModel()
                PreferencesScreen(
                    viewModel = viewModel,
                    onBack = {
                        if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                    },
                    onLogout = {
                        while (backStack.isNotEmpty()) {
                            backStack.removeAt(backStack.size - 1)
                        }
                        backStack.add(Screen.Login)
                    }
                )
            }
        }
    )
}
