package com.mercuriy94.olliechat.data.repository.work

import com.mercuriy94.olliechat.data.db.dao.OllieChatWorkDao
import com.mercuriy94.olliechat.data.db.model.OllieChatWorkDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieChatWorkDbEntityWithTask
import com.mercuriy94.olliechat.data.db.model.OllieChatWorkTaskDbEntity
import com.mercuriy94.olliechat.utils.CoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

internal class OllieChatWorksRepository(
    private val ollieChatWorksDao: OllieChatWorkDao,
    private val coroutineDispatchers: CoroutineDispatchers,
) {

    suspend fun saveWork(
        work: OllieChatWorkDbEntity,
        task: OllieChatWorkTaskDbEntity,
    ) {
        withContext(coroutineDispatchers.io) {
            ollieChatWorksDao.insertOrReplaceChatWork(
                OllieChatWorkDbEntityWithTask(work = work, task = task)
            )
        }
    }

    suspend fun getAllChatWorkRequestIds(): List<String>{
        return withContext(coroutineDispatchers.io) {
            ollieChatWorksDao.getAllChatWorkRequestIds()
        }
    }

    suspend fun getWorkByRequestId(workRequestId: String): OllieChatWorkDbEntityWithTask? {
        return withContext(coroutineDispatchers.io) {
            ollieChatWorksDao.getChatWork(workRequestId = workRequestId)
        }
    }

    fun observeWorksByChatId(chatId: Long): Flow<List<OllieChatWorkDbEntityWithTask>> {
        return ollieChatWorksDao.observeChatWorks(chatId = chatId)
            .flowOn(coroutineDispatchers.io)
    }

    suspend fun getWorksByChatId(chatId: Long): List<OllieChatWorkDbEntityWithTask> {
        return withContext(coroutineDispatchers.io) {
            ollieChatWorksDao.getChatWorks(chatId = chatId)
        }
    }

    suspend fun deleteWork(workRequestId: String) {
        withContext(coroutineDispatchers.io) {
            ollieChatWorksDao.deleteChatWork(workRequestId = workRequestId)
        }
    }

    suspend fun deleteWorks(workRequestId: List<String>) {
        withContext(coroutineDispatchers.io) {
            ollieChatWorksDao.deleteChatWorks(workRequestIds = workRequestId)
        }
    }

}
