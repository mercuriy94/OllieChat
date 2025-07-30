package com.mercuriy94.olliechat.domain.repository.chat

import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity.AssistantMessageEntity
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity.UserMessageEntity
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageTokenUsage
import kotlinx.coroutines.flow.Flow

internal interface OllieMessageRepository {

    suspend fun getLastAssistantMessageByChatId(chatId: Long): AssistantMessageEntity?

    fun observeFinishedMessages(chatId: Long): Flow<List<OllieChatMessageEntity>>

    suspend fun saveNewUserMessage(chatId: Long, text: String): Long

    suspend fun getMessageById(chatId: Long, messageId: Long): OllieChatMessageEntity?

    suspend fun saveNewAssistantMessage(
        chatId: Long,
        userMessageId: Long,
        aiModel: AssistantMessageEntity.AiModel,
    ): Long

    suspend fun updateUserMessageStatus(messageId: Long, status: UserMessageEntity.Status)

    suspend fun appendPartialResponseToAssistantMessage(messageId: Long, partialResponse: String)

    suspend fun updateAssistantMessageStatus(messageId: Long, status: AssistantMessageEntity.Status)

    suspend fun updateAssistantMessage(
        messageId: Long,
        text: String,
        status: AssistantMessageEntity.Status,
        tokenUsage: OllieChatMessageTokenUsage? = null,
    )
}
