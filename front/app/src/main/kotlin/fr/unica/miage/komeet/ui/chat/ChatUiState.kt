package fr.unica.miage.komeet.ui.chat

import fr.unica.miage.komeet.data.remote.dto.ChatMessageDto
import fr.unica.miage.komeet.data.remote.dto.ProfileDto

data class ChatUiState(
    val profile: ProfileDto? = null,
    val messages: List<ChatMessageDto> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val errorMessage: String? = null
)
