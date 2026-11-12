package fr.unica.miage.komeet.data.remote

import fr.unica.miage.komeet.data.local.SessionDataStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createKtorClient(sessionDataStore: SessionDataStore): HttpClient {
    return HttpClient(OkHttp) {
        defaultRequest {
            url("http://10.0.2.2:8080/api/v1/")
            contentType(ContentType.Application.Json)
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
                prettyPrint = false
            })
        }
        install(Auth) {
            bearer {
                loadTokens {
                    val token = sessionDataStore.getAccessToken()
                    if (!token.isNullOrBlank()) {
                        BearerTokens(accessToken = token, refreshToken = "")
                    } else {
                        null
                    }
                }
                sendWithoutRequest { request ->
                    // Do not send auth token for login, register or health
                    val path = request.url.encodedPath
                    !path.contains("auth/login") && !path.contains("auth/register") && !path.contains("health")
                }
            }
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }
}
