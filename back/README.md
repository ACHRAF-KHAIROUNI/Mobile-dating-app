# 🚀 KoMeet - Backend API REST & Moteur IA Gemini

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Ktor](https://img.shields.io/badge/Ktor-3.x-087CFA.svg?style=flat&logo=ktor)](https://ktor.io)
[![SQLite](https://img.shields.io/badge/SQLite-3-003B57.svg?style=flat&logo=sqlite)](https://sqlite.org)
[![Exposed](https://img.shields.io/badge/ORM-Exposed%200.58-E51F24.svg)](https://github.com/JetBrains/Exposed)
[![Gemini](https://img.shields.io/badge/AI-Gemini%203.1%20Flash--Lite-4285F4.svg?style=flat&logo=google)](https://deepmind.google/technologies/gemini/)

Backend complet et performant pour l'application mobile de rencontre **KoMeet**.
Ce serveur est développé en **Kotlin / Ktor 3.x**, avec persistance **SQLite** via **Exposed ORM**, authentification **JWT (HMAC-SHA256)**, import de profils multi-pays, gestion de régions géographiques et intégration du **moteur d'IA conversationnelle Google Gemini 3.1 Flash-Lite** simulant le persona de chaque profil.

---

## 👨‍💻 Auteur & Développeur

- **Développeur :** **ACHRAF KHAIROUNI**
- **Portfolio :** [achraf.khairouni.info](https://achraf.khairouni.info)
- **Dépôt GitHub du projet :** [https://github.com/ACHRAF-KHAIROUNI/Mobile-dating-app.git](https://github.com/ACHRAF-KHAIROUNI/Mobile-dating-app.git)

---

## 🎬 Démonstration Vidéo YouTube

[![KoMeet - Démo Vidéo YouTube](https://img.youtube.com/vi/x6MHFEI0s74/maxresdefault.jpg)](https://youtu.be/x6MHFEI0s74)

🔗 **Lien de la vidéo :** [https://youtu.be/x6MHFEI0s74](https://youtu.be/x6MHFEI0s74)

---

## 🧠 Fonctionnalités Avancées du Backend

### 1. Moteur d'IA Conversationnelle Gemini 3.1 Flash-Lite
- **Architecture Sécurisée** : L'API Gemini n'est appelée que par le backend via `GeminiChatClient.kt`. Aucune clé ni logique sensible n'est exposée sur le mobile.
- **Simulation de Persona Dynamique** :
  - Lorsqu'un utilisateur écrit à un profil, le `ChatService` génère une consigne système (*system instruction*) sur mesure : nom, prénom, âge, genre, ville, région et pays du profil.
  - L'IA adopte un ton naturel, complice, chaleureux et séduisant, en incitant activement à prolonger la discussion pour planifier une rencontre réelle.
- **Historique & Contexte de Conversation** :
  - Tous les messages sont persistés dans la table SQLite `ChatMessages` avec horodatage `Instant`.
  - L'historique des échanges précédents est transmis à l'API Gemini pour maintenir la continuité narrative de la discussion.
- **Gestion des Timeouts Réseau** : Configuration étendue à **60 secondes** (`requestTimeoutMillis = 60000L`) pour absorber sans interruption les temps de génération du modèle LLM.

### 2. Multi-Régions & Profils Internationaux
- **Table des Régions (`Regions`)** : Indexation de 36 régions officielles réparties sur 5 pays :
  - 🇲🇦 **Maroc** (Casablanca-Settat, Marrakech-Safi, Tanger-Tétouan, Rabat-Salé-Kénitra, Fès-Meknès...).
  - 🇫🇷 **France** (Île-de-France, Auvergne-Rhône-Alpes, Provence-Alpes-Côte d'Azur...).
  - 🇺🇸 **USA** (California, New York, Texas, Florida, Washington...).
  - 🇯🇵 **Japon** (Tokyo, Osaka, Kyoto, Hokkaido, Fukuoka...).
  - 🇸🇦 **Arabie Saoudite** (Riyadh, Makkah, Eastern Province, Asir...).
- **Seeder Automatique (`InitialDataSeeder.kt`)** : Peuplement initial garanti de 72 profils internationaux qualifiés répartis dans chacune de ces régions avec photos haute définition.

### 3. Gestion des Utilisateurs, Favoris & Préférences
- **Authentification JWT** avec mot de passe chiffré par **BCrypt**.
- **Gestion des Favoris** avec contrainte d'unicité `(userId, profileId)` et endpoints paginés.
- **Préférences de Recherche** personnalisables par utilisateur (tranche d'âge, pays ciblé, région).

---

## 📋 Endpoints de l'API REST (`/api/v1`)

### 1. Authentification & Utilisateurs

| Méthode | Route | Accès | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Public | Inscription (nom, email, mot de passe, région) |
| `POST` | `/api/v1/auth/login` | Public | Connexion utilisateur (retourne le token JWT) |
| `POST` | `/api/v1/auth/logout` | Public | Déconnexion |
| `GET` | `/api/v1/users/me` | **Bearer JWT** | Informations sur l'utilisateur connecté |
| `PATCH` | `/api/v1/users/me` | **Bearer JWT** | Mise à jour du profil |
| `DELETE` | `/api/v1/users/me` | **Bearer JWT** | Suppression du compte et des données liées |

### 2. Profils & Régions

| Méthode | Route | Accès | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/profiles` | Public | Liste paginée, filtrée par genre, pays, région et âge |
| `GET` | `/api/v1/profiles/{id}` | Public | Consultation détaillée d'un profil par identifiant |
| `GET` | `/api/v1/regions` | Public | Liste des régions officielles (filtrable par pays) |

### 3. Favoris & Préférences

| Méthode | Route | Accès | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/favorites` | **Bearer JWT** | Liste paginée des profils mis en favoris |
| `POST` | `/api/v1/favorites/{profileId}` | **Bearer JWT** | Ajouter un profil aux favoris |
| `DELETE` | `/api/v1/favorites/{profileId}` | **Bearer JWT** | Retirer un profil des favoris |
| `GET` | `/api/v1/preferences` | **Bearer JWT** | Obtenir ses préférences de découverte |
| `PUT` | `/api/v1/preferences` | **Bearer JWT** | Mettre à jour ses critères de recherche |

### 4. Chatbot IA Gemini (`/api/v1/chat`)

| Méthode | Route | Accès | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/chat/conversations` | **Bearer JWT** | Liste ordonnée de tous les profils avec lesquels l'utilisateur a conversé (avec dernier message) |
| `GET` | `/api/v1/chat/{profileId}/messages` | **Bearer JWT** | Historique complet de la discussion avec un profil spécifique |
| `POST` | `/api/v1/chat/{profileId}/messages` | **Bearer JWT** | Envoi d'un message utilisateur et réception de la réponse du persona IA |
| `DELETE` | `/api/v1/chat/{profileId}/messages` | **Bearer JWT** | Effacer l'historique de discussion avec ce profil |

---

## ⚙️ Configuration (`.env`)

Le backend charge sa configuration automatiquement via `dotenv` depuis le fichier `.env` :

```properties
# Configuration du serveur KoMeet
PORT=8080
HOST=0.0.0.0

# Base de données SQLite
DATABASE_PATH=data/komeet.db

# Authentification JWT
JWT_SECRET=komeet-dev-secret-key-change-in-production-1234567890
JWT_ISSUER=http://0.0.0.0:8080/
JWT_AUDIENCE=komeet-api
JWT_EXPIRATION_HOURS=24

# Configuration Gemini IA Chatbot
GEMINI_API_KEY=your_gemini_api_key_here
GEMINI_MODEL=gemini-3.1-flash-lite

# Import Random User
RANDOM_USER_API_URL=https://randomuser.me/api/
RANDOM_USER_NATIONALITY=fr
RANDOM_USER_IMPORT_COUNT=50
RANDOM_USER_SEED=komeet
AUTO_IMPORT_ON_STARTUP=true

# CORS
CORS_ALLOWED_HOSTS=*
```

---

## 🛠️ Démarrage et Tests

### 1. Prérequis
- **JDK 21 (LTS)**

### 2. Lancer les tests unitaires et d'intégration
```bash
# Windows
.\gradlew.bat test

# macOS / Linux
./gradlew test
```

### 3. Démarrer le serveur
```bash
# Windows
.\gradlew.bat run

# macOS / Linux
./gradlew run
```

Le serveur sera actif sur :
- Local : `http://localhost:8080`
- Émulateur Android : `http://10.0.2.2:8080`
- Santé de l'API : `GET http://localhost:8080/health`

---

> Conçu et développé par **[ACHRAF KHAIROUNI](https://achraf.khairouni.info)**.
