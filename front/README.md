# KoMeet Android --- Application Tinder-like (Starter Étudiant)

Bienvenue sur le projet frontend Android **KoMeet**, une application mobile de type **Tinder** permettant de découvrir des profils, de swiper (Like / Pass), de gérer ses favoris et d'ajuster ses préférences de recherche.

Cette application est réalisée en **Kotlin**, **Jetpack Compose**, **Material 3**, **Navigation 3**, **Ktor Client 3** et **Koin 4**.

---

## 1. Objectif du TP / Projet

Le backend est déjà fourni dans `../back`. Il gère la persistance SQLite, l'authentification JWT, l'import des profils depuis Random User Generator, les favoris et les préférences.

Ce starter frontend vous fournit une base saine et prête à l'emploi :
- ✅ **Configuration Android & Gradle** (Gradle Wrapper, JDK 21, version catalog `libs.versions.toml`).
- ✅ **Navigation 3 (Nav3)** configurée avec des clés de routes type-safe (`Screen : NavKey`).
- ✅ **Injection de dépendances avec Koin 4** (`appModule`, `koinViewModel()`).
- ✅ **Réseau avec Ktor Client 3** configuré avec `Auth` (Bearer JWT) et `ContentNegotiation` (Kotlinx JSON).
- ✅ **Session utilisateur persistante** via Jetpack DataStore Preferences.
- ✅ **Écrans de Connexion (`LoginScreen`) et d'Inscription (`RegisterScreen`)** fonctionnels.

👉 **Votre mission :** Réaliser le cœur de l'application de rencontre (découverte avec gestes de swipe façon Tinder, gestion des favoris, fiche détaillée et réglage des préférences).

---

## 2. Prérequis et Démarrage

### Prérequis
1. **JDK 21 (LTS)** configuré.
2. **Android Studio**
3. Un **émulateur Android** (API 26+) ou un téléphone physique en mode débogage USB.

### Étape 1 : Démarrer le backend
Dans un premier terminal, positionnez-vous dans le dossier backend et lancez-le :
```bash
cd ../back 
./gradlew run
```
Le serveur répond sur `http://localhost:8080` (et `GET http://localhost:8080/health`).

### Étape 2 : Compiler et lancer l'application Android

Vous pouvez lancer l'application soit via **Android Studio**, soit directement en **ligne de commande avec Gradle**.

#### Option A : Depuis Android Studio (Recommandé)
1. Ouvrez le dossier `front` dans **Android Studio**.
2. Attendez la synchronisation Gradle (`Sync Project with Gradle Files`).
3. Sélectionnez votre émulateur dans la barre d'outils et cliquez sur le bouton **Run** (`Shift + F10`).

#### Option B : En ligne de commande avec Gradle & ADB

1. **Vérifier ou démarrer l'émulateur** :
   ```bash
   # Lister les émulateurs configurés
   emulator -list-avds

   # Démarrer votre émulateur (remplacer par le nom de votre AVD)
   emulator -avd <nom_avd> &
   ```
   *Attendez que l'émulateur soit complètement démarré et déverrouillé.*

