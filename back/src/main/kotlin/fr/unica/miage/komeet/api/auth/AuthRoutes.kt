package fr.unica.miage.komeet.api.auth

import fr.unica.miage.komeet.api.dto.LoginRequest
import fr.unica.miage.komeet.api.dto.MessageResponse
import fr.unica.miage.komeet.api.dto.RegisterRequest
import fr.unica.miage.komeet.application.auth.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoutes(authService: AuthService) {
    route("/api/v1/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val response = authService.register(request)
            call.respond(HttpStatusCode.Created, response)
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val response = authService.login(request)
            call.respond(HttpStatusCode.OK, response)
        }

        post("/logout") {
            // Stateless JWT logout: client should discard token locally
            call.respond(
                HttpStatusCode.OK,
                MessageResponse("Déconnexion réussie. Le jeton d'accès peut être supprimé localement.")
            )
        }
    }
}
