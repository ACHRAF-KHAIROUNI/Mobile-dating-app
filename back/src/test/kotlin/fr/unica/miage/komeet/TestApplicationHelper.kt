package fr.unica.miage.komeet

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import fr.unica.miage.komeet.application.auth.AuthService
import fr.unica.miage.komeet.application.favorites.FavoriteService
import fr.unica.miage.komeet.application.preferences.PreferencesService
import fr.unica.miage.komeet.application.profiles.ProfileService
import fr.unica.miage.komeet.application.users.UserService
import fr.unica.miage.komeet.config.AppConfig
import fr.unica.miage.komeet.infrastructure.database.DatabaseFactory
import fr.unica.miage.komeet.domain.model.Profile
import fr.unica.miage.komeet.infrastructure.randomuser.RandomUserDataSource
import fr.unica.miage.komeet.infrastructure.repository.ExposedFavoriteRepository
import fr.unica.miage.komeet.infrastructure.repository.ExposedImportMetadataRepository
import fr.unica.miage.komeet.infrastructure.repository.ExposedPreferencesRepository
import fr.unica.miage.komeet.infrastructure.repository.ExposedProfileRepository
import fr.unica.miage.komeet.infrastructure.repository.ExposedUserRepository
import fr.unica.miage.komeet.infrastructure.security.BCryptPasswordHasher
import fr.unica.miage.komeet.infrastructure.security.JwtService
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.Properties

class MockRandomUserDataSource(
    var mockProfiles: List<Profile> = emptyList(),
    var shouldFail: Boolean = false
) : RandomUserDataSource {
    override suspend fun fetchProfiles(count: Int, seed: String?, nationality: String?): List<Profile> {
        if (shouldFail) {
            throw RuntimeException("Simulation d'erreur réseau Random User Generator")
        }
        return mockProfiles.take(count)
    }
}

class TestContext(
    val config: AppConfig,
    val database: Database,
    val userRepository: ExposedUserRepository,
    val profileRepository: ExposedProfileRepository,
    val favoriteRepository: ExposedFavoriteRepository,
    val preferencesRepository: ExposedPreferencesRepository,
    val importMetadataRepository: ExposedImportMetadataRepository,
    val passwordHasher: BCryptPasswordHasher,
    val jwtService: JwtService,
    val mockRandomUserDataSource: MockRandomUserDataSource,
    val authService: AuthService,
    val userService: UserService,
    val profileService: ProfileService,
    val favoriteService: FavoriteService,
    val preferencesService: PreferencesService
) {
    companion object {
        fun create(mockProfiles: List<Profile> = emptyList()): TestContext {
            val database = DatabaseFactory.connect(":memory:")

            val config = AppConfig(
                port = 8080,
                host = "0.0.0.0",
                databasePath = ":memory:",
                jwtSecret = "test-secret-key-for-testing-at-least-32-chars-long",
                jwtIssuer = "http://test-issuer/",
                jwtAudience = "test-audience",
                jwtExpirationHours = 1L,
                randomUserApiUrl = "https://randomuser.me/api/",
                randomUserNationality = "fr",
                randomUserImportCount = 10,
                randomUserSeed = "test",
                autoImportOnStartup = false,
                corsAllowedHosts = listOf("*")
            )

            val userRepository = ExposedUserRepository(database)
            val profileRepository = ExposedProfileRepository(database)
            val favoriteRepository = ExposedFavoriteRepository(database, profileRepository)
            val preferencesRepository = ExposedPreferencesRepository(database)
            val importMetadataRepository = ExposedImportMetadataRepository(database)

            val passwordHasher = BCryptPasswordHasher(cost = 4) // Fast for tests
            val jwtService = JwtService(config)
            val mockRandomUserDataSource = MockRandomUserDataSource(mockProfiles)

            val authService = AuthService(userRepository, passwordHasher, jwtService)
            val userService = UserService(userRepository, favoriteRepository, preferencesRepository)
            val profileService = ProfileService(profileRepository, mockRandomUserDataSource, importMetadataRepository)
            val favoriteService = FavoriteService(favoriteRepository, profileRepository)
            val preferencesService = PreferencesService(preferencesRepository)

            return TestContext(
                config = config,
                database = database,
                userRepository = userRepository,
                profileRepository = profileRepository,
                favoriteRepository = favoriteRepository,
                preferencesRepository = preferencesRepository,
                importMetadataRepository = importMetadataRepository,
                passwordHasher = passwordHasher,
                jwtService = jwtService,
                mockRandomUserDataSource = mockRandomUserDataSource,
                authService = authService,
                userService = userService,
                profileService = profileService,
                favoriteService = favoriteService,
                preferencesService = preferencesService
            )
        }
    }
}

fun ApplicationTestBuilder.setupTestApp(context: TestContext): HttpClient {
    application {
        komeetModule(
            config = context.config,
            authService = context.authService,
            userService = context.userService,
            profileService = context.profileService,
            favoriteService = context.favoriteService,
            preferencesService = context.preferencesService,
            jwtService = context.jwtService
        )
    }
    return createClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
}

fun sampleProfile(
    id: String = "profile-1",
    firstName: String = "Jean",
    lastName: String = "Dupont",
    email: String = "jean.dupont@example.com",
    age: Int = 30,
    gender: String? = "male",
    phone: String? = "06 12 34 56 78",
    streetNumber: Int? = 12,
    streetName: String? = "Avenue Jean Médecin",
    city: String = "Nice",
    department: String? = "Alpes-Maritimes",
    postcode: String? = "06000",
    country: String = "France"
): Profile = Profile(
    id = id,
    firstName = firstName,
    lastName = lastName,
    email = email,
    age = age,
    gender = gender,
    phone = phone,
    location = fr.unica.miage.komeet.domain.model.Location(
        streetNumber = streetNumber,
        streetName = streetName,
        city = city,
        department = department,
        postcode = postcode,
        country = country
    ),
    thumbnailUrl = "https://example.com/thumb.jpg",
    pictureUrl = "https://example.com/picture.jpg",
    source = "randomuser.me",
    sourceId = id,
    createdAt = Instant.now()
)
