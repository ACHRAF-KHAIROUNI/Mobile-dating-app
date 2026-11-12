# KoMeet - Backend API REST

Backend complet pour l'application Android **KoMeet**, une application de découverte de profils et de gestion de favoris.

Ce serveur est réalisé en **Kotlin / Ktor 3.x**, avec persistance **SQLite** via **Exposed ORM 2.x**, authentification par jetons **JWT**, import depuis l'API publique **Random User Generator**, et documentation **OpenAPI 3.1** / **Postman**.

---

## 1. Prérequis et Environnement

- **JDK 21 (LTS)** requis (OpenJDK 21, Temurin 21, Corretto 21 ou Oracle JDK 21).
- Le **Gradle Wrapper** (v8.12) est inclus dans le dépôt (`./gradlew`).

Vérifiez votre version de Java active :
```bash
java -version
```

Si votre environnement par défaut pointe vers une autre version, exportez `JAVA_HOME` vers votre installation de JDK 21 :
```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home # Exemple macOS
```

---

## 2. Configuration (`.env`)

Copiez le fichier d'exemple `.env.example` en `.env` :
```bash
cp .env.example .env
```

### Variables d'environnement supportées

| Variable | Description | Valeur par défaut |
| :--- | :--- | :--- |
| `PORT` | Port d'écoute du serveur HTTP | `8080` |
| `HOST` | Hôte d'écoute réseau | `0.0.0.0` |
| `DATABASE_PATH` | Chemin du fichier SQLite | `data/komeet.db` |
| `JWT_SECRET` | Clé secrète HMAC-SHA256 pour la signature JWT | `komeet-dev-secret-key-change-in-production-1234567890` |
| `JWT_ISSUER` | Émetteur des jetons JWT (`iss`) | `http://0.0.0.0:8080/` |
| `JWT_AUDIENCE` | Audience cible des jetons JWT (`aud`) | `komeet-api` |
| `JWT_EXPIRATION_HOURS` | Durée de validité des jetons en heures | `24` |
| `RANDOM_USER_API_URL` | URL de l'API Random User Generator | `https://randomuser.me/api/` |
| `RANDOM_USER_NATIONALITY` | Nationalité des profils importés (`fr` pour profils français) | `fr` |
| `RANDOM_USER_IMPORT_COUNT` | Nombre de profils à importer au premier démarrage | `50` |
| `RANDOM_USER_SEED` | Seed de génération pour imports reproductibles | `komeet` |
| `AUTO_IMPORT_ON_STARTUP` | Déclenche l'import si la base locale est vide | `true` |
| `CORS_ALLOWED_HOSTS` | Hôtes CORS autorisés (séparés par virgule, ou `*`) | `*` |

---

## 3. Compilation et Démarrage

### Compiler le projet
```bash
./gradlew build
```

### Lancer les tests automatisés
```bash
./gradlew test
```

### Lancer le serveur localement
```bash
./gradlew run
```

Le serveur sera disponible sur `http://localhost:8080` (et `http://0.0.0.0:8080`).

## 4. Points d'entrée de l'API (Endpoints REST)

Toutes les routes métier de l'API sont préfixées par `/api/v1` (à l'exception de la route de santé standard `/health` et des fichiers de spécification OpenAPI).
Les routes protégées nécessitent le header HTTP :
```http
Authorization: Bearer <accessToken>
```

### 4.1 Résumé des routes

