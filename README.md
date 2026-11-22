# 🔥 KoMeet - Application Mobile de Rencontre Tinder-Like avec Chatbot IA (Gemini)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg?style=flat&logo=android)](https://android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Ktor](https://img.shields.io/badge/Backend-Ktor%203.x-087CFA.svg?style=flat&logo=ktor)](https://ktor.io)
[![Gemini 3.1 Flash-Lite](https://img.shields.io/badge/AI-Google%20Gemini%203.1%20Flash--Lite-blue.svg?style=flat&logo=google)](https://deepmind.google/technologies/gemini/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> **KoMeet** est une application mobile moderne de rencontre inspirée de **Tinder / Bumble**, développée en **Kotlin** avec une architecture complète **Full-Stack (Backend Ktor + Frontend Jetpack Compose)**.
> Elle intègre un **Chatbot IA conversationnel révolutionnaire propulsé par Google Gemini 3.1 Flash-Lite**, capable d'incarner fidèlement chaque profil selon son identité, sa région et sa culture pour simuler une véritable rencontre amoureuse.

---

## 👨‍💻 Auteur & Développeur

- **Créateur :** **ACHRAF KHAIROUNI**
- **Portfolio :** [achraf.khairouni.info](https://achraf.khairouni.info)
- **Dépôt GitHub :** [https://github.com/ACHRAF-KHAIROUNI/Mobile-dating-app.git](https://github.com/ACHRAF-KHAIROUNI/Mobile-dating-app.git)

---

## 🎬 Démonstration Vidéo YouTube

Découvrez l'application en action sur notre vidéo de démonstration complète (Swiping Tinder, Chatbot IA Gemini en direct, filtrage par pays et gestion des favoris) :

[![KoMeet - Démo Vidéo YouTube](https://img.youtube.com/vi/x6MHFEI0s74/maxresdefault.jpg)](https://youtu.be/x6MHFEI0s74)

🔗 **Lien direct vers la vidéo :** [https://youtu.be/x6MHFEI0s74](https://youtu.be/x6MHFEI0s74)

---

## ✨ Fonctionnalités Principales

### 1. 🎴 Découverte & Swiper Façon Tinder
- **Gestes fluides de Swipe** : Glissez la carte à droite pour **Liker (💚)** ou à gauche pour **Passer (❌)** avec animations dynamiques et badges réactifs (*LIKE* vert / *NOPE* rouge).
- **Design de carte Ultra-Premium** :
  - Badge de compatibilité en verre dépoli (`✨ 98% Match`).
  - Badge officiel Vérifié bleu (`✓`) à côté du nom et de l'âge.
  - Puces de centres d'intérêt incrustées (`✈️ Voyages`, `☕ Café`, `🎵 Musique`, `📸 Photo`...).
  - Bouton circulaire d'information pour consulter la fiche complète.
- **Dock d'actions rapides Tinder** : Boutons tactiles avec ombres 3D pour Passer, Recharger, Discuter en un tap et Liker.

### 2. 🌍 Filtrage Avancé & Multi-Régions International
- **Filtres de Genre Dédiés** : Sélection indépendante entre **Tous**, **👩 Filles** et **👨 Garçons**.
- **Couverture Internationale Multi-Pays** :
  - 🌍 **Tous les pays** (exploration mondiale aléatoire).
  - 🇲🇦 **Maroc** (Casablanca-Settat, Marrakech-Safi, Tanger-Tétouan, Rabat-Salé-Kénitra, Fès-Meknès...).
  - 🇫🇷 **France** (Île-de-France, Auvergne-Rhône-Alpes, Provence-Alpes-Côte d'Azur, etc.).
  - 🇺🇸 **USA** (California, New York, Texas, Florida, Washington...).
  - 🇯🇵 **Japon** (Tokyo, Osaka, Kyoto, Hokkaido, Fukuoka...).
  - 🇸🇦 **Arabie Saoudite** (Riyadh, Makkah, Eastern Province, Asir...).
- **Indicateur de région active** avec possibilité de réinitialisation en un clic.

### 3. 🤖 Chatbot IA Dédié avec Persona (Google Gemini 3.1 Flash-Lite)
- **Incarnation ultra-réaliste du profil** :
  - L'IA adopte rigoureusement l'identité de la personne : son prénom, nom, âge, genre, ville, région et pays d'origine.
  - Elle répond avec chaleur, bienveillance et intérêt romantique, cherchant naturellement à poursuivre la discussion pour organiser un rendez-vous dans le monde réel.
  - Elle intègre des spécificités culturelles et géographiques réelles (ex: mentionner les montagnes d'Abha pour un profil de la région d'Asir en Arabie Saoudite).
- **Architecture 100% sécurisée** : Toute l'intégration Gemini s'effectue exclusivement côté backend. La clé API ne transite jamais vers le client mobile.
- **Historique persistant en base SQLite** : Chaque échange est archivé et rechargé automatiquement.

### 4. 💬 Espace Dédié aux Discussions (Conversations)
- **Carrousel "Matchs récents" façon Stories** : Rangée horizontale d'avatars avec anneau dégradé flamboyant (Tinder Flame / Coral) pour démarrer une discussion d'un simple toucher.
- **Liste des conversations actives** : Affiche le dernier message échangé (avec distinction *"Vous : ..."*), l'horodatage et la pastille verte "En ligne".
- **Interface de Chat Moderne** :
  - Bulles de messages au dégradé Tinder vibrant.
  - Puces de suggestions rapides (*"Coucou ! 😊"*, *"Tu viens d'où ? 🌍"*, *"On prend un café ? ☕"*) pour briser la glace immédiatement.
  - Indicateur de frappe animé : *"✨ [Prénom] est en train d'écrire..."*.
  - Option corbeille pour effacer l'historique et repartir à zéro.

### 5. ❤️ Favoris & Préférences de Recherche
- **Écran des Favoris** : Grille élégante en 2 colonnes avec accès direct au chat et suppression rapide.
- **Préférences de Recherche** : Réglage de la tranche d'âge cible et du pays / région de prédilection.
- **Page Paramètres & CGU** : Conditions Générales d'Utilisation et attribution officielle au créateur **ACHRAF KHAIROUNI**.

---

## 🏛️ Architecture Technique

```
Mobile-dating-app/
├── back/                           # Serveur API REST (Kotlin / Ktor 3)
│   ├── src/main/kotlin/
│   │   └── fr/unica/miage/komeet/
│   │       ├── api/                # Routes Ktor (Auth, Profiles, Favorites, Preferences, Chat)
│   │       ├── application/        # Logique métier & Services (ChatService, AuthService...)
│   │       ├── domain/             # Modèles du domaine (User, Profile, ChatMessage, Region)
│   │       └── infrastructure/     # Exposed ORM, SQLite, Client Gemini, RandomUser
│   ├── data/komeet.db              # Base de données SQLite locale persistante
│   └── .env                        # Variables d'environnement (Clé Gemini, JWT, Ports...)
│
└── front/                          # Application Android (Kotlin / Jetpack Compose)
    └── app/src/main/kotlin/
        └── fr/unica/miage/komeet/
            ├── data/               # Sources distantes (Ktor Client 3) & Repositories
            ├── di/                 # Injection de dépendances Koin 4
            ├── navigation/         # Navigation 3 (Nav3) type-safe
            └── ui/                 # Écrans Compose & ViewModels (Discovery, Chat, Favorites...)
```

### Stack Backend :
- **Langage :** Kotlin 2.1
- **Framework Web :** Ktor 3.x (Netty Engine)
- **Persistance :** Exposed ORM 2.x + Base de données SQLite
- **Intelligence Artificielle :** Google Gemini API (`gemini-3.1-flash-lite`) via client HTTP Ktor CIO
- **Sécurité :** Authentification JWT (HMAC-SHA256) + Hachage de mot de passe BCrypt
- **Configuration :** Dotenv (`.env`)

### Stack Frontend Android :
- **Langage :** Kotlin 2.1
- **Framework UI :** Jetpack Compose + Material 3
- **Navigation :** Android Navigation 3 (Nav3) avec `rememberNavBackStack`
- **Injection de dépendances :** Koin 4.x
- **Réseau :** Ktor Client 3.x (OkHttp Engine avec gestion des timeouts étendus à 60s)
- **Chargement d'images :** Coil 2.7
- **Stockage local :** Jetpack DataStore Preferences

---

## 🚀 Guide de Démarrage Rapide

### Prérequis
- **JDK 21 (LTS)**
- **Android Studio** (avec un émulateur Android configuré, API 26+)
- **Git**

---

### 1. Cloner le Projet
```bash
git clone https://github.com/ACHRAF-KHAIROUNI/Mobile-dating-app.git
cd Mobile-dating-app
```

---

### 2. Lancer le Backend
1. Rendez-vous dans le dossier `back` :
   ```bash
   cd back
   ```
2. Vérifiez que votre fichier `.env` est bien présent (déjà configuré par défaut avec la clé Gemini et la configuration SQLite) :
   ```properties
   PORT=8080
   HOST=0.0.0.0
   DATABASE_PATH=data/komeet.db
   JWT_SECRET=komeet-dev-secret-key-change-in-production-1234567890
   GEMINI_API_KEY=your_gemini_api_key_here
   GEMINI_MODEL=gemini-3.1-flash-lite
   ```
3. Démarrez le serveur :
   - **Sous Windows :**
     ```powershell
     .\gradlew.bat run
     ```
   - **Sous macOS / Linux :**
     ```bash
     ./gradlew run
     ```
   Le serveur démarre sur `http://localhost:8080` (et `http://10.0.2.2:8080` pour l'émulateur Android).

---

### 3. Lancer l'Application Android
1. Ouvrez le dossier `front` dans **Android Studio**.
2. Laissez Gradle synchroniser le projet (`Sync Project with Gradle Files`).
3. Lancez votre émulateur Android.
4. Cliquez sur le bouton **Run ▶** (`Shift + F10`) pour installer et exécuter l'application sur l'émulateur.

*Alternative en ligne de commande :*
```powershell
cd front
.\gradlew.bat installDebug
adb shell am start -n fr.unica.miage.komeet/.MainActivity
```

---

## 📄 Licence

Ce projet est sous licence MIT - voir le fichier [LICENSE](LICENSE) pour plus d'informations.

---

> Réalisé avec passion par **[ACHRAF KHAIROUNI](https://achraf.khairouni.info)**.
