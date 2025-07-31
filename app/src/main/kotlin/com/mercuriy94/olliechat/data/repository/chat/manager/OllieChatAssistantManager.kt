package com.mercuriy94.olliechat.data.repository.chat.manager

import com.mercuriy94.olliechat.data.langchain4j.ChatAssistant
import com.mercuriy94.olliechat.data.repository.chat.memory.OllieChatMemoryProvider
import com.mercuriy94.olliechat.domain.repository.model.OllieModelRepository
import com.mercuriy94.olliechat.utils.CoroutineDispatchers
import com.mercuriy94.olliechat.utils.ext.buildAiServices
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class OllieChatAssistantManager(
    private val ollamaChatModelFactory: OllamaChatModelFactory,
    private val ollieChatMemoryProvider: OllieChatMemoryProvider,
    private val ollieModelRepository: OllieModelRepository,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val okHttpClient: OkHttpClient,
) {

    private val okHttpClientDispatcher: Dispatcher = okHttpClient.dispatcher

    @Suppress("ForbiddenComment")
    // TODO: Clean Dead references ?
    private val assistants = ConcurrentHashMap<Long, WeakReference<OllieChatAssistantSource>>()

    private val assistantsModificationMutex = Mutex()

    suspend fun getAssistantSource(
        modelId: Long,
    ): OllieChatAssistantSource {
        return assistants[modelId]?.get()
            ?: assistantsModificationMutex.withLock {
                assistants[modelId]?.get()?.let { return@withLock it }
                buildAssistantHolder(
                    modelId = modelId,
                    okHttpRequestTag = generateOkHttpRequestTag(),
                ).also { newAssistantProvider ->
                    assistants[modelId] = WeakReference(newAssistantProvider)
                }
            }
    }

    private fun buildAssistantHolder(
        modelId: Long,
        okHttpRequestTag: String,
    ): OllieChatAssistantSource {
        return OllieChatAssistantHolder(
            aiModelId = modelId,
            assistantProvider = { aiModelId ->
                buildAssistant(
                    modelId = aiModelId,
                    okHttpRequestTag = okHttpRequestTag,
                )
            },
        )
    }

    private suspend fun buildAssistant(
        modelId: Long,
        okHttpRequestTag: String,
    ): OllieChatAssistant {
        val model = requireNotNull(ollieModelRepository.getModelById(modelId)) {
            "Couldn't find passed chat by id"
        }
        return OllieChatAssistantImpl(
            assistant = buildAiServices<ChatAssistant> {
                streamingChatModel(
                    ollamaChatModelFactory.createStreamingChatModel(
                        tag = okHttpRequestTag,
                        model = model,
                        okHttpClient = okHttpClient,
                    )
                )
                chatMemoryProvider(ollieChatMemoryProvider)
            },
            okHttpRequestTag = okHttpRequestTag,
            okHttpClientDispatcher = okHttpClientDispatcher,
            coroutineDispatchers = coroutineDispatchers,
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun generateOkHttpRequestTag(): String = Uuid.random().toHexDashString()
}
