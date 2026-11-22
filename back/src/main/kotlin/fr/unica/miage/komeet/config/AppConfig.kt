package fr.unica.miage.komeet.config

import io.github.cdimascio.dotenv.dotenv
import org.slf4j.LoggerFactory
import java.io.File

data class AppConfig(
    val port: Int,
    val host: String,
    val databasePath: String,
    val jwtSecret: String,
    val jwtIssuer: String,
    val jwtAudience: String,
    val jwtExpirationHours: Long,
    val randomUserApiUrl: String,
    val randomUserNationality: String,
    val randomUserImportCount: Int,
    val randomUserSeed: String,
    val autoImportOnStartup: Boolean,
    val corsAllowedHosts: List<String>,
    val geminiApiKey: String = "",
    val geminiModel: String = "gemini-3.1-flash-lite"
) {
    companion object {
        private val logger = LoggerFactory.getLogger(AppConfig::class.java)

        fun load(): AppConfig {
            val dotenv = try {
                dotenv {
                    ignoreIfMissing = true
                    ignoreIfMalformed = true
                }
            } catch (e: Exception) {
                logger.warn("Could not load .env file: ${e.message}")
                null
            }

            fun get(key: String, default: String): String {
                return System.getenv(key)
                    ?: dotenv?.get(key)
                    ?: default
            }

            val port = get("PORT", "8080").toIntOrNull() ?: 8080
            val host = get("HOST", "0.0.0.0")
            val databasePath = get("DATABASE_PATH", "data/komeet.db")
            val jwtSecret = get("JWT_SECRET", "komeet-dev-secret-key-change-in-production-1234567890")
            val jwtIssuer = get("JWT_ISSUER", "http://0.0.0.0:8080/")
            val jwtAudience = get("JWT_AUDIENCE", "komeet-api")
            val jwtExpirationHours = get("JWT_EXPIRATION_HOURS", "24").toLongOrNull() ?: 24L
            val randomUserApiUrl = get("RANDOM_USER_API_URL", "https://randomuser.me/api/")
            val randomUserNationality = get("RANDOM_USER_NATIONALITY", "fr")
            val randomUserImportCount = get("RANDOM_USER_IMPORT_COUNT", "50").toIntOrNull() ?: 50
            val randomUserSeed = get("RANDOM_USER_SEED", "komeet")
            val autoImportOnStartup = get("AUTO_IMPORT_ON_STARTUP", "true").toBoolean()
            val corsRaw = get("CORS_ALLOWED_HOSTS", "*")
            val corsAllowedHosts = corsRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val geminiApiKey = get("GEMINI_API_KEY", "")
            val geminiModel = get("GEMINI_MODEL", "gemini-3.1-flash-lite")

            // Ensure parent directory for database exists if not memory
            if (databasePath != ":memory:") {
                val dbFile = File(databasePath)
                dbFile.parentFile?.mkdirs()
            }

            return AppConfig(
                port = port,
                host = host,
                databasePath = databasePath,
                jwtSecret = jwtSecret,
                jwtIssuer = jwtIssuer,
                jwtAudience = jwtAudience,
                jwtExpirationHours = jwtExpirationHours,
                randomUserApiUrl = randomUserApiUrl,
                randomUserNationality = randomUserNationality,
                randomUserImportCount = randomUserImportCount,
                randomUserSeed = randomUserSeed,
                autoImportOnStartup = autoImportOnStartup,
                corsAllowedHosts = corsAllowedHosts,
                geminiApiKey = geminiApiKey,
                geminiModel = geminiModel
            )
        }
    }
}
