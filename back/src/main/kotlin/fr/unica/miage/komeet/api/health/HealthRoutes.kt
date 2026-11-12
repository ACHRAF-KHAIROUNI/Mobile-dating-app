package fr.unica.miage.komeet.api.health

import fr.unica.miage.komeet.api.dto.HealthDto
import fr.unica.miage.komeet.application.profiles.ProfileService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import java.time.Instant

private suspend fun respondHealth(call: ApplicationCall, profileService: ProfileService) {
    val profilesCount = try {
        profileService.count()
    } catch (_: Exception) {
        -1L
    }
    val dbStatus = if (profilesCount >= 0) "UP" else "DOWN"
    val status = if (dbStatus == "UP") "healthy" else "degraded"
    call.respond(
        status = HttpStatusCode.OK,
        message = HealthDto(
            status = status,
            version = "1.0.0",
            database = dbStatus,
            profilesCount = profilesCount,
            timestamp = Instant.now().toString()
        )
    )
}

fun Route.healthRoutes(profileService: ProfileService) {
    get("/health") {
        respondHealth(call, profileService)
    }

    get("/api/v1/health") {
        respondHealth(call, profileService)
    }
}
