package fr.unica.miage.komeet.infrastructure.gemini

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

@Serializable
data class GeminiPart(val text: String)

@Serializable
data class GeminiContent(val role: String, val parts: List<GeminiPart>)

@Serializable
data class GeminiSystemInstruction(val parts: List<GeminiPart>)

@Serializable
data class GeminiGenerateRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiSystemInstruction? = null
)

@Serializable
data class GeminiCandidate(val content: GeminiContent)

@Serializable
data class GeminiGenerateResponse(
    val candidates: List<GeminiCandidate>? = null
)

class GeminiChatClient(
    private val apiKey: String = "",
    private val model: String = "gemini-3.1-flash-lite",
    private val client: HttpClient = createDefaultHttpClient()
) {
    private val logger = LoggerFactory.getLogger(GeminiChatClient::class.java)

    companion object {
        fun createDefaultHttpClient(): HttpClient {
            return HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    })
                }
                install(HttpTimeout) {
                    requestTimeoutMillis = 60000L // 1 minute pour les requêtes Gemini
                    connectTimeoutMillis = 30000L
                    socketTimeoutMillis = 60000L
                }
            }
        }
    }

    suspend fun generateReply(
        systemPrompt: String,
        history: List<Pair<String, String>>, // list of (sender, text), sender="user" or "profile"
        userMessage: String
    ): String {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val contents = mutableListOf<GeminiContent>()
        // Append history
        for ((sender, text) in history) {
            val role = if (sender == "user") "user" else "model"
            contents.add(GeminiContent(role = role, parts = listOf(GeminiPart(text = text))))
        }
        // Append current message
        contents.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = userMessage))))

        val requestBody = GeminiGenerateRequest(
            contents = contents,
            systemInstruction = GeminiSystemInstruction(parts = listOf(GeminiPart(text = systemPrompt)))
        )

        return try {
            val response: GeminiGenerateResponse = client.post(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            text?.trim() ?: "Je suis tellement content(e) de parler avec toi ! Raconte-moi un peu plus sur toi 😊"
        } catch (e: Exception) {
            logger.error("Gemini API call failed: ${e.message}", e)
            "Coucou ! Ravie de discuter avec toi 😊 J'aimerais beaucoup en savoir plus sur toi, comment s'est passée ta journée ?"
        }
    }
}
