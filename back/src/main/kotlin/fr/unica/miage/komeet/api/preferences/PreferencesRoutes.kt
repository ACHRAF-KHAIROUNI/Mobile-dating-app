package fr.unica.miage.komeet.api.preferences

import fr.unica.miage.komeet.api.dto.UpdatePreferencesRequest
import fr.unica.miage.komeet.application.preferences.PreferencesService
import fr.unica.miage.komeet.plugins.AUTH_JWT
import fr.unica.miage.komeet.plugins.getUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.preferencesRoutes(preferencesService: PreferencesService) {
    route("/api/v1/preferences") {
        authenticate(AUTH_JWT) {
            get {
                val userId = call.getUserId()
                val preferences = preferencesService.getPreferences(userId)
                call.respond(HttpStatusCode.OK, preferences)
            }

            put {
                val userId = call.getUserId()
                val request = call.receive<UpdatePreferencesRequest>()
                val updated = preferencesService.updatePreferences(userId, request)
                call.respond(HttpStatusCode.OK, updated)
            }
        }
    }
}
