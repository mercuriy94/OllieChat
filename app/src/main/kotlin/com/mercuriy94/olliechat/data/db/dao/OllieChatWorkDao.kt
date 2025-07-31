package com.mercuriy94.olliechat.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mercuriy94.olliechat.data.db.model.OllieChatWorkDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieChatWorkDbEntityWithTask
import com.mercuriy94.olliechat.data.db.model.OllieChatWorkTaskDbEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface OllieChatWorkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceChatWork(chatWork: OllieChatWorkDbEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceChatWorkTask(task: OllieChatWorkTaskDbEntity): Long

    @Query("SELECT work_request_id FROM ollie_chat_works")
    suspend fun getAllChatWorkRequestIds(): List<String>

    @Query("SELECT * FROM ollie_chat_works WHERE work_request_id = :workRequestId")
    suspend fun getChatWork(workRequestId: String): OllieChatWorkDbEntityWithTask?

    @Query("SELECT * FROM ollie_chat_works WHERE chat_id = :chatId")
    fun observeChatWorks(chatId: Long): Flow<List<OllieChatWorkDbEntityWithTask>>

    @Query("SELECT * FROM ollie_chat_works WHERE chat_id = :chatId")
    suspend fun getChatWorks(chatId: Long): List<OllieChatWorkDbEntityWithTask>

    @Query("DELETE FROM ollie_chat_works WHERE work_request_id = :workRequestId")
    suspend fun deleteChatWork(workRequestId: String)

    @Query("DELETE FROM ollie_chat_works WHERE work_request_id in (:workRequestIds)")
    suspend fun deleteChatWorks(workRequestIds: List<String>)

    @Transaction
    suspend fun insertOrReplaceChatWork(chatWork: OllieChatWorkDbEntityWithTask) {
        val workId = insertOrReplaceChatWork(chatWork.work)
        insertOrReplaceChatWorkTask(chatWork.task.copy(workId = workId))
    }

}
