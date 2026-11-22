package fr.unica.miage.komeet.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import fr.unica.miage.komeet.ui.discovery.getCountryFlag
import fr.unica.miage.komeet.ui.theme.LikeGreen
import fr.unica.miage.komeet.ui.theme.PassRed
import fr.unica.miage.komeet.ui.theme.TinderCoral
import fr.unica.miage.komeet.ui.theme.TinderFlame

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailScreen(
    profileId: String,
    viewModel: ProfileDetailViewModel,
    onBack: () -> Unit,
    onNavigateToChat: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(profileId) {
        viewModel.loadProfile(profileId)
    }

    LaunchedEffect(uiState.actionMessage) {
        uiState.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.profile?.let { "${it.firstName} ${it.lastName}" } ?: "Profil",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    uiState.profile?.let { profile ->
                        IconButton(onClick = { onNavigateToChat(profile.id) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Discuter",
                                tint = TinderFlame
                            )
                        }
                        IconButton(onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Découvre le profil de ${profile.firstName} sur KoMeet !"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Partager le profil"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Partager")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (uiState.profile != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FloatingActionButton(
                        onClick = viewModel::toggleFavorite,
                        containerColor = if (uiState.isLiked) TinderFlame else MaterialTheme.colorScheme.surface,
                        contentColor = if (uiState.isLiked) Color.White else TinderFlame,
                        shape = CircleShape,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (uiState.isLiked) "Retirer des favoris" else "Ajouter aux favoris",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    FloatingActionButton(
                        onClick = { onNavigateToChat(uiState.profile!!.id) },
                        containerColor = TinderFlame,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Discuter",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(color = TinderFlame)
                }

                uiState.errorMessage != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "Erreur",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadProfile(profileId) }) {
                            Text("Réessayer")
                        }
                    }
                }

                uiState.profile != null -> {
                    val profile = uiState.profile!!
                    val flag = getCountryFlag(profile.country)

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Grande Photo de profil
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp)
                        ) {
                            AsyncImage(
                                model = profile.pictureUrl.ifBlank { profile.thumbnailUrl },
                                contentDescription = "${profile.firstName} ${profile.lastName}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Dégradé sombre
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.82f)
                                            )
                                        )
                                    )
                            )

                            // Nom, Âge et Pays
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = "${profile.firstName} ${profile.lastName}, ${profile.age}",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = TinderCoral,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.size(4.dp))
                                    val regionOrDept = profile.region ?: profile.location.region ?: profile.location.department
                                    val regionText = regionOrDept?.let { " • $it" } ?: ""
                                    Text(
                                        text = "$flag ${profile.location.city}$regionText, ${profile.country}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White.copy(alpha = 0.95f)
                                    )
                                }
                            }
                        }

                        // Détails et informations
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            // Badges (Genre, Âge, Pays, Région)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                profile.gender?.let { gender ->
                                    val label = when (gender.lowercase()) {
                                        "male", "homme" -> "Homme ♂️"
                                        "female", "femme" -> "Femme ♀️"
                                        else -> gender
                                    }
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(label) }
                                    )
                                }
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("${profile.age} ans") }
                                )
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("$flag ${profile.country}") }
                                )
                            }

                            // Passions & Intérêts
                            Text(
                                text = "Centres d'intérêt",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                            val hobbies = remember(profile.id) {
                                val seed = profile.firstName.hashCode()
                                val allHobbies = listOf("✈️ Voyages", "☕ Café", "🎵 Musique", "📸 Photographie", "🍕 Gastronomie", "🎨 Art & Design", "🏃 Fitness", "🎬 Séries & Cinéma", "📚 Lecture", "🌿 Nature")
                                val idx1 = Math.abs(seed) % allHobbies.size
                                val idx2 = Math.abs(seed * 3) % allHobbies.size
                                val idx3 = Math.abs(seed * 7) % allHobbies.size
                                val idx4 = Math.abs(seed * 11) % allHobbies.size
                                listOf(allHobbies[idx1], allHobbies[idx2], allHobbies[idx3], allHobbies[idx4]).distinct()
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                hobbies.forEach { hobby ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = TinderFlame.copy(alpha = 0.1f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, TinderFlame.copy(alpha = 0.25f))
                                    ) {
                                        Text(
                                            text = hobby,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TinderFlame,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Section Localisation & Région
                            DetailCard(
                                title = "Adresse & Région",
                                icon = Icons.Default.Map
                            ) {
                                val street = listOfNotNull(
                                    profile.location.streetNumber?.toString(),
                                    profile.location.streetName
                                ).joinToString(" ")

                                if (street.isNotBlank()) {
                                    DetailItem(label = "Rue", value = street)
                                }
                                DetailItem(
                                    label = "Ville",
                                    value = profile.location.city.ifBlank { profile.city }
                                )
                                val reg = profile.region ?: profile.location.region ?: profile.location.department
                                reg?.let {
                                    DetailItem(label = "Région / État", value = it)
                                }
                                profile.location.postcode?.let {
                                    DetailItem(label = "Code postal", value = it)
                                }
                                DetailItem(label = "Pays", value = "$flag ${profile.location.country}")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Section Contact
                            DetailCard(
                                title = "Coordonnées de Contact",
                                icon = Icons.Default.Call
                            ) {
                                profile.phone?.let { phone ->
                                    DetailItem(
                                        label = "Téléphone",
                                        value = phone,
                                        action = {
                                            OutlinedButton(
                                                onClick = {
                                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                                        data = Uri.parse("tel:$phone")
                                                    }
                                                    context.startActivity(intent)
                                                },
                                                modifier = Modifier.padding(top = 4.dp)
                                            ) {
                                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Appeler")
                                            }
                                        }
                                    )
                                }

                                profile.email?.let { email ->
                                    DetailItem(
                                        label = "Email",
                                        value = email,
                                        action = {
                                            OutlinedButton(
                                                onClick = {
                                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                                        data = Uri.parse("mailto:$email")
                                                    }
                                                    context.startActivity(intent)
                                                },
                                                modifier = Modifier.padding(top = 4.dp)
                                            ) {
                                                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Envoyer un email")
                                            }
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Section Infos Système
                            DetailCard(
                                title = "Informations sur le profil",
                                icon = Icons.Default.Info
                            ) {
                                DetailItem(label = "Identifiant", value = profile.id)
                                DetailItem(label = "Source", value = profile.source)
                                DetailItem(label = "Membre depuis", value = profile.createdAt)
                            }

                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TinderFlame,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TinderFlame
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    action: (@Composable () -> Unit)? = null
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
        action?.invoke()
    }
}
