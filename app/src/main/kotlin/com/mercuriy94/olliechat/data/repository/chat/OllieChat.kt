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
        userMessageId: Long,
        assistantMessageId: Long,
        aiModelId: Long,
    ): Flow<OllieChatMessageStreamingReply>

    suspend fun generateTitle(modelId: Long)
}

internal class OllieChatImpl(
    override val chatId: Long,
    private val chatRepository: OllieChatRepository,
    private val messageRepository: OllieMessageRepository,
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
        userMessageId: Long,
        assistantMessageId: Long,
        aiModelId: Long,
    ): Flow<OllieChatMessageStreamingReply> {
        return flow {
            val chatMemoryId = createMemoryChatId(
                userMessageId = userMessageId,
                assistantMessageId = assistantMessageId
            )
            val message = requireNotNull(getMessageById(userMessageId)) {
                "Couldn't find message with id = $userMessageId!"
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


    private fun createMemoryChatId(
        assistantMessageId: Long,
        userMessageId: Long,
    ): OllieChatMemoryId {
        return OllieChatMemoryId(
            chatId = chatId,
            userMessageId = userMessageId,
            assistantMessageId = assistantMessageId,
        )
    }
}
