package fr.unica.miage.komeet.ui.discovery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import fr.unica.miage.komeet.data.remote.dto.ProfileDto
import fr.unica.miage.komeet.ui.theme.LikeGreen
import fr.unica.miage.komeet.ui.theme.PassRed
import fr.unica.miage.komeet.ui.theme.TinderCoral
import fr.unica.miage.komeet.ui.theme.TinderFlame

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
    viewModel: DiscoveryViewModel,
    onNavigateToFavorites: () -> Unit,
    onNavigateToPreferences: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.lastActionMessage) {
        uiState.lastActionMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = TinderFlame,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "KoMeet",
                            fontWeight = FontWeight.ExtraBold,
                            color = TinderFlame,
                            fontSize = 24.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToFavorites) {
                        Icon(Icons.Default.Favorite, contentDescription = "Mes Favoris", tint = TinderCoral)
                    }
                    IconButton(onClick = onNavigateToPreferences) {
                        Icon(Icons.Default.Settings, contentDescription = "Préférences", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = { viewModel.logout(onLogout) }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Déconnexion", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(color = TinderFlame)
                }
                uiState.errorMessage != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "Erreur",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = viewModel::loadProfiles) {
                            Text("Réessayer")
                        }
                    }
                }
                uiState.currentProfile != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Carte de profil Tinder
                        // TODO Étudiants (Jalon 2) : Ajouter le geste de drag/swipe sur la carte
                        // avec Modifier.pointerInput { detectDragGestures { ... } } et rotation
                        ProfileCard(
                            profile = uiState.currentProfile!!,
                            onCardClick = { onNavigateToDetail(uiState.currentProfile!!.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Boutons d'actions rapides (Tinder Actions)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Bouton Pass (Croix rouge)
                            FloatingActionButton(
                                onClick = viewModel::passCurrentProfile,
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = PassRed,
                                shape = CircleShape,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Pass", modifier = Modifier.size(32.dp))
                            }

                            // Bouton Recharger / Rafraîchir
                            FloatingActionButton(
                                onClick = viewModel::loadProfiles,
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                shape = CircleShape,
                                modifier = Modifier.size(50.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Recharger", modifier = Modifier.size(24.dp))
                            }

                            // Bouton Like (Cœur vert / rose)
                            FloatingActionButton(
                                onClick = viewModel::likeCurrentProfile,
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = LikeGreen,
                                shape = CircleShape,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(Icons.Default.Favorite, contentDescription = "Like", modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
                else -> {
                    // Fin de la pile de profils
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🎉 Vous avez vu tous les profils !",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Rechargez pour revoir les profils ou ajustez vos préférences.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = viewModel::loadProfiles) {
                            Text("Recharger les profils")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileCard(
    profile: ProfileDto,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Photo de profil en plein écran dans la carte
            AsyncImage(
                model = profile.pictureUrl,
                contentDescription = "${profile.firstName} ${profile.lastName}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dégradé sombre pour assurer la lisibilité du texte
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Informations du profil superposées en bas
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${profile.firstName} ${profile.lastName}, ${profile.age}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onCardClick) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Voir détail",
                            tint = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = TinderCoral,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    val deptText = profile.location.department?.let { " ($it)" } ?: ""
                    Text(
                        text = "${profile.location.city}$deptText",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                if (!profile.phone.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📞 ${profile.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}
