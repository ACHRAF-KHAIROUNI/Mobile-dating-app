package fr.unica.miage.komeet.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.unica.miage.komeet.data.remote.dto.ChatMessageDto
import fr.unica.miage.komeet.data.repository.ChatRepository
import fr.unica.miage.komeet.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val profileRepository: ProfileRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentProfileId: String? = null

    fun initChat(profileId: String) {
        if (currentProfileId == profileId && _uiState.value.profile != null) return
        currentProfileId = profileId

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 1. Charger les détails du profil pour le header
            val profileRes = profileRepository.getProfile(profileId)
            val profile = profileRes.getOrNull()

            // 2. Charger l'historique de discussion
            val historyRes = chatRepository.getChatHistory(profileId)
            val history = historyRes.getOrElse { emptyList() }

            _uiState.update {
                it.copy(
                    profile = profile,
                    messages = history,
                    isLoading = false,
                    errorMessage = if (profile == null) "Impossible de charger le profil" else null
                )
            }
        }
    }

    fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val profileId = currentProfileId ?: return
        val text = _uiState.value.inputText.trim()
        if (text.isBlank() || _uiState.value.isSending) return

        // Message optimiste pour affichage immédiat
        val optimisticUserMsg = ChatMessageDto(
            id = UUID.randomUUID().toString(),
            userId = "",
            profileId = profileId,
            sender = "USER",
            content = text,
            createdAt = System.currentTimeMillis().toString()
        )

        _uiState.update {
            it.copy(
                inputText = "",
                messages = it.messages + optimisticUserMsg,
                isSending = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val result = chatRepository.sendChatMessage(profileId, text)
            result.fold(
                onSuccess = { replyDto ->
                    _uiState.update {
                        it.copy(
                            messages = it.messages.filter { m -> m.id != optimisticUserMsg.id } + listOf(replyDto.userMessage, replyDto.botMessage),
                            isSending = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            errorMessage = error.message ?: "Échec de l'envoi du message"
                        )
                    }
                }
            )
        }
    }

    fun clearHistory() {
        val profileId = currentProfileId ?: return
        viewModelScope.launch {
            chatRepository.clearChatHistory(profileId)
            _uiState.update { it.copy(messages = emptyList()) }
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
