package fr.unica.miage.komeet.infrastructure.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import fr.unica.miage.komeet.config.AppConfig
import fr.unica.miage.komeet.domain.model.User
import java.time.Instant
import java.util.Date

class JwtService(
    private val config: AppConfig
) {
    private val algorithm: Algorithm = Algorithm.HMAC256(config.jwtSecret)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(config.jwtIssuer)
        .withAudience(config.jwtAudience)
        .build()

    fun generateToken(user: User): Pair<String, Long> {
        val now = Instant.now()
        val expiresInSeconds = config.jwtExpirationHours * 3600
        val expiresAt = now.plusSeconds(expiresInSeconds)

        val token = JWT.create()
            .withSubject(user.id)
            .withIssuer(config.jwtIssuer)
            .withAudience(config.jwtAudience)
            .withClaim("email", user.email)
            .withClaim("name", user.displayName)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(expiresAt))
            .sign(algorithm)

        return Pair(token, expiresInSeconds)
    }

    fun verify(token: String): DecodedJWT? {
        return try {
            verifier.verify(token)
        } catch (_: Exception) {
            null
        }
    }
}
