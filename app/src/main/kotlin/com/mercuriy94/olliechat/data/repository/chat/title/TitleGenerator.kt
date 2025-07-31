package com.mercuriy94.olliechat.data.repository.chat.title

import com.mercuriy94.olliechat.data.langchain4j.TitleGenerationAssistant
import com.mercuriy94.olliechat.data.langchain4j.http.Langchain4jOkHttpClientBuilder
import com.mercuriy94.olliechat.di.NetworkModule.OLLAMA_HOST
import com.mercuriy94.olliechat.di.NetworkModule.OLLAMA_PORT
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity.AssistantMessageEntity
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity.UserMessageEntity
import com.mercuriy94.olliechat.domain.entity.model.OllieModel
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param.Key.MIN_P
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param.Key.NUM_CTX
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param.Key.REPEAT_PENALTY
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param.Key.TEMPERATURE
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param.Key.TOP_K
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param.Key.TOP_P
import com.mercuriy94.olliechat.domain.repository.chat.OllieChatRepository
import com.mercuriy94.olliechat.domain.repository.chat.OllieMessageRepository
import com.mercuriy94.olliechat.domain.repository.model.OllieModelRepository
import com.mercuriy94.olliechat.utils.CoroutineDispatchers
import com.mercuriy94.olliechat.utils.ext.buildAiServices
import dev.langchain4j.data.message.AiMessage.aiMessage
import dev.langchain4j.data.message.UserMessage.userMessage
import dev.langchain4j.model.chat.Capability
import dev.langchain4j.model.chat.request.ResponseFormat
import dev.langchain4j.model.ollama.OllamaChatModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import timber.log.Timber
import java.time.Duration

private const val DEFAULT_HTTP_TIMEOUT_SEC = 1200L
const val DEFAULT_TITLE_GENERATION_PROMPT = """### Task:
            Generate a concise, 3-5 word title with an emoji summarizing the chat history.
            ### Guidelines:
            - The title should clearly represent the main theme or subject of the conversation.
            - Use emojis that enhance understanding of the topic, but avoid quotation marks or special formatting.
            - Write the title in the chat's primary language; default to English if multilingual.
            - Prioritize accuracy over excessive creativity; keep it clear and simple.
            ### Output:
            JSON format: { "title": "your concise title here" }
            ### Examples:
            - { "title": "📉 Stock Market Trends" },
            - { "title": "🍪 Perfect Chocolate Chip Recipe" },
            - { "title": "Evolution of Music Streaming" },
            - { "title": "Remote Work Productivity Tips" },
            - { "title": "Artificial Intelligence in Healthcare" },
            - { "title": "🎮 Video Game Development Insights" }
          ### Chat History:
          <chat_history>
          {{MESSAGES:END:2}}
          </chat_history>"""

internal class TitleGenerator(
    private val okHttpClient: OkHttpClient,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val modelRepository: OllieModelRepository,
    private val chatRepository: OllieChatRepository,
    private val messageRepository: OllieMessageRepository,
) {

    suspend fun generateTitle(
        modelId: Long,
        chatId: Long,
    ) {

        withContext(coroutineDispatchers.io) {
            val model = modelRepository.getModelById(modelId) ?: return@withContext

            try {

                val storedMessages = messageRepository.observeFinishedMessages(chatId)
                    .firstOrNull()
                    ?.map {
                        when (it) {
                            is AssistantMessageEntity -> aiMessage(it.text)
                            is UserMessageEntity -> userMessage(it.text)
                        }
                    }

                val assistant = buildAiServices<TitleGenerationAssistant> {
                    val chatModel = OllamaChatModel.builder()
                        .baseUrl("$OLLAMA_HOST:$OLLAMA_PORT")
                        .timeout(Duration.ofSeconds(DEFAULT_HTTP_TIMEOUT_SEC))
                        .httpClientBuilder(
                            Langchain4jOkHttpClientBuilder(okHttpClient.newBuilder())
                                .requestTag("title_generator")
                        )
                        .responseFormat(ResponseFormat.JSON)
                        .supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
                        .modelName(model.name)
                        .addModelParameters(model)
                        .build()

                    chatModel(chatModel)
                }

                val title = assistant.generateTitle(storedMessages ?: emptyList())

                title.title?.let {
                    chatRepository.updateChatTitle(chatId, it)
                }

            } catch (ignoreException: Exception) {
                Timber.tag("TitleGenerator").e(ignoreException)
            }
        }

    }

    private fun OllamaChatModel.OllamaChatModelBuilder.addModelParameters(
        model: OllieModel,
    ): OllamaChatModel.OllamaChatModelBuilder {
        return apply {
            model.params.forEach { param ->
                when (param.key) {
                    TOP_K -> {
                        (param.value as? OllieModel.Param.Value.IntValue)
                            ?.value?.also(::topK)
                    }

                    TOP_P -> {
                        (param.value as? OllieModel.Param.Value.FloatValue)
                            ?.value?.toBigDecimal()?.toDouble()?.also(::topP)
                    }

                    MIN_P -> {
                        (param.value as? OllieModel.Param.Value.FloatValue)
                            ?.value?.toBigDecimal()?.toDouble()?.also(::minP)
                    }

                    TEMPERATURE -> {
                        (param.value as? OllieModel.Param.Value.FloatValue)
                            ?.value?.toBigDecimal()?.toDouble()?.also(::temperature)
                    }

                    NUM_CTX -> {
                        (param.value as? OllieModel.Param.Value.IntValue)?.value?.also(::numCtx)
                    }

                    REPEAT_PENALTY -> {
                        (param.value as? OllieModel.Param.Value.FloatValue)
                            ?.value?.toBigDecimal()?.toDouble()?.also(::repeatPenalty)
                    }
                }
            }
        }
    }
}