| Méthode | Route | Authentification | Description | Statut succès |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/health` | Publique | État de santé du serveur et de SQLite | `200 OK` |
| `GET` | `/api/v1/health` | Publique | Alias versionné de santé | `200 OK` |
| `POST` | `/api/v1/auth/register` | Publique | Inscription d'un nouvel utilisateur | `201 Created` |
| `POST` | `/api/v1/auth/login` | Publique | Connexion utilisateur (retourne JWT) | `200 OK` |
| `POST` | `/api/v1/auth/logout` | Publique | Déconnexion (stateless) | `200 OK` |
| `GET` | `/api/v1/users/me` | **Bearer JWT** | Consultation du compte connecté | `200 OK` |
| `PATCH` | `/api/v1/users/me` | **Bearer JWT** | Mise à jour du profil (nom affiché) | `200 OK` |
| `DELETE` | `/api/v1/users/me` | **Bearer JWT** | Suppression du compte et des données liées | `200 OK` |
| `GET` | `/api/v1/profiles` | Publique | Liste paginée, filtrée et triée des profils | `200 OK` |
| `GET` | `/api/v1/profiles/{id}` | Publique | Détail complet d'un profil par identifiant | `200 OK` |
| `GET` | `/api/v1/favorites` | **Bearer JWT** | Liste paginée des favoris de l'utilisateur | `200 OK` |
| `POST` | `/api/v1/favorites/{profileId}` | **Bearer JWT** | Ajout d'un profil aux favoris | `201 Created` |
| `DELETE` | `/api/v1/favorites/{profileId}` | **Bearer JWT** | Retrait d'un profil des favoris | `200 OK` |
| `GET` | `/api/v1/preferences` | **Bearer JWT** | Consultation des préférences de recherche | `200 OK` |
| `PUT` | `/api/v1/preferences` | **Bearer JWT** | Définition / mise à jour des préférences | `200 OK` |
| `GET` | `/` | Publique | Message de bienvenue | `200 OK` |
| `GET` | `/docs/openapi.yaml` | Publique | Spécification OpenAPI 3.1 YAML | `200 OK` |
| `GET` | `/openapi.yaml` | Publique | Alias pour la spécification OpenAPI | `200 OK` |

### 4.2 Détail des paramètres et requêtes

#### Santé (`Health`)
- **`GET /health`** & **`GET /api/v1/health`**
  - **Réponse (200 OK)** : `{"status": "UP", "timestamp": "...", "database": "CONNECTED", "profilesCount": 50}`

#### Authentification (`Auth`)
- **`POST /api/v1/auth/register`**
  - **Corps JSON** : `{"email": "string", "password": "string (min 8 car.)", "displayName": "string"}`
  - **Réponse (201 Created)** : `{"user": {"id": "...", "email": "...", "displayName": "..."}, "accessToken": "..."}`
- **`POST /api/v1/auth/login`**
  - **Corps JSON** : `{"email": "string", "password": "string"}`
  - **Réponse (200 OK)** : `{"user": {"id": "...", "email": "...", "displayName": "..."}, "accessToken": "..."}`
- **`POST /api/v1/auth/logout`**
  - **Réponse (200 OK)** : `{"message": "Déconnexion réussie. Le client doit supprimer le jeton d'accès localement."}`

#### Utilisateur connecté (`Users`)
- **`GET /api/v1/users/me`**
  - **En-tête** : `Authorization: Bearer <accessToken>`
  - **Réponse (200 OK)** : `UserResponse` (`id`, `email`, `displayName`, `createdAt`, `updatedAt`)
- **`PATCH /api/v1/users/me`**
  - **En-tête** : `Authorization: Bearer <accessToken>`
  - **Corps JSON** : `{"displayName": "Nouveau Nom"}`
  - **Réponse (200 OK)** : `UserResponse` mis à jour
- **`DELETE /api/v1/users/me`**
  - **En-tête** : `Authorization: Bearer <accessToken>`
  - **Réponse (200 OK)** : `{"message": "Compte utilisateur supprimé avec succès."}` (supprime en cascade les favoris et préférences)

#### Profils (`Profiles`)
- **`GET /api/v1/profiles`**
  - **Paramètres d'URL (Query params)** :
    - `page` (défaut `1`) : numéro de la page
    - `pageSize` (défaut `20`, max `100`) : nombre d'éléments par page
    - `gender` : filtrer par genre (`"male"`, `"female"`)
    - `minAge`, `maxAge` : tranche d'âge minimale et maximale (entiers)
    - `department` : département français (ex. `"Alpes-Maritimes"`, `"Paris"`)
    - `country` : pays (ex. `"France"`)
    - `search` : recherche plein-texte sur prénom, nom, ville ou département
    - `sort` : champ de tri (`"name"`, `"age"`, `"createdAt"`)
    - `order` : ordre de tri (`"asc"` ou `"desc"`, défaut `"asc"`)
  - **Réponse (200 OK)** : `{"items": [...], "pagination": {"page": 1, "pageSize": 20, "totalCount": 50, "totalPages": 3}}`
- **`GET /api/v1/profiles/{id}`**
  - **Paramètre de chemin** : `{id}` identifiant du profil (ex: `"prof-123"`)
  - **Réponse (200 OK)** : Objet `ProfileResponse` complet (id, nom, prénom, email, âge, genre, téléphone, adresse, photos)
  - **Réponse (404 Not Found)** : Si aucun profil ne correspond à l'identifiant

#### Favoris (`Favorites`)
- **`GET /api/v1/favorites`**
  - **En-tête** : `Authorization: Bearer <accessToken>`
  - **Paramètres d'URL** : `page` (défaut `1`), `pageSize` (défaut `20`, max `100`)
  - **Réponse (200 OK)** : Liste paginée des profils favoris de l'utilisateur connecté
- **`POST /api/v1/favorites/{profileId}`**
  - **En-tête** : `Authorization: Bearer <accessToken>`
  - **Paramètre de chemin** : `{profileId}`
  - **Réponse (201 Created)** : `{"message": "Favori ajouté avec succès."}` (idempotent si déjà présent)
- **`DELETE /api/v1/favorites/{profileId}`**
  - **En-tête** : `Authorization: Bearer <accessToken>`
  - **Paramètre de chemin** : `{profileId}`
  - **Réponse (200 OK)** : `{"message": "Favori supprimé avec succès."}`

#### Préférences (`Preferences`)
- **`GET /api/v1/preferences`**
  - **En-tête** : `Authorization: Bearer <accessToken>`
  - **Réponse (200 OK)** : `{"userId": "...", "minAge": 18, "maxAge": 99, "country": "France", "updatedAt": "..."}`
- **`PUT /api/v1/preferences`**
  - **En-tête** : `Authorization: Bearer <accessToken>`
  - **Corps JSON** : `{"minAge": 22, "maxAge": 40, "country": "France"}`
  - **Réponse (200 OK)** : Objet de préférences mis à jour

---

## 5. Commandes cURL pour Tester Simplement l'API

Voici les commandes `curl` prêtes à l'emploi pour tester l'ensemble des fonctionnalités de l'API dans votre terminal.

### 1. Vérification de la santé du serveur
```bash
curl -i http://localhost:8080/health
```

### 2. Inscription d'un nouvel utilisateur
```bash
curl -i -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "Password123",
    "displayName": "Alice Dupont"
  }'
