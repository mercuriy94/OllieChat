package com.mercuriy94.olliechat.presentation.feature.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mercuriy94.olliechat.di.CoroutineDispatchersModule
import com.mercuriy94.olliechat.di.OllieChatDataModule
import com.mercuriy94.olliechat.domain.repository.chat.OllieChatRepository
import com.mercuriy94.olliechat.domain.repository.chat.OllieMessageRepository
import com.mercuriy94.olliechat.domain.repository.model.OllieModelRepository
import com.mercuriy94.olliechat.presentation.common.model.LlmModelDetailsUi
import com.mercuriy94.olliechat.presentation.common.model.LlmModelUi
import com.mercuriy94.olliechat.presentation.feature.welcome.WelcomeNews.OpenChats
import com.mercuriy94.olliechat.presentation.feature.welcome.WelcomeNews.OpenNewChat
import com.mercuriy94.olliechat.presentation.feature.welcome.model.WelcomeOllieModelUi
import com.mercuriy94.olliechat.utils.CoroutineDispatchers
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

internal class WelcomeViewModel(
    private val coroutineDispatchers: CoroutineDispatchers,
    private val ollieModelRepository: OllieModelRepository,
    private val ollieChatRepository: OllieChatRepository,
    private val ollieMessageRepository: OllieMessageRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "WelcomeViewModel"
        private const val NEWS_BUFFER_CAPACITY = 16

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                WelcomeViewModel(
                    ollieChatRepository = OllieChatDataModule.ollieChatRepository,
                    ollieModelRepository = OllieChatDataModule.ollieModelRepository,
                    ollieMessageRepository = OllieChatDataModule.ollieMessageRepository,
                    coroutineDispatchers = CoroutineDispatchersModule.coroutineDispatchers,
                )
            }
        }
    }

    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState: StateFlow<WelcomeUiState> = _uiState.asStateFlow()

    private val _newsEvent = MutableSharedFlow<WelcomeNews>(
        replay = 0,
        extraBufferCapacity = NEWS_BUFFER_CAPACITY
    )
    val newsEvent = _newsEvent.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.tag(TAG).e("Exception caught: ${throwable.message}")
    }

    init {
        getModels()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getModels() {
        viewModelScope.launch(exceptionHandler) {
            ollieModelRepository.getModels()
                .catch {
                    Timber.tag(TAG).e(it, "Could not get models!")
                    emit(emptyList())
                }
                .mapLatest { ollieModels ->
                    ollieModels.map { ollieModel ->
                        WelcomeOllieModelUi(
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
                .map { it.sortedBy(WelcomeOllieModelUi::size) } // for testing
                .flowOn(coroutineDispatchers.default)
                .collect { ollieModels ->
                    _uiState.update { state ->
                        state.copy(
                            selectedModel = state.selectedModel ?: ollieModels.firstOrNull(),
                            availableModels = ollieModels,
                        )
                    }
                }
        }
    }

    fun selectModel(model: LlmModelUi) {
        _uiState.update { state ->
            state.copy(selectedModel = model)
        }
    }

    fun onNewMessage(message: String) {
        val selectedModel = uiState.value.selectedModel ?: return

        viewModelScope.launch(exceptionHandler) {
            val chatId = ollieChatRepository.createNewChat()
            val messageId = ollieMessageRepository.saveNewUserMessage(chatId, message)
            _newsEvent.emit(
                OpenNewChat(
                    chatId = chatId,
                    chatMessage = messageId,
                    selectedModel = selectedModel.id
                )
            )
        }
    }

    fun onChatsClicked() {
        _newsEvent.tryEmit(OpenChats)
    }
}

internal data class WelcomeUiState(
    val selectedModel: LlmModelUi? = null,
    val availableModels: List<LlmModelUi> = emptyList(),
    val inProgress: Boolean = false,
)

internal sealed interface WelcomeNews {

    data class OpenNewChat(
        val chatId: Long,
        val chatMessage: Long,
        val selectedModel: Long,
    ) : WelcomeNews

    data object OpenChats : WelcomeNews

}
