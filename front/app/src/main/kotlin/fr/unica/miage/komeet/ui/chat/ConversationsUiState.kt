package fr.unica.miage.komeet.ui.chat

import fr.unica.miage.komeet.data.remote.dto.ConversationDto

data class ConversationsUiState(
    val conversations: List<ConversationDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
