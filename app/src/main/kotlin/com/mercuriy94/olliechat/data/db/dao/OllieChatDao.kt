package com.mercuriy94.olliechat.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import com.mercuriy94.olliechat.data.db.model.OllieChatDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieChatDbEntityWithLatestMessage
import kotlinx.coroutines.flow.Flow

@Dao
internal interface OllieChatDao {

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        """
    SELECT * FROM (
        SELECT
            c.id,
            c.title,
            m.id AS latest_message_id,
            m.chat_id AS latest_message_chat_id,
            m.text AS latest_message_text,
            m.author AS latest_message_author,
            m.created_at AS latest_message_created_at,
            m.status AS latest_message_status,
            m.user_massage_id AS latest_message_user_massage_id,
            m_ai.message_id AS message_ai_model_message_id,
            m_ai.id AS message_ai_model_id,
            m_ai.ai_model_id AS message_ai_model_ai_model_id,
            m_ai.name AS message_ai_model_name,
            ROW_NUMBER() OVER (PARTITION BY c.id ORDER BY m.created_at DESC, m.id DESC) as rn
        FROM ollie_chats AS c
        INNER JOIN ollie_messages AS m ON c.id = m.chat_id
        LEFT JOIN (
            SELECT 
                message_id,
                id,
                ai_model_id,
                name,
                ROW_NUMBER() OVER (PARTITION BY message_id ORDER BY id DESC) AS rn_ai
            FROM ollie_messages_ai_models
        ) AS m_ai ON m_ai.message_id = m.id AND m_ai.rn_ai = 1
    )
    WHERE rn = 1
    ORDER BY latest_message_created_at DESC
    """
    )
    fun observeChats(): Flow<List<OllieChatDbEntityWithLatestMessage>>

    @Query("UPDATE ollie_chats SET title = :title WHERE id = :chatId ")
    suspend fun updateChatTitle(chatId: Long, title: String): Int

    @Query("SELECT ollie_chats.title FROM ollie_chats WHERE ollie_chats.id = :chatId ")
    fun observeChatTitle(chatId: Long): Flow<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceChat(chat: OllieChatDbEntity): Long

    @Query("SELECT * FROM ollie_chats WHERE ollie_chats.id = :id ")
    suspend fun getChatById(id: Long): OllieChatDbEntity

}
