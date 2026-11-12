package fr.unica.miage.komeet.domain.error

import io.ktor.http.HttpStatusCode

open class DomainException(
    val statusCode: HttpStatusCode,
    val code: String,
    message: String,
    val details: List<String> = emptyList()
) : RuntimeException(message)

class ValidationException(
    message: String,
    details: List<String> = emptyList()
) : DomainException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", message, details)

class EmailAlreadyExistsException(
    message: String = "Cette adresse e-mail est déjà utilisée"
) : DomainException(HttpStatusCode.Conflict, "EMAIL_ALREADY_EXISTS", message)

class InvalidCredentialsException(
    message: String = "Identifiants invalides"
) : DomainException(HttpStatusCode.Unauthorized, "INVALID_CREDENTIALS", message)

class UnauthorizedException(
    message: String = "Authentification requise"
) : DomainException(HttpStatusCode.Unauthorized, "UNAUTHORIZED", message)

class ProfileNotFoundException(
    profileId: String
) : DomainException(HttpStatusCode.NotFound, "PROFILE_NOT_FOUND", "Profil introuvable : $profileId")

class FavoriteNotFoundException(
    profileId: String
) : DomainException(HttpStatusCode.NotFound, "FAVORITE_NOT_FOUND", "Favori introuvable pour le profil : $profileId")

class UserNotFoundException(
    userId: String
) : DomainException(HttpStatusCode.NotFound, "USER_NOT_FOUND", "Utilisateur introuvable : $userId")
