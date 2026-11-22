package fr.unica.miage.komeet.api.dto

import fr.unica.miage.komeet.domain.model.ChatMessage
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageDto(
    val id: String,
    val userId: String,
    val profileId: String,
    val sender: String, // "user" or "profile"
    val content: String,
    val createdAt: String
)

@Serializable
data class SendChatMessageRequest(
    val message: String
)

@Serializable
data class ChatReplyDto(
    val userMessage: ChatMessageDto,
    val botMessage: ChatMessageDto
)

@Serializable
data class ConversationDto(
    val profile: ProfileDto,
    val lastMessage: ChatMessageDto,
    val totalMessages: Int = 0
)

fun ChatMessage.toDto() = ChatMessageDto(
    id = id,
    userId = userId,
    profileId = profileId,
    sender = sender,
    content = content,
    createdAt = createdAt.toString()
)
