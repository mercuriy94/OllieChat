package com.mercuriy94.olliechat.data.repository.chat.memory

import com.mercuriy94.olliechat.data.db.dao.OllieMessageDao
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageDbEntity.AuthorDb
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageDbEntityWithRelationships
import com.mercuriy94.olliechat.data.langchain4j.chat.OllieChatAiMessage
import com.mercuriy94.olliechat.data.langchain4j.chat.OllieChatUserMessage
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.ChatMessageType
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.store.memory.chat.ChatMemoryStore
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.time.OffsetDateTime

private const val TAG = "PersistentOllieChatMemoryLocalStore"

internal class PersistentOllieChatMemoryLocalStore(
    private val ollieMessageDao: OllieMessageDao,
) : ChatMemoryStore {

    override fun getMessages(memoryId: Any?): List<ChatMessage> {
        Timber.tag(TAG).d("called getMessages")
        val chatMemoryId = requireOllieChatMemoryId(memoryId)
        return runBlocking {
            ollieMessageDao.getChatMessages(chatMemoryId.chatId)
                .filterNot { dbMessage ->
                    (dbMessage.message.id == chatMemoryId.userMessageId ||
                            dbMessage.message.id == chatMemoryId.assistantMessageId)
                            && dbMessage.message.status == OllieChatMessageDbEntity.StatusDb.PENDING
                }
                .map { dbMessage ->
                    when (dbMessage.message.author) {
                        AuthorDb.USER -> OllieChatUserMessage(dbMessage = dbMessage)
                        AuthorDb.ASSISTANT -> OllieChatAiMessage(dbMessage = dbMessage)
                    }
                }

        }
    }

    override fun updateMessages(memoryId: Any?, messages: List<ChatMessage?>?) {
        Timber.tag(TAG).d("called updateMessages")
        val chatMemoryId = requireOllieChatMemoryId(memoryId)
        val dbMessages = messages?.filterNotNull()?.map { ollamaMessage ->
            when (ollamaMessage.type()) {
                ChatMessageType.USER -> {
                    when (ollamaMessage) {
                        is OllieChatUserMessage -> ollamaMessage.dbMessage

                        is UserMessage -> {
                            val originalMessage = ollieMessageDao.getChatMessage(
                                messageId = chatMemoryId.userMessageId
                            )
                            OllieChatMessageDbEntityWithRelationships(
                                OllieChatMessageDbEntity(
                                    id = chatMemoryId.userMessageId,
                                    chatId = chatMemoryId.chatId,
                                    author = AuthorDb.USER,
                                    text = ollamaMessage.singleText(),
                                    status = OllieChatMessageDbEntity.StatusDb.SENT,
                                    createdAt = originalMessage.createdAt
                                )
                            )
                        }

                        else -> {
                            throw UnsupportedOperationException("System messages are not supported")
                        }
                    }
                }

                ChatMessageType.AI -> {
                    when (ollamaMessage) {
                        is OllieChatAiMessage -> ollamaMessage.dbMessage

                        is AiMessage -> {
                            OllieChatMessageDbEntityWithRelationships(
                                OllieChatMessageDbEntity(
                                    id = chatMemoryId.assistantMessageId,
                                    chatId = chatMemoryId.chatId,
                                    author = AuthorDb.ASSISTANT,
                                    text = ollamaMessage.text(),
                                    status = OllieChatMessageDbEntity.StatusDb.COMPLETED,
                                    createdAt = OffsetDateTime.now(),
                                    userMessageId = chatMemoryId.userMessageId,
                                )
                            )
                        }

                        else -> {
                            throw UnsupportedOperationException("System messages are not supported")
                        }
                    }
                }

                ChatMessageType.SYSTEM,
                ChatMessageType.TOOL_EXECUTION_RESULT,
                ChatMessageType.CUSTOM,
                    -> {
                    throw UnsupportedOperationException("messages are not supported")
                }
            }
        }

        dbMessages?.also {
            runBlocking {
                ollieMessageDao.saveMessagesWithRelationships(messages = it)
            }
        }
    }

    override fun deleteMessages(memoryId: Any?) {
        Timber.tag(TAG).d("called deleteMessages")
        runBlocking {
            ollieMessageDao.deleteMessages(requireOllieChatMemoryId(memoryId).chatId)
        }
    }

    private fun requireOllieChatMemoryId(memoryId: Any?): OllieChatMemoryId {
        return requireNotNull(memoryId as? OllieChatMemoryId) { "memoryId must be a OllieChatMemoryId" }
    }
}
