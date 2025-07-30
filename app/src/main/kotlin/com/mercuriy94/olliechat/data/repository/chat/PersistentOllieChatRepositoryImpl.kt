package com.mercuriy94.olliechat.data.repository.chat

import com.mercuriy94.olliechat.data.db.dao.OllieChatDao
import com.mercuriy94.olliechat.data.db.model.OllieChatDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageDbEntityWithRelationships
import com.mercuriy94.olliechat.data.mapper.chat.OllieChatMessageDbToDomainEntityMapper
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatEntity
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatWithLatestMessage
import com.mercuriy94.olliechat.domain.repository.chat.OllieChatRepository
import com.mercuriy94.olliechat.utils.CoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext

internal class PersistentOllieChatRepositoryImpl(
    private val coroutineDispatchers: CoroutineDispatchers,
    private val messageDbToDomainEntityMapper: OllieChatMessageDbToDomainEntityMapper,
    private val ollieChatDao: OllieChatDao,
) : OllieChatRepository {

    override fun observeChatTitle(chatId: Long): Flow<String> {
        return ollieChatDao.observeChatTitle(chatId)
            .flowOn(coroutineDispatchers.io)
    }

    override suspend fun createNewChat(): Long {
        return withContext(coroutineDispatchers.io) {
            ollieChatDao.insertOrReplaceChat(OllieChatDbEntity(title = "New Chat"))
        }
    }

    override suspend fun getChatById(id: Long): OllieChatEntity {
        return withContext(coroutineDispatchers.io) {
            val dbChat = ollieChatDao.getChatById(id)
            OllieChatEntity(id = dbChat.id, title = dbChat.title)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeChats(): Flow<List<OllieChatWithLatestMessage>> =
        ollieChatDao.observeChats()
            .mapLatest { chats ->
                chats.map { chatWithLatestMessage ->
                    OllieChatWithLatestMessage(
                        chat = OllieChatEntity(
                            id = chatWithLatestMessage.chat.id,
                            title = chatWithLatestMessage.chat.title
                        ),
                        latestMessage = messageDbToDomainEntityMapper(
                            OllieChatMessageDbEntityWithRelationships(
                                message = chatWithLatestMessage.latestMessage,
                                aiModel = chatWithLatestMessage.messageAiModel
                            )
                        )
                    )
                }
            }.flowOn(coroutineDispatchers.io)

    override suspend fun updateChatTitle(chatId: Long, title: String) {
        withContext(coroutineDispatchers.io) {
            ollieChatDao.updateChatTitle(chatId = chatId, title = title)
        }
    }
}
