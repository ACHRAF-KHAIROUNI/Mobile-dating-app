package fr.unica.miage.komeet.api.favorites

import fr.unica.miage.komeet.api.dto.MessageResponse
import fr.unica.miage.komeet.application.favorites.FavoriteService
import fr.unica.miage.komeet.plugins.AUTH_JWT
import fr.unica.miage.komeet.plugins.getUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.favoriteRoutes(favoriteService: FavoriteService) {
    route("/api/v1/favorites") {
        authenticate(AUTH_JWT) {
            get {
                val userId = call.getUserId()
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
                val result = favoriteService.listFavorites(userId, page, pageSize)
                call.respond(HttpStatusCode.OK, result)
            }

            post("/{profileId}") {
                val userId = call.getUserId()
                val profileId = call.parameters["profileId"] ?: ""
                val favorite = favoriteService.addFavorite(userId, profileId)
                call.respond(HttpStatusCode.Created, favorite)
            }

            delete("/{profileId}") {
                val userId = call.getUserId()
                val profileId = call.parameters["profileId"] ?: ""
                favoriteService.removeFavorite(userId, profileId)
                call.respond(
                    HttpStatusCode.OK,
                    MessageResponse("Profil retiré des favoris avec succès")
                )
            }
        }
    }
}
