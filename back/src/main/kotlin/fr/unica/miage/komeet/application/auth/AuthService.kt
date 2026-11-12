package fr.unica.miage.komeet.application.auth

import fr.unica.miage.komeet.api.dto.AuthResponse
import fr.unica.miage.komeet.api.dto.LoginRequest
import fr.unica.miage.komeet.api.dto.RegisterRequest
import fr.unica.miage.komeet.api.dto.toDto
import fr.unica.miage.komeet.domain.error.EmailAlreadyExistsException
import fr.unica.miage.komeet.domain.error.InvalidCredentialsException
import fr.unica.miage.komeet.domain.error.ValidationException
import fr.unica.miage.komeet.domain.model.User
import fr.unica.miage.komeet.domain.repository.UserRepository
import fr.unica.miage.komeet.infrastructure.security.JwtService
import fr.unica.miage.komeet.infrastructure.security.PasswordHasher
import java.time.Instant
import java.util.UUID

class AuthService(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val jwtService: JwtService
) {
    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

    fun register(request: RegisterRequest): AuthResponse {
        val email = request.email.trim().lowercase()
        val displayName = request.displayName.trim()
        val password = request.password

        val validationErrors = mutableListOf<String>()
        if (email.isBlank()) {
            validationErrors.add("L'adresse e-mail est obligatoire")
        } else if (!emailRegex.matches(email)) {
            validationErrors.add("Format de l'adresse e-mail invalide")
        }

        if (password.length < 6) {
            validationErrors.add("Le mot de passe doit contenir au moins 6 caractères")
        }

        if (displayName.isBlank()) {
            validationErrors.add("Le nom d'affichage est obligatoire")
        }

        if (validationErrors.isNotEmpty()) {
            throw ValidationException("Données d'inscription invalides", validationErrors)
        }

        if (userRepository.findByEmail(email) != null) {
            throw EmailAlreadyExistsException("L'adresse e-mail est déjà utilisée")
        }

        val now = Instant.now()
        val newUser = User(
            id = UUID.randomUUID().toString(),
            email = email,
            passwordHash = passwordHasher.hash(password),
            displayName = displayName,
            createdAt = now,
            updatedAt = now
        )

        userRepository.save(newUser)

        val (token, expiresIn) = jwtService.generateToken(newUser)
        return AuthResponse(
            accessToken = token,
            tokenType = "Bearer",
            expiresIn = expiresIn,
            user = newUser.toDto()
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        val email = request.email.trim().lowercase()
        val password = request.password

        if (email.isBlank() || password.isBlank()) {
            throw InvalidCredentialsException("Identifiants de connexion invalides")
        }

        val user = userRepository.findByEmail(email)
            ?: throw InvalidCredentialsException("Identifiants de connexion invalides")

        if (!passwordHasher.verify(password, user.passwordHash)) {
            throw InvalidCredentialsException("Identifiants de connexion invalides")
        }

        val (token, expiresIn) = jwtService.generateToken(user)
        return AuthResponse(
            accessToken = token,
            tokenType = "Bearer",
            expiresIn = expiresIn,
            user = user.toDto()
        )
    }
}