```

### 3. Connexion et sauvegarde automatique du jeton JWT
```bash
# Se connecter et exporter le jeton dans une variable $TOKEN de votre shell
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "Password123"
  }' | jq -r .accessToken)

echo "Mon jeton JWT : $TOKEN"
```

### 4. Consultation du compte connecté (`/users/me`)
```bash
curl -i http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer $TOKEN"
```

### 5. Mise à jour du nom affiché
```bash
curl -i -X PATCH http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"displayName": "Alice D."}'
```

### 6. Consultation et recherche de profils
```bash
# Lister les 5 premiers profils français
curl -s "http://localhost:8080/api/v1/profiles?page=1&pageSize=5" | jq .

# Filtrer par genre féminin et tranche d'âge [20, 40]
curl -s "http://localhost:8080/api/v1/profiles?gender=female&minAge=20&maxAge=40" | jq .

# Recherche textuelle et tri par âge décroissant
curl -s "http://localhost:8080/api/v1/profiles?search=Paris&sort=age&order=desc" | jq .
```

### 7. Détail d'un profil par ID
```bash
# Remplacer <PROFILE_ID> par un ID retourné précédemment
curl -s http://localhost:8080/api/v1/profiles/<PROFILE_ID> | jq .
```

### 8. Gestion des favoris
```bash
# Ajouter un profil aux favoris
curl -i -X POST http://localhost:8080/api/v1/favorites/<PROFILE_ID> \
  -H "Authorization: Bearer $TOKEN"

# Lister mes favoris
curl -s http://localhost:8080/api/v1/favorites \
  -H "Authorization: Bearer $TOKEN" | jq .

# Supprimer un favori
curl -i -X DELETE http://localhost:8080/api/v1/favorites/<PROFILE_ID> \
  -H "Authorization: Bearer $TOKEN"
```

### 9. Gestion des préférences utilisateur
```bash
# Consulter mes préférences actuelles
curl -s http://localhost:8080/api/v1/preferences \
  -H "Authorization: Bearer $TOKEN" | jq .

# Mettre à jour mes préférences
curl -i -X PUT http://localhost:8080/api/v1/preferences \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "minAge": 22,
    "maxAge": 35,
    "country": "France"
  }'
