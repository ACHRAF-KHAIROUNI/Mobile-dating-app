# 📱 KoMeet Android - Application de Rencontre Tinder-Like avec IA Gemini

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Platform-Android%208.0%2B-green.svg?style=flat&logo=android)](https://android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material%203-7B1FA2.svg?style=flat)](https://m3.material.io)
[![Koin](https://img.shields.io/badge/DI-Koin%204.0-brightgreen.svg)](https://insert-koin.io/)
[![Ktor Client](https://img.shields.io/badge/Networking-Ktor%20Client%203-087CFA.svg?style=flat&logo=ktor)](https://ktor.io)

Application mobile Android **KoMeet**, une expérience moderne de rencontre amoureuse de type **Tinder / Bumble**.
Développée avec les dernières technologies Android (Jetpack Compose, Navigation 3, Material 3, Koin 4 et Ktor Client 3), elle offre une interface graphique soignée, un swiper interactif, un filtrage multi-pays et une **messagerie instantanée connectée à un Chatbot IA propulsé par Google Gemini 3.1 Flash-Lite**.

---

## 👨‍💻 Auteur & Développeur

- **Développeur :** **ACHRAF KHAIROUNI**
- **Portfolio :** [achraf.khairouni.info](https://achraf.khairouni.info)
- **Dépôt GitHub du projet :** [https://github.com/ACHRAF-KHAIROUNI/Mobile-dating-app.git](https://github.com/ACHRAF-KHAIROUNI/Mobile-dating-app.git)

---

## 🎬 Démonstration Vidéo YouTube

Découvrez la démonstration complète de l'application en vidéo :

[![KoMeet - Démo Vidéo YouTube](https://img.youtube.com/vi/x6MHFEI0s74/maxresdefault.jpg)](https://youtu.be/x6MHFEI0s74)

🔗 **Lien direct :** [https://youtu.be/x6MHFEI0s74](https://youtu.be/x6MHFEI0s74)

---

## 🎨 Écrans et Expérience Utilisateur

### 1. 🎴 Écran Découverte (`DiscoveryScreen`)
- **Swiper de cartes de profils** :
  - Détection tactile fluide (`pointerInput`, `detectDragGestures`) avec rotation angulaire progressive et retour élastique (*spring animation*).
  - Badges visuels dynamiques **LIKE** vert ou **NOPE** rouge s'intensifiant selon la direction du glissement.
- **Carte Profil Ultra-Design** :
  - Courbure élégante à `28.dp` avec ombre portée (`10.dp`).
  - Badge de compatibilité : `✨ 98% Match` en verre dépoli.
  - Badge officiel Vérifié bleu (`✓`) à côté du nom et de l'âge.
  - Badges de passions incrustés (`✈️ Voyages`, `☕ Café`, `🎵 Musique`, `📸 Photo`...).
  - Bouton circulaire d'information pour basculer vers la vue détaillée.
- **Barre d'action Tinder à 4 boutons** :
  - ❌ **Pass** (58.dp, contour rouge vif `PassRed`).
  - 🔄 **Recharger** (46.dp, actualisation de la pile).
  - 💬 **Discuter** (56.dp, **dégradé flamboyant Tinder Flame**, icône blanche, ombre 3D).
  - 💚 **Like** (64.dp, contour vert `LikeGreen`).
- **Filtres de recherche intuitifs** :
  - Rangée de genres : **✨ Tous**, **👩 Filles**, **👨 Garçons**.
  - Rangée de pays défilante : **🌍 Tous les pays**, **🇲🇦 Maroc**, **🇫🇷 France**, **🇺🇸 USA**, **🇯🇵 Japon**, **🇸🇦 Arabie Saoudite**.
  - Badge de région active avec bouton de suppression rapide.

### 2. 💬 Écran des Discussions (`ConversationsScreen`)
- **Carrousel "Matchs récents" (Style Stories)** :
  - Rangée horizontale d'avatars avec bordure dégradée éclatante (`TinderFlame` à `TinderPeach`).
  - Toucher n'importe quel profil ouvre directement le chat.
- **Liste des conversations actives** :
  - Affiche chaque personne avec qui vous avez conversé.
  - Photo de profil avec pastille verte "En ligne".
  - Nom, âge, drapeau et ville/région.
  - Aperçu du dernier message (avec préfixe *"Vous : ..."* si vous êtes l'émetteur).
- **Vue vide engageante** : Illustration soignée invitant à découvrir des profils.

### 3. 💌 Écran de Chat IA (`ChatScreen`)
- **Simulation de Persona par Gemini 3.1 Flash-Lite** :
  - Le profil adopte son identité, son âge, sa ville, sa région et son pays, tout en manifestant un intérêt sincère pour planifier une rencontre.
- **Bulles de messages expressives** :
  - Dégradé Tinder (`#FD297B` vers `#FF5864`) pour les messages de l'utilisateur.
  - Bulles modernes contrastées avec avatar pour les réponses du profil.
- **Barre de suggestions rapides** :
  - Puces cliquables (*"Coucou ! 😊"*, *"Tu viens d'où ? 🌍"*, *"Un café ? ☕"*) pour engager la conversation instantanément.
- **Indicateur de frappe animé** : Animation pulsante douce *"✨ [Prénom] est en train d'écrire..."*.
- **En-tête de chat** : Avatar avec bordure, pastille verte et sous-titre *"En ligne • Répond en quelques sec"*.
- **Bouton Corbeille** : Effacement et réinitialisation de l'historique en un clic.

### 4. ❤️ Écran des Favoris (`FavoritesScreen`)
- Grille responsive à 2 colonnes avec cartes adoucies (`22.dp`).
- Badge coeur en haut à gauche, nom, âge, drapeau et ville.
- Bouton de suppression rapide et bouton flottant de discussion directe en dégradé.

### 5. 🔍 Détail du Profil (`ProfileDetailScreen`)
- Grande photo de profil plein écran avec dégradé d'assombrissement.
- Nom, âge, drapeau du pays et région.
- Badges de caractéristiques (Genre, Âge, Pays).
- Section dédiée *"Centres d'intérêt"* avec puces stylisées.
- Cartes d'adresses et de coordonnées.
- Dock flottant d'actions : bouton Like / Favoris et bouton Discuter.

### 6. ⚙️ Préférences & Paramètres (`PreferencesScreen`)
- Curseurs interactifs pour ajuster la tranche d'âge de recherche.
- Sélecteur de pays et de région cible.
- Section Conditions Générales d'Utilisation (CGU).
- **Mention officielle de l'auteur : ACHRAF KHAIROUNI**.
- Déconnexion sécurisée nettoyant le jeton JWT du DataStore.

---

## 🏗️ Architecture Logicielle

Le projet respecte rigoureusement les principes de la **Clean Architecture** et du pattern **MVVM (Model-View-ViewModel)** :

```
front/app/src/main/kotlin/fr/unica/miage/komeet/
├── KoMeetApplication.kt        # Initialisation de Koin au démarrage
├── MainActivity.kt             # Point d'entrée de l'application
│
├── data/
│   ├── local/                  # Stockage persistant DataStore (Session, Tokens)
│   ├── remote/                 # Client Ktor 3, DTOs (@Serializable), KoMeetRemoteSource
│   └── repository/             # Dépôts de données (Auth, Profile, Chat)
│
├── di/
│   └── AppModule.kt            # Modules d'injection de dépendances Koin 4
│
├── navigation/
│   ├── KoMeetNavHost.kt        # Hôte NavDisplay avec Animated Transitions
│   └── Screen.kt               # Destinations scellées type-safe (@Serializable sealed interface Screen : NavKey)
│
└── ui/
    ├── auth/                   # Login & Register
    ├── chat/                   # ConversationsScreen, ChatScreen & ViewModels
    ├── discovery/              # DiscoveryScreen, Tinder Swiper & ProfileCard
    ├── favorites/              # FavoritesScreen & FavoriteCard
    ├── preferences/            # PreferencesScreen
    ├── profile/                # ProfileDetailScreen
    └── theme/                  # Couleurs Tinder Flame, Typographies, Thème sombre/clair
```

---

## 🛠️ Installation et Exécution

### 1. Prérequis
- **Android Studio** (Koala / Ladybug ou version récente)
- **JDK 21 (LTS)**
- Un **émulateur Android** (API 26 ou supérieure)

### 2. Démarrer le Backend au préalable
Dans un terminal séparé, lancez le serveur backend :
```bash
cd ../back
# Windows :
.\gradlew.bat run
# macOS / Linux :
./gradlew run
```
*Le backend sera accessible depuis l'émulateur via l'alias `http://10.0.2.2:8080/api/v1/`.*

### 3. Compiler et Exécuter le Frontend

#### Via Android Studio (Recommandé) :
1. Ouvrez le dossier `front` dans Android Studio.
2. Synchronisez le projet avec les fichiers Gradle (`Sync Project with Gradle Files`).
3. Sélectionnez votre émulateur dans la barre d'outils.
4. Cliquez sur **Run ▶** (`Shift + F10`).

#### En Ligne de Commande :
```bash
# Compiler l'APK de debug
.\gradlew.bat assembleDebug

# Installer l'application sur l'émulateur connecté
.\gradlew.bat installDebug

# Lancer l'application
adb shell am start -n fr.unica.miage.komeet/.MainActivity
```

#### Exécuter les Tests Unitaires :
```bash
.\gradlew.bat testDebugUnitTest
```

---

> Conçu et développé avec passion par **[ACHRAF KHAIROUNI](https://achraf.khairouni.info)**.
