package fr.unica.miage.komeet.api.users

import fr.unica.miage.komeet.api.dto.MessageResponse
import fr.unica.miage.komeet.api.dto.UpdateUserRequest
import fr.unica.miage.komeet.application.users.UserService
import fr.unica.miage.komeet.plugins.AUTH_JWT
import fr.unica.miage.komeet.plugins.getUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.route

fun Route.userRoutes(userService: UserService) {
    route("/api/v1/users") {
        authenticate(AUTH_JWT) {
            get("/me") {
                val userId = call.getUserId()
                val user = userService.getMe(userId)
                call.respond(HttpStatusCode.OK, user)
            }

            patch("/me") {
                val userId = call.getUserId()
                val request = call.receive<UpdateUserRequest>()
                val updated = userService.updateMe(userId, request)
                call.respond(HttpStatusCode.OK, updated)
            }

            delete("/me") {
                val userId = call.getUserId()
                userService.deleteMe(userId)
                call.respond(
                    HttpStatusCode.OK,
                    MessageResponse("Compte utilisateur supprimé avec succès")
                )
            }
        }
    }
}
