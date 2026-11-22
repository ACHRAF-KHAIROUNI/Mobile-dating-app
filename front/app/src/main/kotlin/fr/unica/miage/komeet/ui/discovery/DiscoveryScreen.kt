package fr.unica.miage.komeet.ui.discovery

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import fr.unica.miage.komeet.data.remote.dto.ProfileDto
import fr.unica.miage.komeet.ui.theme.LikeGreen
import fr.unica.miage.komeet.ui.theme.PassRed
import fr.unica.miage.komeet.ui.theme.SuperlikeBlue
import fr.unica.miage.komeet.ui.theme.TinderCoral
import fr.unica.miage.komeet.ui.theme.TinderFlame
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

fun getCountryFlag(country: String): String {
    return when (country.trim().lowercase()) {
        "maroc", "morocco" -> "🇲🇦"
        "france" -> "🇫🇷"
        "usa", "united states", "états-unis" -> "🇺🇸"
        "japon", "japan" -> "🇯🇵"
        "arabie saoudite", "saudi arabia" -> "🇸🇦"
        else -> "🌍"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
    viewModel: DiscoveryViewModel,
    onNavigateToFavorites: () -> Unit,
    onNavigateToConversations: () -> Unit = {},
    onNavigateToPreferences: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToChat: (String) -> Unit = {},
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Swipe animations
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    // Screen width in pixels for fly-off calculation
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val swipeThreshold = screenWidthPx * 0.35f

    // Reset card position whenever current profile changes
    LaunchedEffect(uiState.currentIndex) {
        offsetX.snapTo(0f)
        offsetY.snapTo(0f)
    }

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
                    IconButton(onClick = onNavigateToConversations) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Mes Discussions",
                            tint = TinderFlame
                        )
                    }
                    IconButton(onClick = onNavigateToFavorites) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Mes Favoris",
                            tint = TinderCoral
                        )
                    }
                    IconButton(onClick = onNavigateToPreferences) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Paramètres & Préférences",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { viewModel.logout(onLogout) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Déconnexion",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            // 1. FILTRE GENRE DÉDIÉ (Garçons & Filles bien à part)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tous
                FilterChip(
                    selected = uiState.selectedGender == null,
                    onClick = { viewModel.setGenderFilter(null) },
                    label = { Text("✨ Tous", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TinderFlame.copy(alpha = 0.15f),
                        selectedLabelColor = TinderFlame
                    )
                )

                // Filles
                FilterChip(
                    selected = uiState.selectedGender == "female",
                    onClick = { viewModel.setGenderFilter("female") },
                    label = { Text("👩 Filles", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TinderFlame.copy(alpha = 0.2f),
                        selectedLabelColor = TinderFlame
                    )
                )

                // Garçons
                FilterChip(
                    selected = uiState.selectedGender == "male",
                    onClick = { viewModel.setGenderFilter("male") },
                    label = { Text("👨 Garçons", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SuperlikeBlue.copy(alpha = 0.2f),
                        selectedLabelColor = SuperlikeBlue
                    )
                )
            }

            // 2. FILTRE PAYS & RÉGIONS (Avec option "Tous les pays")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Option "Tous les pays"
                val isAllCountries = uiState.selectedCountry == null
                FilterChip(
                    selected = isAllCountries,
                    onClick = { viewModel.setCountryFilter(null) },
                    label = { Text("🌍 Tous les pays", fontWeight = if (isAllCountries) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TinderFlame.copy(alpha = 0.2f),
                        selectedLabelColor = TinderFlame
                    )
                )

                // Pays spécifiques
                val countries = listOf("Maroc", "France", "USA", "Japon", "Arabie Saoudite")
                countries.forEach { c ->
                    val isSelected = uiState.selectedCountry?.equals(c, ignoreCase = true) == true
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.setCountryFilter(if (isSelected) null else c)
                        },
                        label = { Text("${getCountryFlag(c)} $c", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TinderFlame.copy(alpha = 0.2f),
                            selectedLabelColor = TinderFlame
                        )
                    )
                }
            }

            // Indicateur de filtre Région active si sélectionnée
            if (uiState.selectedRegion.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = TinderFlame,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Région filtrée : ${uiState.selectedRegion}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = TinderFlame
                        )
                    }
                    Text(
                        text = "Réinitialiser",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { viewModel.setRegionFilter("") }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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
                            // Stack de cartes de profil Tinder
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                // Carte suivante visible en dessous (effet de pile Tinder)
                                uiState.nextProfile?.let { nextProfile ->
                                    ProfileCard(
                                        profile = nextProfile,
                                        onCardClick = { onNavigateToDetail(nextProfile.id) },
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .scale(0.94f)
                                    )
                                }

                                // Carte active glissable (Swipeable Top Card)
                                val currentProfile = uiState.currentProfile!!
                                val dragModifier = Modifier
                                    .fillMaxSize()
                                    .offset {
                                        IntOffset(
                                            offsetX.value.roundToInt(),
                                            offsetY.value.roundToInt()
                                        )
                                    }
                                    .graphicsLayer {
                                        rotationZ = (offsetX.value / 40f).coerceIn(-25f, 25f)
                                    }
                                    .pointerInput(currentProfile.id) {
                                        detectDragGestures(
                                            onDragEnd = {
                                                coroutineScope.launch {
                                                    when {
                                                        offsetX.value > swipeThreshold -> {
                                                            offsetX.animateTo(
                                                                targetValue = screenWidthPx * 1.5f,
                                                                animationSpec = tween(220)
                                                            )
                                                            viewModel.likeCurrentProfile()
                                                        }
                                                        offsetX.value < -swipeThreshold -> {
                                                            offsetX.animateTo(
                                                                targetValue = -screenWidthPx * 1.5f,
                                                                animationSpec = tween(220)
                                                            )
                                                            viewModel.passCurrentProfile()
                                                        }
                                                        else -> {
                                                            launch {
                                                                offsetX.animateTo(
                                                                    0f,
                                                                    spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                                                )
                                                            }
                                                            launch {
                                                                offsetY.animateTo(
                                                                    0f,
                                                                    spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                coroutineScope.launch {
                                                    offsetX.snapTo(offsetX.value + dragAmount.x)
                                                    offsetY.snapTo(offsetY.value + dragAmount.y)
                                                }
                                            }
                                        )
                                    }

                                Box(modifier = dragModifier) {
                                    ProfileCard(
                                        profile = currentProfile,
                                        onCardClick = { onNavigateToDetail(currentProfile.id) },
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Badge LIKE translucide vert
                                    val likeAlpha = (offsetX.value / (swipeThreshold * 0.7f)).coerceIn(0f, 1f)
                                    if (likeAlpha > 0f) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(28.dp)
                                                .rotate(-15f)
                                                .alpha(likeAlpha)
                                                .border(
                                                    width = 4.dp,
                                                    color = LikeGreen,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .background(
                                                    color = Color.Black.copy(alpha = 0.25f),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "LIKE",
                                                color = LikeGreen,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 32.sp,
                                                letterSpacing = 2.sp
                                            )
                                        }
                                    }

                                    // Badge NOPE translucide rouge
                                    val nopeAlpha = (-offsetX.value / (swipeThreshold * 0.7f)).coerceIn(0f, 1f)
                                    if (nopeAlpha > 0f) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(28.dp)
                                                .rotate(15f)
                                                .alpha(nopeAlpha)
                                                .border(
                                                    width = 4.dp,
                                                    color = PassRed,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .background(
                                                    color = Color.Black.copy(alpha = 0.25f),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "NOPE",
                                                color = PassRed,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 32.sp,
                                                letterSpacing = 2.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Boutons d'actions rapides (Tinder Actions)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // PASS (NOPE)
                                FloatingActionButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            offsetX.animateTo(
                                                targetValue = -screenWidthPx * 1.5f,
                                                animationSpec = tween(220)
                                            )
                                            viewModel.passCurrentProfile()
                                        }
                                    },
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = PassRed,
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .size(58.dp)
                                        .border(1.dp, PassRed.copy(alpha = 0.2f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Pass",
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                // REFRESH
                                FloatingActionButton(
                                    onClick = viewModel::loadProfiles,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Recharger",
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // CHAT (DISCUTER) - Avec dégradé Tinder et icône blanche
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(listOf(TinderFlame, TinderCoral))
                                        )
                                        .clickable {
                                            uiState.currentProfile?.let { onNavigateToChat(it.id) }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Discuter avec ${uiState.currentProfile?.firstName ?: "ce profil"}",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                // LIKE
                                FloatingActionButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            offsetX.animateTo(
                                                targetValue = screenWidthPx * 1.5f,
                                                animationSpec = tween(220)
                                            )
                                            viewModel.likeCurrentProfile()
                                        }
                                    },
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = LikeGreen,
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .border(1.dp, LikeGreen.copy(alpha = 0.2f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = "Like",
                                        modifier = Modifier.size(32.dp)
                                    )
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
                                text = "Ajustez vos filtres de pays ou région pour découvrir d'autres profils.",
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
}

@Composable
fun ProfileCard(
    profile: ProfileDto,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = profile.pictureUrl.ifBlank { profile.thumbnailUrl },
                contentDescription = "${profile.firstName} ${profile.lastName}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dégradé sombre doux pour lisibilité
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.5f),
                                Color.Black.copy(alpha = 0.92f)
                            )
                        )
                    )
            )

            // Badge "✨ Match 98%" en haut à droite
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.45f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Text(
                    text = "✨ 98% Match",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            // Informations du profil superposées en bas de la carte
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${profile.firstName} ${profile.lastName}, ${profile.age}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Badge Vérifié
                        Surface(
                            shape = CircleShape,
                            color = SuperlikeBlue,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Vérifié",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }

                    // Bouton Info stylisé
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.22f),
                        modifier = Modifier
                            .size(38.dp)
                            .clickable(onClick = onCardClick)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Voir détail",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Affichage du Pays, Drapeau et Région
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = TinderCoral,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    val flag = getCountryFlag(profile.country)
                    val regionOrDept = profile.region ?: profile.location.region ?: profile.location.department
                    val regionText = regionOrDept?.let { " ($it)" } ?: ""
                    Text(
                        text = "$flag ${profile.location.city}$regionText • ${profile.country}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.95f),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Badges d'intérêts et passions (Hobby chips)
                val hobbies = remember(profile.id) {
                    val seed = profile.firstName.hashCode()
                    val allHobbies = listOf("✈️ Voyages", "☕ Café", "🎵 Musique", "📸 Photo", "🍕 Cuisine", "🎨 Art", "🏃 Sport", "🎬 Cinéma", "📚 Livres")
                    val idx1 = Math.abs(seed) % allHobbies.size
                    val idx2 = Math.abs(seed * 3) % allHobbies.size
                    val idx3 = Math.abs(seed * 7) % allHobbies.size
                    listOf(allHobbies[idx1], allHobbies[idx2], allHobbies[idx3]).distinct()
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    hobbies.forEach { hobby ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.18f),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = hobby,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
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