```

---

## 6. Connexion depuis une Application Android

### Émulateur Android officiel
L'émulateur standard Android tourne dans une machine virtuelle dédiée. L'adresse de votre machine hôte de développement est accessible via l'adresse IP spéciale **`10.0.2.2`**.

Dans votre client Retrofit ou Ktor Client Android :
```kotlin
val BASE_URL = "http://10.0.2.2:8080"
```

> **Attention pour Android 9+ (API 28+)** : Le trafic en clair `http://` nécessite d'autoriser le cleartext traffic dans votre `AndroidManifest.xml` :
> ```xml
> <application
>     android:usesCleartextTraffic="true"
>     ... >
> ```
> ou d'ajouter une configuration réseau spécifique dans `res/xml/network_security_config.xml`.

### Appareil physique ou émulateur tiers (Genymotion)
- **Genymotion** : l'hôte est généralement accessible sur `http://10.0.3.2:8080`.
- **Appareil physique (Wi-Fi)** : utilisez l'adresse IP locale de votre machine (ex: `http://192.168.1.XX:8080`) en veillant à être sur le même réseau Wi-Fi.

---

## 7. Import et Initialisation des Profils

Au premier démarrage, si `AUTO_IMPORT_ON_STARTUP=true` et que la base SQLite ne contient aucun profil, le serveur interroge automatiquement l'API **Random User Generator** avec le paramètre `nat=fr` (configurable via `RANDOM_USER_NATIONALITY`) pour importer un jeu initial de profils français (50 profils avec la seed `komeet`).

- **Champs stockés** : chaque profil conserve son identifiant, nom, prénom, email, âge, genre (`gender`: male/female), numéro de téléphone au format français (`phone`, ex. `06 12 34 56 78`), et un objet `location` complet comprenant le numéro de rue (`streetNumber`), le nom de la rue (`streetName`), la ville (`city`), le département français (`department`), le code postal (`postcode`), et le pays (`country: "France"`), ainsi que ses photos (miniature et haute définition).
- **Filtrage étendu** : l'endpoint `/api/v1/profiles` permet de filtrer la liste par genre (`?gender=female`), tranche d'âge (`?minAge=20&maxAge=40`), département (`?department=Alpes-Maritimes`) ou recherche textuelle (`?search=Nice`).
- **Idempotence** : Le relancement de l'import ou le redémarrage du serveur ne crée aucun doublon grâce aux identifiants stables et aux clauses d'upsert SQLite.
- **Résilience hors-ligne** : Si l'API externe est inaccessible mais que la base SQLite contient déjà des profils, le serveur continue de fonctionner normalement avec les données locales sans interrompre le service.

---

## 8. Architecture Technique 

Le projet adopte une architecture en couches nette et modulaire :

```
src/
  main/
    kotlin/fr/unica/miage/komeet/
      Application.kt               # Point d'entrée serveur et composition
      config/AppConfig.kt          # Chargement et validation de la configuration
      domain/
        model/                     # Modèles métier (User, Profile, Favorite, Preferences)
        repository/                # Contrats des dépôts indépendants du framework
        error/                     # Exceptions métier typées et codes d'erreur
      infrastructure/
        database/DatabaseFactory.kt# Exposed ORM, tables et création du schéma SQLite
        repository/                # Implémentations Exposed ORM des contrats de dépôt
        security/                  # BCryptPasswordHasher & JwtService
        randomuser/                # Client HTTP d'import Random User Generator
      application/                 # Services applicatifs et orchestration
      api/
        dto/                       # DTOs sérialisables JSON (aucun modèle DB exposé)
        health/                    # Point d'entrée /health et /api/v1/health
        auth/                      # Inscription, connexion, déconnexion
        users/                     # Consultation et mise à jour /users/me
        profiles/                  # Recherche, filtres, pagination des profils
        favorites/                 # Ajout, liste et suppression des favoris
        preferences/               # Gestion des critères de recherche utilisateur
      plugins/
        Serialization.kt           # Configuration JSON kotlinx.serialization
        Security.kt                # Configuration JWT Ktor
        StatusPages.kt             # Gestion centralisée des erreurs HTTP en JSON
        Routing.kt                 # Enregistrement des routes et CORS
    database/                  # Schéma déclaré en Kotlin avec Exposed
docs/
  openapi.yaml                     # Contrat d'API public OpenAPI 3.1
  postman/
    KoMeet.postman_collection.json # Scénarios Postman complets et automatisés
    KoMeet.postman_environment.json# Variables d'environnement Postman
```

