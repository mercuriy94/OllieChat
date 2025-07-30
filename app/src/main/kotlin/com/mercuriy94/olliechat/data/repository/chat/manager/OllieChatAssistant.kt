package com.mercuriy94.olliechat.data.repository.chat.manager

import com.mercuriy94.olliechat.data.langchain4j.ChatAssistant
import com.mercuriy94.olliechat.data.repository.chat.memory.OllieChatMemoryId
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageStreamingReply
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageTokenUsage
import com.mercuriy94.olliechat.utils.CoroutineDispatchers
import dev.langchain4j.kotlin.model.chat.StreamingChatModelReply
import dev.langchain4j.kotlin.model.chat.StreamingChatModelReply.CompleteResponse
import dev.langchain4j.kotlin.model.chat.StreamingChatModelReply.PartialResponse
import dev.langchain4j.kotlin.service.asReplyFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import okhttp3.Call
import okhttp3.Dispatcher
import timber.log.Timber

private const val TAG = "OllieChatAssistant"

internal interface OllieChatAssistant {

    fun chat(
        chatMemoryId: OllieChatMemoryId,
        text: String,
    ): Flow<OllieChatMessageStreamingReply>
}

internal class OllieChatAssistantImpl(
    private val assistant: ChatAssistant,
    private val okHttpRequestTag: String,
    private val okHttpClientDispatcher: Dispatcher,
    private val coroutineDispatchers: CoroutineDispatchers,
) : OllieChatAssistant {

    override fun chat(
        chatMemoryId: OllieChatMemoryId,
        text: String,
    ): Flow<OllieChatMessageStreamingReply> =
        flow {
            emitAll(assistant.chat(chatMemoryId = chatMemoryId, text = text).asReplyFlow())
        }.onStart {
            Timber.tag(TAG).d("Starting request with tag: $okHttpRequestTag")
        }.map { reply ->
            when (reply) {
                is CompleteResponse -> {

                    OllieChatMessageStreamingReply.CompleteResponse(
                        aiMessageId = chatMemoryId.assistantMessageId,
                        response = reply.response.aiMessage().text(),
                        tokenUsage = with(reply.response.tokenUsage()) {
                            OllieChatMessageTokenUsage(
                                inputTokenCount = inputTokenCount(),
                                outputTokenCount = outputTokenCount(),
                                totalTokenCount = totalTokenCount(),
                            )
                        }
                    )
                }

                is StreamingChatModelReply.Error -> {
                    OllieChatMessageStreamingReply.Error(
                        aiMessageId = chatMemoryId.assistantMessageId,
                        cause = reply.cause
                    )
                }

                is PartialResponse -> {
                    OllieChatMessageStreamingReply.PartialResponse(
                        aiMessageId = chatMemoryId.assistantMessageId,
                        partialResponse = reply.partialResponse
                    )
                }
            }
        }.onCompletion { cause ->
            if (cause != null) {
                Timber.tag(TAG).d("Canceling request with tag: $okHttpRequestTag due to: $cause")
                okHttpClientDispatcher.cancelCallsByTag(okHttpRequestTag)
            }
        }.flowOn(coroutineDispatchers.io)

    private fun Dispatcher.cancelCallsByTag(tag: String) {
        queuedCalls().cancelCallsByTag(tag)
        runningCalls().cancelCallsByTag(tag)
    }

    private fun List<Call>.cancelCallsByTag(tag: String) {
        forEach { call ->
            if (call.request().tag() == tag && !call.isCanceled()) {
                call.cancel()
            }
        }
    }
}
