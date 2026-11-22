package fr.unica.miage.komeet.infrastructure.repository

import fr.unica.miage.komeet.domain.model.ChatMessage
import fr.unica.miage.komeet.domain.repository.ChatRepository
import fr.unica.miage.komeet.infrastructure.database.ChatMessages
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class ExposedChatRepository(private val db: Database) : ChatRepository {

    override fun save(message: ChatMessage): ChatMessage = transaction(db) {
        ChatMessages.upsert(ChatMessages.id) {
            it[id] = message.id
            it[userId] = message.userId
            it[profileId] = message.profileId
            it[sender] = message.sender
            it[content] = message.content
            it[createdAt] = message.createdAt.toString()
        }
        message
    }

    override fun getHistory(userId: String, profileId: String, limit: Int): List<ChatMessage> = transaction(db) {
        ChatMessages.selectAll()
            .where { (ChatMessages.userId eq userId) and (ChatMessages.profileId eq profileId) }
            .orderBy(ChatMessages.createdAt to SortOrder.ASC)
            .limit(limit)
            .map {
                ChatMessage(
                    id = it[ChatMessages.id],
                    userId = it[ChatMessages.userId],
                    profileId = it[ChatMessages.profileId],
                    sender = it[ChatMessages.sender],
                    content = it[ChatMessages.content],
                    createdAt = Instant.parse(it[ChatMessages.createdAt])
                )
            }
    }

    override fun clearHistory(userId: String, profileId: String): Int = transaction(db) {
        ChatMessages.deleteWhere { (ChatMessages.userId eq userId) and (ChatMessages.profileId eq profileId) }
    }

    override fun getRecentProfileIds(userId: String): List<String> = transaction(db) {
        ChatMessages
            .select(ChatMessages.profileId, ChatMessages.createdAt)
            .where { ChatMessages.userId eq userId }
            .orderBy(ChatMessages.createdAt to SortOrder.DESC)
            .map { it[ChatMessages.profileId] }
            .distinct()
    }
}
