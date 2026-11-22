package fr.unica.miage.komeet.api.regions

import fr.unica.miage.komeet.api.dto.toDto
import fr.unica.miage.komeet.domain.repository.RegionRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.regionRoutes(regionRepository: RegionRepository) {
    route("/api/v1/regions") {
        get {
            val country = call.request.queryParameters["country"]
            val regions = regionRepository.findAll(country).map { it.toDto() }
            call.respond(HttpStatusCode.OK, regions)
        }
    }
}
