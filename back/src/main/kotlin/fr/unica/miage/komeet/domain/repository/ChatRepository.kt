package fr.unica.miage.komeet.domain.repository

import fr.unica.miage.komeet.domain.model.ChatMessage

interface ChatRepository {
    fun save(message: ChatMessage): ChatMessage
    fun getHistory(userId: String, profileId: String, limit: Int = 100): List<ChatMessage>
    fun clearHistory(userId: String, profileId: String): Int
    fun getRecentProfileIds(userId: String): List<String>
}