2. **Compiler l'application** :
   ```bash
   ./gradlew assembleDebug
   ```
   *(Génère l'APK dans `app/build/outputs/apk/debug/app-debug.apk`)*

3. **Compiler, installer et lancer sur l'émulateur** :
   ```bash
   # Installer l'application sur l'émulateur actif
   ./gradlew installDebug

   # Lancer l'écran principal de l'application
   adb shell am start -n fr.unica.miage.komeet/.MainActivity
   ```

   *Astuce : Vous pouvez combiner la compilation, l'installation et le lancement en une seule commande :*
   ```bash
   ./gradlew installDebug && adb shell am start -n fr.unica.miage.komeet/.MainActivity
   ```

> 💡 **Adresse réseau de l'émulateur Android** :
> Pour contacter le serveur backend tournant sur votre machine hôte, l'adresse spéciale **`http://10.0.2.2:8080/api/v1/`** est déjà pré-configurée dans [KtorClientFactory.kt](file:///Users/donati/Projects/KoMeet/front/app/src/main/kotlin/fr/unica/miage/komeet/data/remote/KtorClientFactory.kt) et autorisée dans `res/xml/network_security_config.xml`.
> Si vous utilisez un téléphone physique en Wi-Fi, remplacez `10.0.2.2` par l'adresse IP locale de votre machine (ex: `192.168.1.XX`).

---

## 3. Architecture du Projet

Le projet applique les principes de l'architecture moderne Android (Clean Architecture / MVVM) :

```
front/app/src/main/kotlin/fr/unica/miage/komeet/
├── KoMeetApplication.kt        # Classe Application démarrant Koin (startKoin)
├── MainActivity.kt             # Point d'entrée lançant KoMeetNavHost
│
├── di/
│   └── AppModule.kt            # Modules Koin : injection de DataStore, Ktor, Repositories, ViewModels
│
├── navigation/
│   ├── Screen.kt               # Destinations de l'app (@Serializable sealed interface Screen : NavKey)
│   └── KoMeetNavHost.kt        # NavDisplay Nav3 et gestion explicite du backStack
│
├── data/
│   ├── local/
│   │   └── SessionDataStore.kt # Gestion DataStore du token JWT et du profil connecté
│   ├── remote/
│   │   ├── KtorClientFactory.kt# Client Ktor 3 avec plugin Auth (Bearer)
│   │   ├── KoMeetRemoteSource.kt# Appels HTTP typés vers l'API KoMeet
│   │   └── dto/                # Modèles de données JSON sérialisables
│   └── repository/
│       ├── AuthRepository.kt   # Inscription, connexion, déconnexion
│       └── ProfileRepository.kt# Récupération profils, favoris, préférences
│
└── ui/
    ├── theme/                  # Couleurs Tinder (Flamme, Corail), typographie, thèmes
    ├── auth/                   # Écrans de Connexion & Inscription (FOURNIS)
    │   ├── LoginScreen.kt & LoginViewModel.kt
    │   └── RegisterScreen.kt & RegisterViewModel.kt
    ├── discovery/              # Écran Découverte / Swipe Tinder (À COMPLÉTER)
    │   ├── DiscoveryScreen.kt & DiscoveryViewModel.kt
    │   └── ProfileCard.kt
    ├── favorites/              # Écran des favoris (Jalon 3)
    ├── profile/                # Fiche détaillée d'un profil (Jalon 4)
    └── preferences/            # Préférences de recherche (Jalon 4)
```

---

## 4. Fonctionnement de Navigation 3 (Nav3)

Google a repensé la navigation sous Jetpack Compose avec **Navigation 3** (`androidx.navigation3`). Fini les routes sous forme de chaînes de caractères arbitraires ou les contrôleurs opaques !

1. **Chaque écran est une clé typée (`NavKey`)** dans [Screen.kt](file:///Users/donati/Projects/KoMeet/front/app/src/main/kotlin/fr/unica/miage/komeet/navigation/Screen.kt) :
   ```kotlin
   sealed interface Screen : NavKey {
       @Serializable data object Login : Screen
       @Serializable data object Register : Screen
       @Serializable data object Discovery : Screen
       @Serializable data object Favorites : Screen
       @Serializable data class ProfileDetail(val profileId: String) : Screen
       @Serializable data object Preferences : Screen
   }
   ```
2. **La pile de navigation est une simple liste observable** dans [KoMeetNavHost.kt](file:///Users/donati/Projects/KoMeet/front/app/src/main/kotlin/fr/unica/miage/komeet/navigation/KoMeetNavHost.kt) :
   ```kotlin
   val backStack = rememberNavBackStack(initialScreen)

   // Naviguer vers un écran :
   backStack.add(Screen.Favorites)
   backStack.add(Screen.ProfileDetail("prof-123"))

   // Revenir en arrière :
   backStack.removeAt(backStack.size - 1)

   // Vider la pile (ex: après connexion réussie) :
   backStack.clear()
   backStack.add(Screen.Discovery)
   ```
3. **Le composable `NavDisplay` se charge d'afficher le bon écran** :
   ```kotlin
   NavDisplay(
       backStack = backStack,
       entryProvider = entryProvider {
           entry<Screen.Login> { LoginScreen(...) }
           entry<Screen.Discovery> { DiscoveryScreen(...) }
           entry<Screen.ProfileDetail> { key -> ProfileDetailScreen(profileId = key.profileId, ...) }
           // ...
       }
   )
   ```

---

## 5. Fonctionnement de Koin 4 & Ktor Client 3

### Injection de dépendances avec Koin
Les dépendances sont déclarées dans [AppModule.kt](file:///Users/donati/Projects/KoMeet/front/app/src/main/kotlin/fr/unica/miage/komeet/di/AppModule.kt).
Dans vos écrans Compose, vous pouvez injecter un ViewModel en une seule ligne :
```kotlin
val viewModel: DiscoveryViewModel = koinViewModel()
```

### Client HTTP Ktor 3 et gestion du Bearer JWT
Toutes les requêtes envoyées via `createKtorClient` incluent automatiquement l'en-tête `Authorization: Bearer <token>` dès qu'un utilisateur est connecté, grâce au plugin Ktor `Auth.bearer` connecté à `SessionDataStore`.

---

## 6. Travail attendu

### Exercice 1 : Affichage des profils et cartes
- Examinez [DiscoveryViewModel.kt](file:///Users/donati/Projects/KoMeet/front/app/src/main/kotlin/fr/unica/miage/komeet/ui/discovery/DiscoveryViewModel.kt) et [ProfileRepository.kt](file:///Users/donati/Projects/KoMeet/front/app/src/main/kotlin/fr/unica/miage/komeet/data/repository/ProfileRepository.kt).
- Les profils sont chargés via `GET /api/v1/profiles`.
- Dans [DiscoveryScreen.kt](file:///Users/donati/Projects/KoMeet/front/app/src/main/kotlin/fr/unica/miage/komeet/ui/discovery/DiscoveryScreen.kt), observez le composable `ProfileCard`.
- Personnalisez l'affichage de la carte pour faire apparaître la photo en grand avec Coil (`AsyncImage`), le prénom, le nom, l'âge, et la localisation (ville, département).

### Exercice 2 : Gestes de Swipe Tinder (Like ❤️ / Pass ❌)
- Les boutons d'action rapide sous la carte fonctionnent déjà (Pass, Recharger, Like).
- **Objectif :** Rendre la carte glissable au doigt (geste de swipe façon Tinder) :
  - Utilisez `Modifier.pointerInput` avec `detectDragGestures` ou `Modifier.draggable`.
  - Faites pivoter légèrement la carte en fonction du déplacement horizontal (`Modifier.graphicsLayer { rotationZ = ... }`).
  - Si la carte est glissée vers la droite au-delà d'un seuil (ex: > 300px) : déclenchez `viewModel.likeCurrentProfile()` (qui appelle `POST /api/v1/favorites/{id}`).
  - Si la carte est glissée vers la gauche : déclenchez `viewModel.passCurrentProfile()` pour passer au suivant.
  - Affichez un indicateur visuel translucide vert "LIKE" ou rouge "NOPE" sur la carte pendant le glissement.

### Exercice 3 : Écran des Favoris
- Ouvrez [FavoritesScreen.kt](file:///Users/donati/Projects/KoMeet/front/app/src/main/kotlin/fr/unica/miage/komeet/ui/favorites/FavoritesScreen.kt).
- Créez un `FavoritesViewModel` déclaré dans Koin.
- Appelez `profileRepository.getFavorites()` pour lister les profils aimés.
- Affichez-les sous forme de grille (`LazyVerticalGrid`) avec leurs photos, prénoms et villes.
- Permettez à l'utilisateur de retirer un favori via une icône corbeille appelant `profileRepository.removeFavorite(profileId)`.

### Exercice 4 : Fiche Profil Détaillée & Préférences (Estimation : 1h30)
1. **Fiche détaillée** ([ProfileDetailScreen.kt](file:///Users/donati/Projects/KoMeet/front/app/src/main/kotlin/fr/unica/miage/komeet/ui/profile/ProfileDetailScreen.kt)) :
   - Lorsqu'on clique sur une carte de profil, l'identifiant du profil est passé à `Screen.ProfileDetail(profileId)`.
   - Chargez les informations complètes du profil via `profileRepository.getProfile(profileId)`.
   - Affichez son adresse complète (rue, ville, code postal), son numéro de téléphone français, sa photo en pleine résolution, et son email.
2. **Préférences de recherche** ([PreferencesScreen.kt](file:///Users/donati/Projects/KoMeet/front/app/src/main/kotlin/fr/unica/miage/komeet/ui/preferences/PreferencesScreen.kt)) :
   - Consultez les préférences actuelles avec `profileRepository.getPreferences()`.
   - Permettez à l'utilisateur de modifier la tranche d'âge recherchée (`minAge`, `maxAge`) et le pays avec des composants Compose (`RangeSlider`, `OutlinedTextField`).
   - Sauvegardez avec `profileRepository.updatePreferences(...)`.

---

## 7. Tests Automatisés

Pour lancer les tests unitaires du frontend :
```bash
./gradlew test
```
Le test [LoginViewModelTest.kt](file:///Users/donati/Projects/KoMeet/front/app/src/test/kotlin/fr/unica/miage/komeet/ui/auth/LoginViewModelTest.kt) est déjà présent pour vous montrer comment tester un `ViewModel` sous coroutines avec un `FakeRepository`.
Vous êtes encouragés à ajouter des tests pour vos nouveaux ViewModels (`FavoritesViewModel`, `DiscoveryViewModel`, etc.).

---

## 8. Commandes Utiles

| Commande | Action |
| :--- | :--- |
| `./gradlew assembleDebug` | Compile le projet et génère l'APK (`app/build/outputs/apk/debug/app-debug.apk`) |
| `./gradlew installDebug` | Compile et installe l'application sur l'émulateur connecté |
| `./gradlew installDebug && adb shell am start -n fr.unica.miage.komeet/.MainActivity` | Compile, installe et lance l'application sur l'émulateur |
| `./gradlew uninstallAll` | Désinstalle l'application de tous les émulateurs / appareils connectés |
| `./gradlew test` | Exécute l'ensemble des tests unitaires |
| `./gradlew connectedAndroidTest` | Exécute les tests d'instrumentation sur l'émulateur |
| `./gradlew lint` | Exécute l'analyse statique du code (Android Lint) |
| `./gradlew clean` | Nettoie les caches et répertoires de build |

