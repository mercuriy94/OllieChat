package com.mercuriy94.olliechat.presentation.feature.chat

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.mercuriy94.olliechat.data.db.model.OllieChatWorkDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieChatWorkTaskDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieChatWorkTaskDbEntity.TypeDbEntity
import com.mercuriy94.olliechat.data.repository.work.OllieChatWorkPartialResponse
import com.mercuriy94.olliechat.data.repository.work.OllieChatWorkProgressManager
import com.mercuriy94.olliechat.data.repository.work.OllieChatWorksRepository
import com.mercuriy94.olliechat.data.workmanager.OllieChatWorker
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity.AssistantMessageEntity
import com.mercuriy94.olliechat.domain.repository.chat.OllieChatRepository
import com.mercuriy94.olliechat.domain.repository.chat.OllieMessageRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.UUID

internal class OllieChatWorkManager(
    private val chatRepository: OllieChatRepository,
    private val messageRepository: OllieMessageRepository,
    private val chatWorksRepository: OllieChatWorksRepository,
    private val chatWorkProgressManager: OllieChatWorkProgressManager,
    private val workManager: WorkManager,
) {
    companion object {
        private const val TAG = "OllieChatWorkManager"
    }

    sealed interface UserMessageToSend {
        data class PendingMessage(val messageId: Long) : UserMessageToSend
        data class NewMessage(val text: String) : UserMessageToSend
    }

    fun observeChatTitle(chatId: Long): Flow<String> {
        return chatRepository.observeChatTitle(chatId)
    }

    fun observeFinishedMessages(chatId: Long): Flow<List<OllieChatMessageEntity>> {
        return messageRepository.observeFinishedMessages(chatId)
    }

    suspend fun getLastAssistantMessageByChatId(chatId: Long): AssistantMessageEntity? {
        return messageRepository.getLastAssistantMessageByChatId(chatId)
    }

    suspend fun getMessageById(chatId: Long, messageId: Long): OllieChatMessageEntity? {
        return messageRepository.getMessageById(chatId = chatId, messageId = messageId)
    }

    suspend fun sendMessage(chatId: Long, message: UserMessageToSend, aiModelId: Long) {
        val (userMessageId, generateTitle) = when (message) {
            is UserMessageToSend.NewMessage -> messageRepository.saveNewUserMessage(chatId, message.text) to false
            is UserMessageToSend.PendingMessage -> message.messageId to true
        }

        doChat(
            chatId = chatId,
            aiModelId = aiModelId,
            userMessageId = userMessageId,
            generateTitle = generateTitle,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeActiveStreamingChats(chatId: Long): Flow<List<OllieChatWorkPartialResponse>> {
        return chatWorksRepository.observeWorksByChatId(chatId)
            .map { works -> works.filter { (_, task) -> task.type == TypeDbEntity.DO_CHAT } }
            .flatMapLatest { works ->
                Timber.tag(TAG).d("observeActiveStreamingChats: loaded works: ${works.size}")
                if (works.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    flow {
                        val flow = works.map { (work, _) -> UUID.fromString(work.workRequestId) }
                            .let { workRequestIds -> workManager.getWorkInfosFlow(WorkQuery.fromIds(workRequestIds)) }
                            .map { workInfos ->
                                workInfos
                                    .filter { workInfo -> !workInfo.state.isFinished }
                                    .map { workInfo ->
                                        chatWorkProgressManager.getOrCreateChatProgressFlow(
                                            workRequestId = workInfo.id.toString()
                                        )
                                    }
                            }.flatMapLatest { workInfos ->
                                Timber.tag(TAG)
                                    .d("observeActiveStreamingChats: works running: ${works.size}")
                                if (workInfos.isEmpty()) {
                                    flowOf(emptyList())
                                } else {
                                    combine(workInfos) { workInfosArray -> workInfosArray.toList() }
                                }
                            }
                        emitAll(flow)
                    }
                }

            }
    }

    suspend fun doChat(
        chatId: Long,
        aiModelId: Long,
        userMessageId: Long,
        generateTitle: Boolean,
    ): String {
        val workRequestId = UUID.randomUUID()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<OllieChatWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setId(workRequestId)
            .addTag("App_Chat_Worker")
            .setConstraints(constraints)
            .build()

        chatWorksRepository.saveWork(
            OllieChatWorkDbEntity(
                workRequestId = workRequest.id.toString(),
                chatId = chatId,
                workRequestTag = "App_Chat_Worker"
            ),
            OllieChatWorkTaskDbEntity(
                modelId = aiModelId,
                userMessageId = userMessageId,
                type = TypeDbEntity.DO_CHAT,
            )
        )

        workManager.beginUniqueWork(
            uniqueWorkName = "chat_work_with_chat_id_${chatId}_message_id_${userMessageId}_model_id_${aiModelId}",
            existingWorkPolicy = ExistingWorkPolicy.REPLACE,
            request = workRequest
        ).let {
            if (generateTitle) {
                val workRequestId = UUID.randomUUID()

                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                chatWorksRepository.saveWork(
                    OllieChatWorkDbEntity(
                        workRequestId = workRequestId.toString(),
                        chatId = chatId,
                        workRequestTag = "App_Chat_Worker"
                    ),
                    OllieChatWorkTaskDbEntity(
                        modelId = aiModelId,
                        type = TypeDbEntity.GENERATE_TITLE,
                    )
                )

                val workRequest = OneTimeWorkRequestBuilder<OllieChatWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .setId(workRequestId)
                    .addTag("App_Chat_Worker")
                    .setConstraints(constraints)
                    .build()

                it.then(workRequest)
            } else it
        }.enqueue()

        return workRequestId.toString()
    }

    suspend fun generateTitle(chatId: Long, aiModelId: Long): String {
        val workRequestId = UUID.randomUUID()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        chatWorksRepository.saveWork(
            OllieChatWorkDbEntity(
                workRequestId = workRequestId.toString(),
                chatId = chatId,
                workRequestTag = "App_Chat_Worker"
            ),
            OllieChatWorkTaskDbEntity(
                modelId = aiModelId,
                type = TypeDbEntity.GENERATE_TITLE,
            )
        )

        val workRequest = OneTimeWorkRequestBuilder<OllieChatWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setId(workRequestId)
            .addTag("App_Chat_Worker")
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            uniqueWorkName = "generate_title_chat_id_${chatId}}_model_id_${aiModelId}",
            existingWorkPolicy = ExistingWorkPolicy.REPLACE,
            request = workRequest
        )

        return workRequestId.toString()
    }

    suspend fun stopCurrentStreamingChat(chatId: Long) {
        val workRequestIds = chatWorksRepository.getWorksByChatId(chatId)
            .map { it.work.workRequestId }

        workRequestIds.forEach { workRequestId ->
            workManager.cancelWorkById(UUID.fromString(workRequestId))
        }

        chatWorksRepository.deleteWorks(workRequestIds)
    }

}
