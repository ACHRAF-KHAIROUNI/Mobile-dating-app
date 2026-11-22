package fr.unica.miage.komeet.application.chat

import fr.unica.miage.komeet.api.dto.ChatMessageDto
import fr.unica.miage.komeet.api.dto.ChatReplyDto
import fr.unica.miage.komeet.api.dto.ConversationDto
import fr.unica.miage.komeet.api.dto.toDto
import fr.unica.miage.komeet.domain.error.ProfileNotFoundException
import fr.unica.miage.komeet.domain.error.UserNotFoundException
import fr.unica.miage.komeet.domain.model.ChatMessage
import fr.unica.miage.komeet.domain.repository.ChatRepository
import fr.unica.miage.komeet.domain.repository.ProfileRepository
import fr.unica.miage.komeet.domain.repository.UserRepository
import fr.unica.miage.komeet.infrastructure.gemini.GeminiChatClient
import java.time.Instant
import java.util.UUID

class ChatService(
    private val chatRepository: ChatRepository,
    private val profileRepository: ProfileRepository,
    private val userRepository: UserRepository,
    private val geminiClient: GeminiChatClient
) {

    fun getHistory(userId: String, profileId: String): List<ChatMessageDto> {
        return chatRepository.getHistory(userId, profileId).map { it.toDto() }
    }

    fun getConversations(userId: String): List<ConversationDto> {
        val profileIds = chatRepository.getRecentProfileIds(userId)
        return profileIds.mapNotNull { profileId ->
            val profile = profileRepository.findById(profileId) ?: return@mapNotNull null
            val history = chatRepository.getHistory(userId, profileId, limit = 50)
            val lastMsg = history.lastOrNull() ?: return@mapNotNull null
            ConversationDto(
                profile = profile.toDto(),
                lastMessage = lastMsg.toDto(),
                totalMessages = history.size
            )
        }
    }

    suspend fun sendMessage(userId: String, profileId: String, userText: String): ChatReplyDto {
        val trimmed = userText.trim()
        if (trimmed.isEmpty()) {
            throw IllegalArgumentException("Le message ne peut pas être vide")
        }

        val profile = profileRepository.findById(profileId)
            ?: throw ProfileNotFoundException(profileId)
        val user = userRepository.findById(userId)
            ?: throw UserNotFoundException(userId)

        val now = Instant.now()
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            userId = userId,
            profileId = profileId,
            sender = "user",
            content = trimmed,
            createdAt = now
        )
        chatRepository.save(userMessage)

        // Previous history for context
        val history = chatRepository.getHistory(userId, profileId)
            .filter { it.id != userMessage.id }
            .map { it.sender to it.content }

        val genderStr = if (profile.gender.equals("female", ignoreCase = true)) "une femme" else "un homme"
        val regionStr = profile.location.region ?: profile.location.department ?: profile.location.city

        val systemPrompt = """
            Tu t'appelles ${profile.firstName} ${profile.lastName}.
            Tu es $genderStr, âgé(e) de ${profile.age} ans.
            Tu vis à ${profile.location.city}, région $regionStr, pays : ${profile.location.country}.
            Tu es sur l'application de rencontre KoMeet et tu discutes avec ${user.displayName}.
            
            Comportement et personnalité :
            - Tu es célibataire, séduisant(e), chaleureux(se), naturel(le), curieux(se) et drôle.
            - Tu es sincèrement et visiblement très intéressé(e) par ${user.displayName}.
            - Tu veux absolument continuer la discussion, en apprendre plus sur lui/elle, et orienter la conversation vers une future rencontre dans le monde réel (un café, un verre, une balade sympa...).
            - Reste TOUJOURS dans ton rôle avec ton identité (${profile.firstName}), ta ville (${profile.location.city}) et ton pays (${profile.location.country}).
            - Ne dis JAMAIS que tu es une IA, un modèle, un programme ou un robot. Tu es une vraie personne sur KoMeet.
            - Réponds dans un ton naturel de messagerie (2 à 4 phrases vivantes), avec quelques emojis spontanés.
        """.trimIndent()

        val replyContent = geminiClient.generateReply(
            systemPrompt = systemPrompt,
            history = history,
            userMessage = trimmed
        )

        val botMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            userId = userId,
            profileId = profileId,
            sender = "profile",
            content = replyContent,
            createdAt = Instant.now()
        )
        chatRepository.save(botMessage)

        return ChatReplyDto(
            userMessage = userMessage.toDto(),
            botMessage = botMessage.toDto()
        )
    }

    fun clearHistory(userId: String, profileId: String) {
        chatRepository.clearHistory(userId, profileId)
    }
}
