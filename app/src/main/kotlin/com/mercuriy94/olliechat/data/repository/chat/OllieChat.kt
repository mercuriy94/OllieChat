package com.mercuriy94.olliechat.data.repository.chat

import com.mercuriy94.olliechat.data.repository.chat.manager.OllieChatAssistantManager
import com.mercuriy94.olliechat.data.repository.chat.manager.OllieChatAssistantSourceProvider
import com.mercuriy94.olliechat.data.repository.chat.manager.OllieChatAssistantSourceProviderImpl
import com.mercuriy94.olliechat.data.repository.chat.memory.OllieChatMemoryId
import com.mercuriy94.olliechat.data.repository.chat.title.TitleGenerator
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity.AssistantMessageEntity
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity.UserMessageEntity
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageStreamingReply
import com.mercuriy94.olliechat.domain.repository.chat.OllieChatRepository
import com.mercuriy94.olliechat.domain.repository.chat.OllieMessageRepository
import com.mercuriy94.olliechat.domain.repository.model.OllieModelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

internal interface OllieChat {

    val chatId: Long

    fun observeTitle(): Flow<String>

    suspend fun getMessageById(
        messageId: Long,
    ): OllieChatMessageEntity?

    fun sendMessage(
        messageId: Long,
        aiModelId: Long,
    ): Flow<OllieChatMessageStreamingReply>

    suspend fun generateTitle(modelId: Long)
}

internal class OllieChatImpl(
    override val chatId: Long,
    private val chatRepository: OllieChatRepository,
    private val messageRepository: OllieMessageRepository,
    private val modelRepository: OllieModelRepository,
    private val titleGenerator: TitleGenerator,
    ollieChatAssistantManager: OllieChatAssistantManager,
) : OllieChat {

    private val ollieChatAssistantSourceProvider: OllieChatAssistantSourceProvider =
        OllieChatAssistantSourceProviderImpl(ollieChatAssistantManager)

    override fun observeTitle(): Flow<String> = chatRepository.observeChatTitle(chatId)

    override suspend fun getMessageById(messageId: Long): OllieChatMessageEntity? {
        return messageRepository.getMessageById(chatId = chatId, messageId = messageId)
    }

    override fun sendMessage(
        messageId: Long,
        aiModelId: Long,
    ): Flow<OllieChatMessageStreamingReply> {
        return flow {
            val chatMemoryId = createMemoryChatId(userMessageId = messageId, aiModelId = aiModelId)
            val message = requireNotNull(getMessageById(messageId)) {
                "Couldn't find message with id = $messageId!"
            }
            val text = when (message) {
                is UserMessageEntity -> message.text
                is AssistantMessageEntity -> message.text
            }

            val realChatFlow = ollieChatAssistantSourceProvider(aiModelId)
                .getAssistant()
                .chat(chatMemoryId = chatMemoryId, text = text)

            emitAll(realChatFlow)
        }
    }

    override suspend fun generateTitle(modelId: Long) {
        titleGenerator.generateTitle(
            modelId = modelId,
            chatId = chatId
        )
    }


    private suspend fun createMemoryChatId(
        userMessageId: Long,
        aiModelId: Long,
    ): OllieChatMemoryId {
        val model = requireNotNull(modelRepository.getModelById(aiModelId)) {
            "Couldn't find model with id = $aiModelId!"
        }
        return OllieChatMemoryId(
            chatId = chatId,
            userMessageId = userMessageId,
            assistantMessageId = messageRepository.saveNewAssistantMessage(
                chatId = chatId,
                userMessageId = userMessageId,
                AssistantMessageEntity.AiModel(
                    aiModelId = model.id,
                    name = model.name,
                )
            ),
        )
    }
}
