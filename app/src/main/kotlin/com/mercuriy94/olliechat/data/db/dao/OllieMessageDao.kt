@file:Suppress("TooManyFunctions")

package com.mercuriy94.olliechat.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageAiModelDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageDbEntity.StatusDb
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageDbEntityWithRelationships
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageTokenUsageDbEntity
import kotlinx.coroutines.flow.Flow
import java.time.OffsetDateTime

@Dao
internal interface OllieMessageDao {

    @Transaction
    @Query(
        "SELECT * FROM ollie_messages " +
                "WHERE chat_id = :chatId " +
                "AND author = :author " +
                "ORDER BY datetime(created_at) DESC " +
                "LIMIT 1"
    )
    suspend fun getLastMessageByChatId(
        chatId: Long,
        author: OllieChatMessageDbEntity.AuthorDb,
    ): OllieChatMessageDbEntityWithRelationships?

    @Query(
        "SELECT * FROM ollie_messages " +
                "WHERE ollie_messages.chat_id = :chatId " +
                "ORDER BY datetime(created_at)"
    )
    suspend fun getChatMessages(chatId: Long): List<OllieChatMessageDbEntityWithRelationships>

    @Query("SELECT * FROM ollie_messages WHERE ollie_messages.id = :messageId ")
    fun getChatMessage(messageId: Long): OllieChatMessageDbEntity

    @Query(
        "SELECT * FROM ollie_messages " +
                "WHERE chat_id = :chatId " +
                "AND status NOT IN (:skipStatuses) " +
                "ORDER BY datetime(created_at) "
    )
    fun observeFinishedChatMessages(
        chatId: Long,
        skipStatuses: List<StatusDb> = listOf(StatusDb.PARTIAL, StatusDb.PENDING),
    ): Flow<List<OllieChatMessageDbEntityWithRelationships>>

    @Query("SELECT * FROM ollie_messages WHERE chat_id = :chatId AND id = :messageId")
    suspend fun getChatMessageById(chatId: Long, messageId: Long): OllieChatMessageDbEntityWithRelationships?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceMessages(messages: List<OllieChatMessageDbEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceMessage(message: OllieChatMessageDbEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceTokenUsage(tokenUsage: OllieChatMessageTokenUsageDbEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceTokenUsages(tokenUsages: List<OllieChatMessageTokenUsageDbEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceMessageAiModel(aiModel: OllieChatMessageAiModelDbEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceMessageAiModels(aiModels: List<OllieChatMessageAiModelDbEntity>)

    @Query(
        "UPDATE ollie_messages SET text = text || :text, " +
                "status = :status, " +
                "created_at = :createdAt WHERE " +
                "id = :messageId AND " +
                "status != :completedStatus AND " +
                "created_at < :createdAt "
    )
    suspend fun appendTextToChatAssistantMessage(
        messageId: Long,
        text: String,
        createdAt: OffsetDateTime,
        status: StatusDb,
        completedStatus: StatusDb = StatusDb.COMPLETED,
    ): Int

    @Query(
        "UPDATE ollie_messages SET status = :status, " +
                "created_at = :createdAt WHERE " +
                "id = :messageId AND " +
                "status != :completedStatus AND " +
                "created_at < :createdAt "
    )
    suspend fun updateChatMessageStatus(
        messageId: Long,
        status: StatusDb,
        createdAt: OffsetDateTime,
        completedStatus: StatusDb = StatusDb.COMPLETED,
    ): Int

    @Query(
        "UPDATE ollie_messages SET text = :text, " +
                "status = :status, " +
                "created_at = :createdAt WHERE " +
                "id = :messageId AND " +
                "status != :completedStatus AND " +
                "created_at < :createdAt "
    )
    suspend fun updateAssistantMessage(
        messageId: Long,
        text: String,
        createdAt: OffsetDateTime,
        status: StatusDb,
        completedStatus: StatusDb = StatusDb.COMPLETED,
    ): Int

    @Query("DELETE FROM ollie_messages WHERE chat_id = :chatId ")
    suspend fun deleteMessages(chatId: Long)

    @Transaction
    suspend fun saveMessageWithRelationships(message: OllieChatMessageDbEntityWithRelationships): Long {
        val messageId = insertOrReplaceMessage(message.message)
        message.tokenUsage?.let { insertOrReplaceTokenUsage(it.copy(messageId = messageId)) }
        message.aiModel?.let { insertOrReplaceMessageAiModel(it.copy(messageId = messageId)) }
        return messageId
    }

    @Transaction
    suspend fun saveMessagesWithRelationships(messages: List<OllieChatMessageDbEntityWithRelationships>) {
        insertOrReplaceMessages(messages.map { it.message })
        insertOrReplaceTokenUsages(messages.mapNotNull { it.tokenUsage })
        insertOrReplaceMessageAiModels(messages.mapNotNull { it.aiModel })
    }

    @Transaction
    suspend fun updateAssistantMessage(
        messageId: Long,
        text: String,
        createdAt: OffsetDateTime,
        status: StatusDb,
        completedStatus: StatusDb = StatusDb.COMPLETED,
        tokenUsage: OllieChatMessageTokenUsageDbEntity?,
    ) {
        updateAssistantMessage(messageId, text, createdAt, status, completedStatus)
        tokenUsage?.let { insertOrReplaceTokenUsage(it.copy(messageId = messageId)) }
    }

}
