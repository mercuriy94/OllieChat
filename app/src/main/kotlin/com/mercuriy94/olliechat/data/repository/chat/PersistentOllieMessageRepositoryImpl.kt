package com.mercuriy94.olliechat.data.repository.chat

import com.mercuriy94.olliechat.data.db.dao.OllieMessageDao
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageAiModelDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageDbEntity.AuthorDb
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageDbEntity.StatusDb
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageDbEntityWithRelationships
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageTokenUsageDbEntity
import com.mercuriy94.olliechat.data.mapper.chat.OllieChatMessageDbToDomainEntityMapper
import com.mercuriy94.olliechat.data.mapper.chat.OllieChatMessageDomainToDbEntityMapper
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity.AssistantMessageEntity
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity.UserMessageEntity
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageTokenUsage
import com.mercuriy94.olliechat.domain.repository.chat.OllieMessageRepository
import com.mercuriy94.olliechat.utils.CoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.OffsetDateTime

internal class PersistentOllieMessageRepositoryImpl(
    private val coroutineDispatchers: CoroutineDispatchers,
    private val messageDbToDomainEntityMapper: OllieChatMessageDbToDomainEntityMapper,
    private val messageDomainToDbEntityMapper: OllieChatMessageDomainToDbEntityMapper,
    private val ollieMessageDao: OllieMessageDao,
) : OllieMessageRepository {

    override suspend fun saveNewUserMessage(chatId: Long, text: String): Long {
        return withContext(coroutineDispatchers.io) {
            ollieMessageDao.insertOrReplaceMessage(
                OllieChatMessageDbEntity(
                    text = text,
                    chatId = chatId,
                    author = AuthorDb.USER,
                    status = StatusDb.CREATED,
                    createdAt = OffsetDateTime.now()
                )
            )
        }
    }

    override suspend fun saveNewAssistantMessage(
        chatId: Long,
        userMessageId: Long,
        aiModel: AssistantMessageEntity.AiModel,
    ): Long {
        return withContext(coroutineDispatchers.io) {
            ollieMessageDao.saveMessageWithRelationships(
                OllieChatMessageDbEntityWithRelationships(
                    message = OllieChatMessageDbEntity(
                        text = "",
                        chatId = chatId,
                        author = AuthorDb.ASSISTANT,
                        status = StatusDb.PENDING,
                        createdAt = OffsetDateTime.now(),
                        userMessageId = userMessageId,
                    ),
                    tokenUsage = null,
                    aiModel = OllieChatMessageAiModelDbEntity(
                        aiModelId = aiModel.aiModelId,
                        name = aiModel.name,
                    )
                )
            )
        }
    }

    override suspend fun updateAssistantMessage(
        messageId: Long,
        text: String,
        status: AssistantMessageEntity.Status,
        tokenUsage: OllieChatMessageTokenUsage?,
    ) {
        withContext(coroutineDispatchers.io) {
            ollieMessageDao.updateAssistantMessage(
                messageId = messageId,
                text = text,
                createdAt = OffsetDateTime.now(),
                status = messageDomainToDbEntityMapper.mapDomainStatusToDbStatus(status),
                tokenUsage = tokenUsage?.let {
                    OllieChatMessageTokenUsageDbEntity(
                        id = tokenUsage.id,
                        messageId = messageId,
                        inputTokenCount = tokenUsage.inputTokenCount,
                        outputTokenCount = tokenUsage.outputTokenCount,
                        totalTokenCount = tokenUsage.totalTokenCount,
                    )
                }
            )
        }
    }

    override suspend fun getLastAssistantMessageByChatId(chatId: Long): AssistantMessageEntity? {
        return withContext(coroutineDispatchers.io) {
            ollieMessageDao.getLastMessageByChatId(chatId = chatId, author = AuthorDb.ASSISTANT)
                ?.let(messageDbToDomainEntityMapper::invoke) as? AssistantMessageEntity
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeFinishedMessages(chatId: Long): Flow<List<OllieChatMessageEntity>> =
        ollieMessageDao.observeFinishedChatMessages(chatId)
            .map { messageDbToDomainEntityMapper(it) }
            .flowOn(coroutineDispatchers.io)

    override suspend fun getMessageById(chatId: Long, messageId: Long): OllieChatMessageEntity? {
        return withContext(coroutineDispatchers.io) {
            ollieMessageDao.getChatMessageById(chatId = chatId, messageId = messageId)
                ?.let { messageDbToDomainEntityMapper(it) }
        }
    }

    override suspend fun appendPartialResponseToAssistantMessage(messageId: Long, partialResponse: String) {
        withContext(coroutineDispatchers.io) {
            ollieMessageDao.appendTextToChatAssistantMessage(
                messageId = messageId,
                text = partialResponse,
                createdAt = OffsetDateTime.now(),
                status = StatusDb.PARTIAL
            )
        }
    }

    override suspend fun updateUserMessageStatus(messageId: Long, status: UserMessageEntity.Status) {
        withContext(coroutineDispatchers.io) {
            ollieMessageDao.updateChatMessageStatus(
                messageId = messageId,
                createdAt = OffsetDateTime.now(),
                status = messageDomainToDbEntityMapper.mapDomainStatusToDbStatus(status)
            )
        }
    }

    override suspend fun updateAssistantMessageStatus(messageId: Long, status: AssistantMessageEntity.Status) {
        withContext(coroutineDispatchers.io) {
            ollieMessageDao.updateChatMessageStatus(
                messageId = messageId,
                createdAt = OffsetDateTime.now(),
                status = messageDomainToDbEntityMapper.mapDomainStatusToDbStatus(status),
            )
        }
    }

}
