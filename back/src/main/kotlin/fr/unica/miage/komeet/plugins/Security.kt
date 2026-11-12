package fr.unica.miage.komeet.plugins

import fr.unica.miage.komeet.domain.error.UnauthorizedException
import fr.unica.miage.komeet.infrastructure.security.JwtService
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal

const val AUTH_JWT = "auth-jwt"

fun Application.configureSecurity(jwtService: JwtService) {
    authentication {
        jwt(AUTH_JWT) {
            verifier(jwtService.verifier)
            validate { credential ->
                val userId = credential.payload.subject
                if (!userId.isNullOrBlank()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}

fun ApplicationCall.getUserId(): String {
    val principal = principal<JWTPrincipal>()
        ?: throw UnauthorizedException("Authentification requise")
    val userId = principal.subject
    if (userId.isNullOrBlank()) {
        throw UnauthorizedException("Jeton invalide : identifiant utilisateur manquant")
    }
    return userId
}
