package com.mercuriy94.olliechat.presentation.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.WorkManager
import com.mercuriy94.olliechat.OllieChatApplication
import com.mercuriy94.olliechat.di.CoroutineDispatchersModule
import com.mercuriy94.olliechat.di.OllieChatDataModule
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity.AssistantMessageEntity
import com.mercuriy94.olliechat.domain.repository.model.OllieModelRepository
import com.mercuriy94.olliechat.presentation.common.model.LlmModelDetailsUi
import com.mercuriy94.olliechat.presentation.common.model.LlmModelUi
import com.mercuriy94.olliechat.presentation.container.ChatScreenNavKey
import com.mercuriy94.olliechat.presentation.container.ChatScreenNavKey.ChatType.NewChat
import com.mercuriy94.olliechat.presentation.feature.chat.OllieChatWorkManager.UserMessageToSend
import com.mercuriy94.olliechat.presentation.feature.chat.model.ASSISTANT_MESSAGE_ACTION_COPY_ID
import com.mercuriy94.olliechat.presentation.feature.chat.model.ASSISTANT_MESSAGE_ACTION_INFO_ID
import com.mercuriy94.olliechat.presentation.feature.chat.model.ASSISTANT_MESSAGE_ACTION_REPEAT_ID
import com.mercuriy94.olliechat.presentation.feature.chat.model.ASSISTANT_MESSAGE_ACTION_SHARE_ID
import com.mercuriy94.olliechat.presentation.feature.chat.model.ChatOllieMessageUi
import com.mercuriy94.olliechat.presentation.feature.chat.model.ChatOllieMessageUi.AssistantMessage
import com.mercuriy94.olliechat.presentation.feature.chat.model.ChatOllieMessageUi.UserMessage
import com.mercuriy94.olliechat.presentation.feature.chat.model.ChatOllieModelUi
import com.mercuriy94.olliechat.presentation.feature.chat.model.USER_MESSAGE_ACTION_COPY_ID
import com.mercuriy94.olliechat.presentation.feature.chat.model.USER_MESSAGE_ACTION_EDIT_ID
import com.mercuriy94.olliechat.presentation.feature.chat.model.mapper.OllieChatMessageDomainToChatUiEntityMapper
import com.mercuriy94.olliechat.utils.CoroutineDispatchers
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@OptIn(ExperimentalCoroutinesApi::class)
internal class ChatViewModel(
    private val chatType: ChatScreenNavKey.ChatType,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val modelRepository: OllieModelRepository,
    private val ollieChatWorkManager: OllieChatWorkManager,
    private val messageDomainToChatUiEntityMapper: OllieChatMessageDomainToChatUiEntityMapper,
) : ViewModel() {

    companion object {

        private const val TAG = "ChatViewModel"
        private const val NEWS_BUFFER_CAPACITY = 16
        private const val ASSISTANT_MESSAGE_ACTION_COPY_LABEL = "OllieChat Ai Message"
        private const val USER_MESSAGE_ACTION_COPY_LABEL = "OllieChat User Message"

        val CHAT_TYPE_KEY = object : CreationExtras.Key<ChatScreenNavKey.ChatType> {}

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ChatViewModel(
                    modelRepository = OllieChatDataModule.ollieModelRepository,
                    coroutineDispatchers = CoroutineDispatchersModule.coroutineDispatchers,
                    chatType = requireNotNull(this[CHAT_TYPE_KEY]) { "Chat type mustn't be null!" },
                    ollieChatWorkManager = OllieChatWorkManager(
                        chatRepository = OllieChatDataModule.ollieChatRepository,
                        messageRepository = OllieChatDataModule.ollieMessageRepository,
                        chatWorksRepository = OllieChatDataModule.ollieChatWorksRepository,
                        workManager = WorkManager.getInstance(context = OllieChatApplication.context),
                        streamingChatWorkManager = OllieChatDataModule.streamingChatWorkManager,
                        modelRepository = OllieChatDataModule.ollieModelRepository,
                    ),
                    messageDomainToChatUiEntityMapper = OllieChatMessageDomainToChatUiEntityMapper(),
                )
            }
        }
    }

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _newsEvent = MutableSharedFlow<ChatNews>(
        replay = 0,
        extraBufferCapacity = NEWS_BUFFER_CAPACITY
    )
    val newsEvent = _newsEvent.asSharedFlow()

    private val chatId: Long = chatType.chatId

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.tag(TAG).e("Exception caught: ${throwable.message}")
    }

    init {
        viewModelScope.launch(exceptionHandler) {
            if (chatType is NewChat) {
                sendMessage(
                    message = UserMessageToSend.PendingMessage(messageId = chatType.messageId),
                    aiModelId = chatType.selectedModel,
                )
            }
        }
        observeChatTitle()
        observeMessages()
        observeActiveStreamingChat()
        getModels()
    }

    @OptIn(FlowPreview::class)
    private fun observeActiveStreamingChat() {
        viewModelScope.launch(exceptionHandler) {
            ollieChatWorkManager.observeActiveStreamingChats(chatId)
                .sample(48.toDuration(DurationUnit.MILLISECONDS))
                .mapLatest { activeStreamingChats ->
                    if (activeStreamingChats.isNotEmpty()) {
                        activeStreamingChats.forEach { activeStreamingChat ->
                            if (activeStreamingChat.partialResponse.isBlank()) {
                                _uiState.update { state -> state.copy(inProgress = true) }
                                return@forEach
                            }
                            val updatedMessages =
                                _uiState.value.messages.toMutableList().also { updatedMessages ->
                                    val (think, response) = messageDomainToChatUiEntityMapper.parseMessageText(
                                        activeStreamingChat.partialResponse
                                    )
                                    val index =
                                        updatedMessages.indexOfFirst { it.id == activeStreamingChat.aiMessageId }

                                    if (index >= 0) {
                                        val cachedMessage = updatedMessages[index]
                                        if (cachedMessage is AssistantMessage) {
                                            updatedMessages[index] = AssistantMessage(
                                                id = activeStreamingChat.aiMessageId,
                                                think = think,
                                                text = response,
                                                actions = emptyList(),
                                                aiModel = AssistantMessage.AiModel(
                                                    aiModelId = cachedMessage.aiModel.aiModelId,
                                                    name = cachedMessage.aiModel.name,
                                                )
                                            )
                                        }
                                    } else {

                                        val cachedMessage = ollieChatWorkManager.getMessageById(
                                            chatId = chatId,
                                            messageId = activeStreamingChat.aiMessageId
                                        )

                                        if (cachedMessage != null && cachedMessage is AssistantMessageEntity) {
                                            updatedMessages.add(
                                                AssistantMessage(
                                                    id = activeStreamingChat.aiMessageId,
                                                    think = think,
                                                    text = response,
                                                    actions = emptyList(),
                                                    aiModel = AssistantMessage.AiModel(
                                                        aiModelId = cachedMessage.aiModel.aiModelId,
                                                        name = cachedMessage.aiModel.name,
                                                    )
                                                )
                                            )
                                        }
                                    }
                                }
                            _uiState.update { state ->
                                state.copy(messages = updatedMessages, inProgress = true)
                            }
                        }
                    } else {
                        _uiState.update { state -> state.copy(inProgress = false) }
                    }
                }
                .flowOn(coroutineDispatchers.default)
                .launchIn(viewModelScope)
        }
    }

    private fun observeChatTitle() {
        viewModelScope.launch(exceptionHandler) {
            ollieChatWorkManager.observeChatTitle(chatId)
                .distinctUntilChanged()
                .flowOn(coroutineDispatchers.default)
                .collect { _uiState.update { state -> state.copy(chatTitle = it) } }
        }
    }

    private fun observeMessages() {
        viewModelScope.launch(exceptionHandler) {
            ollieChatWorkManager.observeFinishedMessages(chatId)
                .distinctUntilChanged()
                .mapLatest(messageDomainToChatUiEntityMapper::invoke)
                .flowOn(coroutineDispatchers.default)
                .collect { newList -> _uiState.update { state -> state.copy(messages = newList) } }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getModels() {
        viewModelScope.launch(exceptionHandler) {
            modelRepository.getModels()
                .catch {
                    Timber.tag(TAG).e(it, "Could not get models!")
                    emit(emptyList())
                }
                .mapLatest { ollieModels ->
                    ollieModels.map { ollieModel ->
                        ChatOllieModelUi(
                            id = ollieModel.id,
                            name = ollieModel.name,
                            size = ollieModel.size,
                            digest = ollieModel.digest,
                            details = LlmModelDetailsUi(
                                id = ollieModel.details.id,
                                parameterSize = ollieModel.details.parameterSize,
                                quantizationLevel = ollieModel.details.quantizationLevel,
                            ),
                        )
                    }
                }
                .map { it.sortedBy(ChatOllieModelUi::size) } // for testing
                .mapLatest { models ->
                    val selectedModel = _uiState.value.selectedModel ?: let {
                        val aiModelId = when (chatType) {
                            is NewChat -> chatType.selectedModel
                            else -> ollieChatWorkManager.getLastAssistantMessageByChatId(chatId)?.aiModel?.aiModelId
                        }
                        aiModelId?.let { models.firstOrNull { model -> model.id == it } }
                    } ?: models.firstOrNull()
                    models to selectedModel
                }
                .flowOn(coroutineDispatchers.default)
                .collect { (ollieModels, selectedModel) ->
                    _uiState.update { state ->
                        state.copy(selectedModel = selectedModel, availableModels = ollieModels)
                    }
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun send(newUserMessage: String) {
        val selectedModel = _uiState.value.selectedModel ?: return
        if (_uiState.value.inProgress) return
        sendMessage(message = UserMessageToSend.NewMessage(newUserMessage), aiModelId = selectedModel.id)
    }

    private fun sendMessage(message: UserMessageToSend, aiModelId: Long) {
        viewModelScope.launch {
            ollieChatWorkManager.sendMessage(chatId = chatId, message = message, aiModelId = aiModelId)
        }
    }

    fun selectModel(model: LlmModelUi) {
        _uiState.update { state -> state.copy(selectedModel = model) }
    }

    fun stopCurrentStreamingChat() {
        viewModelScope.launch(exceptionHandler) {
            ollieChatWorkManager.stopCurrentStreamingChat(chatId)
        }
        _uiState.update { state -> state.copy(inProgress = false) }
    }

    fun onAssistantMessageActionClick(
        message: AssistantMessage,
        action: ChatOllieMessageUi.Action,
    ) {
        viewModelScope.launch(exceptionHandler) {
            when (action.id) {
                ASSISTANT_MESSAGE_ACTION_COPY_ID -> {
                    _newsEvent.emit(
                        ChatNews.CopyMessage(label = ASSISTANT_MESSAGE_ACTION_COPY_LABEL, text = message.text ?: "")
                    )
                }

                ASSISTANT_MESSAGE_ACTION_SHARE_ID -> {

                }

                ASSISTANT_MESSAGE_ACTION_REPEAT_ID -> {

                }

                ASSISTANT_MESSAGE_ACTION_INFO_ID -> {
                    message.tokenUsage?.let { tokenUsage ->
                        _newsEvent.emit(ChatNews.ShowTokenUsageStatistics(tokenUsage))
                    }
                }

            }
        }
    }

    fun onUserMessageActionClick(message: UserMessage, action: ChatOllieMessageUi.Action) {
        when (action.id) {
            USER_MESSAGE_ACTION_COPY_ID -> {
                viewModelScope.launch(exceptionHandler) {
                    _newsEvent.emit(
                        ChatNews.CopyMessage(label = USER_MESSAGE_ACTION_COPY_LABEL, text = message.text)
                    )
                }
            }

            USER_MESSAGE_ACTION_EDIT_ID -> {

            }
        }

    }
}

internal data class ChatUiState(
    val chatTitle: String = "OllieChat",
    val selectedModel: LlmModelUi? = null,
    val availableModels: List<LlmModelUi> = emptyList(),
    val messages: List<ChatOllieMessageUi> = emptyList(),
    val inProgress: Boolean = false,
)

internal sealed interface ChatNews {

    data class CopyMessage(val label: String, val text: String) : ChatNews
    data class ShowTokenUsageStatistics(val tokenUsage: AssistantMessage.TokenUsage) : ChatNews

}
