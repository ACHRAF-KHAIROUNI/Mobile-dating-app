package fr.unica.miage.komeet

import fr.unica.miage.komeet.application.auth.AuthService
import fr.unica.miage.komeet.application.chat.ChatService
import fr.unica.miage.komeet.application.favorites.FavoriteService
import fr.unica.miage.komeet.application.preferences.PreferencesService
import fr.unica.miage.komeet.application.profiles.ProfileService
import fr.unica.miage.komeet.application.users.UserService
import fr.unica.miage.komeet.config.AppConfig
import fr.unica.miage.komeet.domain.repository.RegionRepository
import fr.unica.miage.komeet.infrastructure.database.DatabaseFactory
import fr.unica.miage.komeet.infrastructure.database.InitialDataSeeder
import fr.unica.miage.komeet.infrastructure.gemini.GeminiChatClient
import fr.unica.miage.komeet.infrastructure.randomuser.KtorRandomUserClient
import fr.unica.miage.komeet.infrastructure.randomuser.RandomUserDataSource
import fr.unica.miage.komeet.infrastructure.repository.*
import fr.unica.miage.komeet.infrastructure.security.BCryptPasswordHasher
import fr.unica.miage.komeet.infrastructure.security.JwtService
import fr.unica.miage.komeet.plugins.configureRouting
import fr.unica.miage.komeet.plugins.configureSecurity
import fr.unica.miage.komeet.plugins.configureSerialization
import fr.unica.miage.komeet.plugins.configureStatusPages
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

fun Application.komeetModule(
    config: AppConfig,
    authService: AuthService,
    userService: UserService,
    profileService: ProfileService,
    favoriteService: FavoriteService,
    preferencesService: PreferencesService,
    regionRepository: RegionRepository,
    jwtService: JwtService,
    chatService: ChatService? = null
) {
    install(CallLogging) {
        level = Level.INFO
    }
    configureSerialization()
    configureStatusPages()
    configureSecurity(jwtService)
    configureRouting(
        config = config,
        authService = authService,
        userService = userService,
        profileService = profileService,
        favoriteService = favoriteService,
        preferencesService = preferencesService,
        regionRepository = regionRepository,
        chatService = chatService
    )
}

fun main() {
    val logger = LoggerFactory.getLogger("fr.unica.miage.komeet.Main")
    val config = AppConfig.load()

    logger.info("Starting KoMeet API server...")
    logger.info("Database path: ${config.databasePath}")
    logger.info("Host: ${config.host}, Port: ${config.port}")

    val database = DatabaseFactory.connect(config.databasePath)

    val userRepository = ExposedUserRepository(database)
    val profileRepository = ExposedProfileRepository(database)
    val favoriteRepository = ExposedFavoriteRepository(database, profileRepository)
    val preferencesRepository = ExposedPreferencesRepository(database)
    val importMetadataRepository = ExposedImportMetadataRepository(database)
    val regionRepository = ExposedRegionRepository(database)

    val passwordHasher = BCryptPasswordHasher()
    val jwtService = JwtService(config)
    val randomUserDataSource: RandomUserDataSource = KtorRandomUserClient.createDefault(config.randomUserApiUrl)

    val authService = AuthService(userRepository, passwordHasher, jwtService)
    val userService = UserService(userRepository, favoriteRepository, preferencesRepository)
    val profileService = ProfileService(profileRepository, randomUserDataSource, importMetadataRepository)
    val favoriteService = FavoriteService(favoriteRepository, profileRepository)
    val preferencesService = PreferencesService(preferencesRepository)

    val chatRepository = ExposedChatRepository(database)
    val geminiClient = GeminiChatClient(
        apiKey = config.geminiApiKey,
        model = config.geminiModel
    )
    val chatService = ChatService(chatRepository, profileRepository, userRepository, geminiClient)

    // Seed regions, international profiles (Maroc, USA, Japon, Arabie Saoudite) and demo users
    InitialDataSeeder.seed(
        db = database,
        regionRepository = regionRepository,
        profileRepository = profileRepository,
        userRepository = userRepository,
        passwordHasher = passwordHasher
    )

    // Initial profiles import if database is empty and autoImport is enabled
    if (config.autoImportOnStartup && profileService.count() == 0L) {
        runBlocking {
            logger.info("Database is empty. Triggering initial profile import from Random User Generator...")
            profileService.importProfiles(
                count = config.randomUserImportCount,
                seed = config.randomUserSeed,
                nationality = config.randomUserNationality,
                force = false
            )
        }
    }

    embeddedServer(Netty, port = config.port, host = config.host) {
        komeetModule(
            config = config,
            authService = authService,
            userService = userService,
            profileService = profileService,
            favoriteService = favoriteService,
            preferencesService = preferencesService,
            regionRepository = regionRepository,
            jwtService = jwtService,
            chatService = chatService
        )
    }.start(wait = true)
}
