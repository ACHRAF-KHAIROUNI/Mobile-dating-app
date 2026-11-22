package fr.unica.miage.komeet.api.chat

import fr.unica.miage.komeet.api.dto.MessageResponse
import fr.unica.miage.komeet.api.dto.SendChatMessageRequest
import fr.unica.miage.komeet.application.chat.ChatService
import fr.unica.miage.komeet.plugins.AUTH_JWT
import fr.unica.miage.komeet.plugins.getUserId
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.chatRoutes(chatService: ChatService) {
    route("/api/v1/chat") {
        authenticate(AUTH_JWT) {
            get("/conversations") {
                val userId = call.getUserId()
                val conversations = chatService.getConversations(userId)
                call.respond(HttpStatusCode.OK, conversations)
            }

            get("/{profileId}/messages") {
                val userId = call.getUserId()
                val profileId = call.parameters["profileId"] ?: ""
                val messages = chatService.getHistory(userId, profileId)
                call.respond(HttpStatusCode.OK, messages)
            }

            post("/{profileId}/messages") {
                val userId = call.getUserId()
                val profileId = call.parameters["profileId"] ?: ""
                val request = call.receive<SendChatMessageRequest>()
                val reply = chatService.sendMessage(userId, profileId, request.message)
                call.respond(HttpStatusCode.Created, reply)
            }

            delete("/{profileId}/messages") {
                val userId = call.getUserId()
                val profileId = call.parameters["profileId"] ?: ""
                chatService.clearHistory(userId, profileId)
                call.respond(HttpStatusCode.OK, MessageResponse("Historique de conversation effacé"))
            }
        }
    }
}
