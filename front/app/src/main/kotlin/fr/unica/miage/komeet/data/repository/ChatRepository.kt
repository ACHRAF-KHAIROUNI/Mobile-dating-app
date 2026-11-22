package fr.unica.miage.komeet.data.repository

import fr.unica.miage.komeet.data.remote.KoMeetRemoteSource
import fr.unica.miage.komeet.data.remote.dto.ChatMessageDto
import fr.unica.miage.komeet.data.remote.dto.ChatReplyDto
import fr.unica.miage.komeet.data.remote.dto.ConversationDto

interface ChatRepository {
    suspend fun getConversations(): Result<List<ConversationDto>>
    suspend fun getChatHistory(profileId: String): Result<List<ChatMessageDto>>
    suspend fun sendChatMessage(profileId: String, content: String): Result<ChatReplyDto>
    suspend fun clearChatHistory(profileId: String): Result<Unit>
}

class ChatRepositoryImpl(
    private val remoteSource: KoMeetRemoteSource
) : ChatRepository {

    override suspend fun getConversations(): Result<List<ConversationDto>> = runCatching {
        remoteSource.getConversations()
    }

    override suspend fun getChatHistory(profileId: String): Result<List<ChatMessageDto>> = runCatching {
        remoteSource.getChatHistory(profileId)
    }

    override suspend fun sendChatMessage(profileId: String, content: String): Result<ChatReplyDto> = runCatching {
        remoteSource.sendChatMessage(profileId, content)
    }

    override suspend fun clearChatHistory(profileId: String): Result<Unit> = runCatching {
        remoteSource.clearChatHistory(profileId).let { }
    }
}