### Format standard des erreurs JSON

Toutes les erreurs renvoyées par l'API respectent la structure :
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "La requête contient des données invalides",
    "details": []
  }
}
```

---

## 9. Documentation OpenAPI et Tests Postman

### Spécification OpenAPI 3.1
Le contrat d'API complet est versionné dans [docs/openapi.yaml](file:///Users/donati/Projects/KoMeet/back-exposed/docs/openapi.yaml).
Il est également servi directement par l'API sur :
- `GET /docs/openapi.yaml`
- `GET /openapi.yaml`

### Collection et Environnement Postman (YAML v3 & JSON)
Deux formats sont disponibles dans le projet :

#### 1. Espace de travail Postman v3 natif (`postman/`)
Le dossier racine `postman/` contient la structure Postman v3 (avec fichiers de requête `.request.yaml` et métadonnées `.resources/definition.yaml`) :
- **Dans l'application Postman Desktop** : ouvrez ce dépôt en tant que Workspace local, la collection **KoMeet API** et l'environnement **KoMeet Local Environment** apparaissent directement dans la barre latérale.
- **En ligne de commande avec la Postman CLI** :
  ```bash
  postman collection run "postman/collections/KoMeet API" -e "postman/environments/KoMeet Local Environment.environment.yaml"
  ```

#### 2. Fichiers autonomes (`docs/postman/`)
Pour l'import direct (via le bouton **Import** de Postman) :
- [KoMeet.postman_collection.yaml](file:///Users/donati/Projects/KoMeet/back-exposed/docs/postman/KoMeet.postman_collection.yaml) & [KoMeet.postman_collection.json](file:///Users/donati/Projects/KoMeet/back-exposed/docs/postman/KoMeet.postman_collection.json)
- [KoMeet.postman_environment.yaml](file:///Users/donati/Projects/KoMeet/back-exposed/docs/postman/KoMeet.postman_environment.yaml) & [KoMeet.postman_environment.json](file:///Users/donati/Projects/KoMeet/back-exposed/docs/postman/KoMeet.postman_environment.json)

Exécution autonome via CLI :
```bash
postman collection run docs/postman/KoMeet.postman_collection.yaml -e docs/postman/KoMeet.postman_environment.yaml
```

#### Gestion automatique du jeton de session JWT
La collection utilise l'authentification **Bearer JWT au niveau de la collection** :
1. Dès l'exécution de la requête `Login User` (ou `Register User`), le script de test extrait le champ `accessToken` de la réponse et le stocke automatiquement dans les variables d'environnement et de collection :
   ```javascript
   const response = pm.response.json();
   if (response.accessToken) {
       pm.environment.set("accessToken", response.accessToken);
       pm.collectionVariables.set("accessToken", response.accessToken);
   }
   ```
2. Toutes les requêtes nécessitant une authentification (`/users/me`, `/favorites`, `/preferences`) héritent directement de ce jeton sans configuration manuelle d'en-tête.
3. Les requêtes publiques (`/health`, `/auth/*`, `/profiles/*`) sont explicitement configurées en `auth: noauth`.

**Pour les utiliser dans Postman Desktop :**
1. Importez les fichiers ou ouvrez le dossier en tant que Workspace local.
2. Sélectionnez l'environnement **KoMeet Local Environment**.
3. Exécutez `2. Authentication > Login User` : le jeton JWT est immédiatement sauvegardé et actif pour toutes les requêtes protégées.
4. Lancez le **Collection Runner** pour exécuter les 20 tests automatisés d'un coup (38 assertions réussies).

---

## 10. Limites Connues et Sécurité

- **Déconnexion stateless (JWT)** : L'authentification utilise des jetons signés stateless. La déconnexion côté client consiste à supprimer le jeton stocké localement. Le jeton émis reste cryptographiquement valide jusqu'à son expiration (par défaut 24h). Aucune liste de révocation en mémoire ou en base n'a été ajoutée afin de maintenir la simplicité de l'architecture pédagogique.
- **Mots de passe** : Les mots de passe sont hachés avec **BCrypt** (coût 12 en production, coût 4 en test). Le hachage n'est jamais divulgué dans les réponses API ou dans les logs.

---

