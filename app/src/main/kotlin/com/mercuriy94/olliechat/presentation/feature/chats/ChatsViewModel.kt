package com.mercuriy94.olliechat.presentation.feature.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity.AssistantMessageEntity
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity.UserMessageEntity
import com.mercuriy94.olliechat.domain.repository.chat.OllieChatRepository
import com.mercuriy94.olliechat.presentation.feature.chat.model.ChatOllieMessageUi.AssistantMessage
import com.mercuriy94.olliechat.presentation.feature.chat.model.ChatOllieMessageUi.UserMessage
import com.mercuriy94.olliechat.presentation.feature.chats.model.ChatUiModel
import com.mercuriy94.olliechat.utils.CoroutineDispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ChatsViewModel(
    private val coroutineDispatchers: CoroutineDispatchers,
    private val chatRepository: OllieChatRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatsUiState())
    val uiState: StateFlow<ChatsUiState> = _uiState.asStateFlow()

    private val _newsEvent = MutableSharedFlow<ChatsNews>()
    val newsEvent = _newsEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            chatRepository.observeChats()
                .map { chats ->
                    chats.map { ollieChatWithLatestMessage ->
                        ChatUiModel(
                            id = ollieChatWithLatestMessage.chat.id,
                            title = ollieChatWithLatestMessage.chat.title,
                            latestMessage = when (val message =
                                ollieChatWithLatestMessage.latestMessage) {
                                is AssistantMessageEntity -> {
                                    AssistantMessage(
                                        id = message.id,
                                        text = message.text,
                                        actions = emptyList(),
                                        think = null,
                                        aiModel = AssistantMessage.AiModel(
                                            0,
                                            ""
                                        ),
                                    )
                                }

                                is UserMessageEntity -> {
                                    UserMessage(id = message.id, text = message.text)
                                }
                            }
                        )
                    }
                }
                .flowOn(coroutineDispatchers.default)
                .collect { newChats ->
                    _uiState.update { currentState ->
                        currentState.copy(chats = newChats)
                    }
                }
        }
    }

    fun chatClicked(id: Long) {
        viewModelScope.launch {
            _newsEvent.emit(ChatsNews.OpenChat(id))
        }
    }
}

internal data class ChatsUiState(
    val chats: List<ChatUiModel> = emptyList(),
)

internal sealed interface ChatsNews {
    data class OpenChat(val chatId: Long) : ChatsNews
}
