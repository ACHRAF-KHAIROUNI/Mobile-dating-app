package fr.unica.miage.komeet.infrastructure.security

import at.favre.lib.crypto.bcrypt.BCrypt

interface PasswordHasher {
    fun hash(password: String): String
    fun verify(password: String, hash: String): Boolean
}

class BCryptPasswordHasher(
    private val cost: Int = 12
) : PasswordHasher {

    override fun hash(password: String): String {
        return BCrypt.withDefaults().hashToString(cost, password.toCharArray())
    }

    override fun verify(password: String, hash: String): Boolean {
        if (password.isEmpty() || hash.isEmpty()) return false
        return try {
            val result = BCrypt.verifyer().verify(password.toCharArray(), hash)
            result.verified
        } catch (_: Exception) {
            false
        }
    }
}
