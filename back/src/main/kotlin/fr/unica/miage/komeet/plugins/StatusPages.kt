package fr.unica.miage.komeet.plugins

import fr.unica.miage.komeet.api.dto.ErrorDetails
import fr.unica.miage.komeet.api.dto.ErrorEnvelope
import fr.unica.miage.komeet.domain.error.DomainException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.SerializationException
import org.slf4j.LoggerFactory

fun Application.configureStatusPages() {
    val logger = LoggerFactory.getLogger("fr.unica.miage.komeet.StatusPages")

    install(StatusPages) {
        exception<DomainException> { call, cause ->
            logger.warn("Domain exception: ${cause.code} - ${cause.message}")
            call.respond(
                status = cause.statusCode,
                message = ErrorEnvelope(
                    error = ErrorDetails(
                        code = cause.code,
                        message = cause.message ?: "Une erreur est survenue",
                        details = cause.details
                    )
                )
            )
        }

        exception<ContentTransformationException> { call, cause ->
            logger.warn("Content transformation error: ${cause.message}")
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ErrorEnvelope(
                    error = ErrorDetails(
                        code = "INVALID_REQUEST_BODY",
                        message = "Le corps de la requête est manquant ou invalide",
                        details = listOf(cause.message ?: "")
                    )
                )
            )
        }

        exception<BadRequestException> { call, cause ->
            logger.warn("Bad request: ${cause.message}")
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ErrorEnvelope(
                    error = ErrorDetails(
                        code = "BAD_REQUEST",
                        message = cause.message ?: "Requête invalide",
                        details = emptyList()
                    )
                )
            )
        }

        exception<SerializationException> { call, cause ->
            logger.warn("JSON serialization error: ${cause.message}")
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ErrorEnvelope(
                    error = ErrorDetails(
                        code = "SERIALIZATION_ERROR",
                        message = "Format JSON invalide ou champs incompatibles",
                        details = listOf(cause.message ?: "")
                    )
                )
            )
        }

        exception<Throwable> { call, cause ->
            logger.error("Unhandled internal server error: ${cause.message}", cause)
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = ErrorEnvelope(
                    error = ErrorDetails(
                        code = "INTERNAL_SERVER_ERROR",
                        message = "Une erreur interne inattendue est survenue",
                        details = emptyList()
                    )
                )
            )
        }

        status(HttpStatusCode.Unauthorized) { call, status ->
            call.respond(
                status = status,
                message = ErrorEnvelope(
                    error = ErrorDetails(
                        code = "UNAUTHORIZED",
                        message = "Jeton d'authentification manquant, invalide ou expiré",
                        details = emptyList()
                    )
                )
            )
        }
    }
}
