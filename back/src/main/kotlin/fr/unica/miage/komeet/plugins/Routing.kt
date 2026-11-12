package fr.unica.miage.komeet.plugins

import fr.unica.miage.komeet.api.auth.authRoutes
import fr.unica.miage.komeet.api.favorites.favoriteRoutes
import fr.unica.miage.komeet.api.health.healthRoutes
import fr.unica.miage.komeet.api.preferences.preferencesRoutes
import fr.unica.miage.komeet.api.profiles.profileRoutes
import fr.unica.miage.komeet.api.users.userRoutes
import fr.unica.miage.komeet.application.auth.AuthService
import fr.unica.miage.komeet.application.favorites.FavoriteService
import fr.unica.miage.komeet.application.preferences.PreferencesService
import fr.unica.miage.komeet.application.profiles.ProfileService
import fr.unica.miage.komeet.application.users.UserService
import fr.unica.miage.komeet.config.AppConfig
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.io.File

fun Application.configureRouting(
    config: AppConfig,
    authService: AuthService,
    userService: UserService,
    profileService: ProfileService,
    favoriteService: FavoriteService,
    preferencesService: PreferencesService
) {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)

        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)

        if (config.corsAllowedHosts.contains("*")) {
            anyHost()
        } else {
            config.corsAllowedHosts.forEach { host ->
                allowHost(host)
            }
        }
    }

    routing {
        get("/") {
            call.respondText(
                "Bienvenue sur l'API KoMeet - Documentation OpenAPI disponible sur /docs/openapi.yaml",
                status = HttpStatusCode.OK
            )
        }

        get("/docs/openapi.yaml") {
            val file = File("docs/openapi.yaml")
            if (file.exists()) {
                call.respondFile(file)
            } else {
                call.respondText("Fichier OpenAPI non trouvé", status = HttpStatusCode.NotFound)
            }
        }

        get("/openapi.yaml") {
            val file = File("docs/openapi.yaml")
            if (file.exists()) {
                call.respondFile(file)
            } else {
                call.respondText("Fichier OpenAPI non trouvé", status = HttpStatusCode.NotFound)
            }
        }

        healthRoutes(profileService)
        authRoutes(authService)
        userRoutes(userService)
        profileRoutes(profileService)
        favoriteRoutes(favoriteService)
        preferencesRoutes(preferencesService)
    }
}
