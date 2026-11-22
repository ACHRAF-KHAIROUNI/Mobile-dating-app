package fr.unica.miage.komeet.ui.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.unica.miage.komeet.ui.theme.LikeGreen
import fr.unica.miage.komeet.ui.theme.PassRed
import fr.unica.miage.komeet.ui.theme.SuperlikeBlue
import fr.unica.miage.komeet.ui.theme.TinderCoral
import fr.unica.miage.komeet.ui.theme.TinderFlame
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PreferencesScreen(
    viewModel: PreferencesViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showTermsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
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
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = TinderFlame,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Paramètres & Préférences", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.logout(onLogout) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Déconnexion",
                            tint = PassRed
                        )
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
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(color = TinderFlame)
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Section 1 : Genre recherché (Garçons / Filles)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = null,
                                        tint = TinderFlame,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Text(
                                        text = "Je souhaite voir",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = uiState.gender == null,
                                        onClick = { viewModel.onGenderChange(null) },
                                        label = { Text("Tous ✨") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = TinderFlame.copy(alpha = 0.15f),
                                            selectedLabelColor = TinderFlame
                                        )
                                    )

                                    FilterChip(
                                        selected = uiState.gender == "female",
                                        onClick = { viewModel.onGenderChange("female") },
                                        label = { Text("Filles 👩") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = TinderFlame.copy(alpha = 0.2f),
                                            selectedLabelColor = TinderFlame
                                        )
                                    )

                                    FilterChip(
                                        selected = uiState.gender == "male",
                                        onClick = { viewModel.onGenderChange("male") },
                                        label = { Text("Garçons 👨") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SuperlikeBlue.copy(alpha = 0.2f),
                                            selectedLabelColor = SuperlikeBlue
                                        )
                                    )
                                }
                            }
                        }

                        // Section 2 : Pays du partenaire recherché
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = null,
                                        tint = TinderFlame,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Text(
                                        text = "Pays du partenaire",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                val countries = listOf(
                                    "Tous" to "🌍 Tous les pays",
                                    "Maroc" to "🇲🇦 Maroc",
                                    "France" to "🇫🇷 France",
                                    "USA" to "🇺🇸 USA",
                                    "Japon" to "🇯🇵 Japon",
                                    "Arabie Saoudite" to "🇸🇦 Arabie Saoudite"
                                )

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    countries.forEach { (code, label) ->
                                        val isSelected = uiState.country.equals(code, ignoreCase = true) ||
                                                (code == "Tous" && uiState.country.isBlank())
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { viewModel.onCountryChange(code) },
                                            label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = TinderFlame.copy(alpha = 0.2f),
                                                selectedLabelColor = TinderFlame
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Section 3 : Région du partenaire recherché (Mission Région)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Map,
                                            contentDescription = null,
                                            tint = TinderFlame,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.size(8.dp))
                                        Text(
                                            text = "Région du partenaire",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }

                                    if (uiState.region.isNotBlank()) {
                                        Text(
                                            text = "Effacer",
                                            color = TinderCoral,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.clickable { viewModel.onRegionChange("") }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Choisissez la région recherchée pour filtrer vos rencontres :",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                if (uiState.isLoadingRegions) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = TinderFlame,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Chargement des régions...", style = MaterialTheme.typography.bodySmall)
                                    }
                                } else if (uiState.availableRegions.isNotEmpty()) {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Option "Toutes les régions"
                                        FilterChip(
                                            selected = uiState.region.isBlank(),
                                            onClick = { viewModel.onRegionChange("") },
                                            label = { Text("Toutes les régions") }
                                        )

                                        uiState.availableRegions.forEach { regionDto ->
                                            val isSelected = uiState.region.equals(regionDto.name, ignoreCase = true)
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    viewModel.onRegionChange(if (isSelected) "" else regionDto.name)
                                                },
                                                label = { Text(regionDto.name) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = TinderFlame.copy(alpha = 0.2f),
                                                    selectedLabelColor = TinderFlame
                                                )
                                            )
                                        }
                                    }
                                } else {
                                    OutlinedTextField(
                                        value = uiState.region,
                                        onValueChange = viewModel::onRegionChange,
                                        label = { Text("Nom de la région") },
                                        placeholder = { Text("Ex: Casablanca-Settat, California, Kanto...") },
                                        leadingIcon = {
                                            Icon(Icons.Default.LocationCity, contentDescription = null, tint = TinderCoral)
                                        },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }

                        // Section 4 : Tranche d'âge
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = null,
                                            tint = TinderFlame,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.size(8.dp))
                                        Text(
                                            text = "Tranche d'âge",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                    Text(
                                        text = "${uiState.minAge} - ${uiState.maxAge} ans",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TinderFlame,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                RangeSlider(
                                    value = uiState.minAge.toFloat()..uiState.maxAge.toFloat(),
                                    onValueChange = { range ->
                                        viewModel.onAgeRangeChange(
                                            min = range.start.roundToInt(),
                                            max = range.endInclusive.roundToInt()
                                        )
                                    },
                                    valueRange = 18f..80f,
                                    steps = 61,
                                    colors = SliderDefaults.colors(
                                        thumbColor = TinderFlame,
                                        activeTrackColor = TinderCoral,
                                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("18 ans", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("80 ans", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        // Bouton d'enregistrement des filtres
                        Button(
                            onClick = viewModel::savePreferences,
                            enabled = !uiState.isSaving,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TinderFlame,
                                contentColor = Color.White
                            )
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = "Enregistrer mes filtres de région",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // CARTE CRÉATEUR DE L'APPLICATION (ACHRAF KHAIROUNI)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                TinderFlame.copy(alpha = 0.08f),
                                                TinderCoral.copy(alpha = 0.15f)
                                            )
                                        )
                                    )
                                    .padding(18.dp)
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(TinderFlame),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "ACHRAF KHAIROUNI",
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 18.sp,
                                                    color = TinderFlame
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Verified,
                                                    contentDescription = "Créateur vérifié",
                                                    tint = SuperlikeBlue,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            Text(
                                                text = "Créateur & Développeur Lead de KoMeet",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "🎓 Université Côte d'Azur — Master MIAGE\n" +
                                                "📱 Application KoMeet Mobile Android v1.0.0 (Kotlin / Jetpack Compose)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        // CARTE CONDITIONS D'UTILISATION (CGU)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showTermsDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = TinderFlame,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.size(12.dp))
                                    Column {
                                        Text(
                                            text = "Conditions Générales d'Utilisation",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "Règles de respect, confidentialité et vie privée",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = LikeGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Bouton Déconnexion
                        Button(
                            onClick = { viewModel.logout(onLogout) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PassRed.copy(alpha = 0.12f),
                                contentColor = PassRed
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = null,
                                tint = PassRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Se déconnecter",
                                color = PassRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // Dialogue Modal des Conditions d'Utilisation
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = TinderFlame,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Conditions d'Utilisation",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Bienvenue sur KoMeet, l'application de rencontre conçue par ACHRAF KHAIROUNI.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "1. Respect mutuel et bienveillance :\n" +
                                "KoMeet est un espace convivial et sécurisé. Tout propos injurieux, diffamatoire ou discriminatoire est strictement prohibé.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "2. Âge minimum requis :\n" +
                                "L'accès à l'application KoMeet est réservé aux personnes âgées de 18 ans et plus.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "3. Protection de la vie privée (RGPD) :\n" +
                                "Vos données personnelles (email, favoris, préférences) sont protégées et ne sont jamais cédées à des tiers. Les communications réseau sont chiffrées avec Bearer JWT.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "4. Sécurité des échanges :\n" +
                                "Ne partagez jamais vos coordonnées bancaires ou mots de passe avec d'autres utilisateurs.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "5. Propriété intellectuelle :\n" +
                                "L'application mobile KoMeet a été développée et pensée par ACHRAF KHAIROUNI dans le cadre du cursus MIAGE - Université Côte d'Azur.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TinderFlame
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTermsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TinderFlame)
                ) {
                    Text("J'ai compris et j'accepte")
                }
            }
        )
    }
}
