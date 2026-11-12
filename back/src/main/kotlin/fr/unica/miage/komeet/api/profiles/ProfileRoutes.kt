package fr.unica.miage.komeet.api.profiles

import fr.unica.miage.komeet.application.profiles.ProfileService
import fr.unica.miage.komeet.domain.repository.ProfileSearchParams
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.profileRoutes(profileService: ProfileService) {
    route("/api/v1/profiles") {
        get {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
            val search = call.request.queryParameters["search"] ?: call.request.queryParameters["q"]
            val minAge = call.request.queryParameters["minAge"]?.toIntOrNull()
            val maxAge = call.request.queryParameters["maxAge"]?.toIntOrNull()
            val gender = call.request.queryParameters["gender"]
            val department = call.request.queryParameters["department"]
            val country = call.request.queryParameters["country"]
            val sort = call.request.queryParameters["sort"] ?: "name"
            val order = call.request.queryParameters["order"] ?: "asc"

            val params = ProfileSearchParams(
                query = search,
                minAge = minAge,
                maxAge = maxAge,
                gender = gender,
                department = department,
                country = country,
                sort = sort,
                order = order,
                page = page,
                pageSize = pageSize
            )

            val result = profileService.listProfiles(params)
            call.respond(HttpStatusCode.OK, result)
        }

        get("/{id}") {
            val id = call.parameters["id"] ?: ""
            val profile = profileService.getProfileById(id)
            call.respond(HttpStatusCode.OK, profile)
        }
    }
}
