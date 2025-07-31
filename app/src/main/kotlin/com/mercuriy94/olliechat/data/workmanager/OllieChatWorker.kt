package com.mercuriy94.olliechat.data.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mercuriy94.olliechat.data.db.model.OllieChatWorkTaskDbEntity.TypeDbEntity
import com.mercuriy94.olliechat.data.repository.work.OllieChatWorkPartialResponse
import com.mercuriy94.olliechat.di.CoroutineDispatchersModule
import com.mercuriy94.olliechat.di.OllieChatDataModule
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity.AssistantMessageEntity
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity.UserMessageEntity
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageStreamingReply.CompleteResponse
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageStreamingReply.Error
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageStreamingReply.PartialResponse
import com.mercuriy94.olliechat.utils.ext.runSuspendCatching
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.chunked
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.system.measureTimeMillis

internal class OllieChatWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(
    appContext = appContext,
    params = params
) {

    companion object {
        private const val TAG = "OllieChatWorker"
        private const val CHAT_TOKENS_BATCH_SIZE = 16
    }

    private val coroutineDispatchers = CoroutineDispatchersModule.coroutineDispatchers
    private val messageRepository = OllieChatDataModule.ollieMessageRepository
    private val chatWorksRepository = OllieChatDataModule.ollieChatWorksRepository
    private val streamingChatWorkManager = OllieChatDataModule.streamingChatWorkManager
    private val ollieChatManager = OllieChatDataModule.ollieChatManager

    override suspend fun doWork(): Result {
        return withContext(coroutineDispatchers.io) {
            runSuspendCatching {
                val workWithTask =
                    requireNotNull(chatWorksRepository.getWorkByRequestId(id.toString()))
                    { "Work with request id = $id not found" }

                val task = workWithTask.task

                val chatId = workWithTask.work.chatId
                when (task.type) {
                    TypeDbEntity.DO_CHAT -> {
                        val chatId = chatId
                        val userMessageId = requireNotNull(task.userMessageId) {
                            "userMessageId must be not null"
                        }
                        val assistantMessageId = requireNotNull(task.assistantMessageId) {
                            "assistantMessageId must be not null"
                        }
                        val modelId = task.modelId
                        doChat(
                            chatId = chatId,
                            userMessageId = userMessageId,
                            assistantMessageId = assistantMessageId,
                            modelId = modelId
                        )
                    }

                    TypeDbEntity.GENERATE_TITLE -> {
                        generateTitle(chatId = chatId, modelId = task.modelId)
                    }
                }
            }.getOrElse { e ->
                Timber.tag(TAG).d("catch: cause = $e")
                Result.failure()
            }
        }
    }

    private suspend fun doChat(chatId: Long, userMessageId: Long, assistantMessageId: Long, modelId: Long): Result {
        return withContext(coroutineDispatchers.io) {
            Timber.tag(TAG).d("Start with params: chatId = $chatId, userMessageId = $userMessageId, modelId = $modelId")

            var previousPartialResponseBatchReadyTime = System.currentTimeMillis()
            var isFirstBatchGot = false

            val responseText = StringBuilder()
            ollieChatManager.getChatById(chatId)
                .sendMessage(
                    userMessageId = userMessageId,
                    assistantMessageId = assistantMessageId,
                    aiModelId = modelId
                )
                .onEach { reply ->
                    when (reply) {
                        is CompleteResponse -> {
                            messageRepository.updateAssistantMessage(
                                messageId = reply.aiMessageId,
                                text = reply.response,
                                status = AssistantMessageEntity.Status.COMPLETED,
                                tokenUsage = reply.tokenUsage
                            )
                        }

                        is Error -> {
                            Timber.tag(TAG).e("Error: ${reply.cause}")
                            messageRepository.updateAssistantMessageStatus(
                                messageId = reply.aiMessageId,
                                status = AssistantMessageEntity.Status.ERROR
                            )
                        }

                        is PartialResponse -> {
                            Timber.tag(TAG).d("PartialResponse: ${reply.partialResponse}")
                            responseText.append(reply.partialResponse)
                            streamingChatWorkManager.getOrCreateStreamingChatFlow(id.toString())
                                .emit(
                                    OllieChatWorkPartialResponse(
                                        aiMessageId = reply.aiMessageId,
                                        partialResponse = responseText.toString(),
                                    )
                                )
                        }
                    }
                }
                .onCompletion { cause ->
                    Timber.tag(TAG).d("onCompletion: cause = $cause")
                    chatWorksRepository.deleteWork(workRequestId = id.toString())
                    streamingChatWorkManager.removeStreamingChatFlow(workRequestId = id.toString())
                }
                .filterIsInstance<PartialResponse>()
                .chunkPartialResponse()
                .onEach { response ->
                    val newPartialResponseBatchReadyTime = System.currentTimeMillis()
                    val batchReadyDuration =
                        newPartialResponseBatchReadyTime - previousPartialResponseBatchReadyTime
                    previousPartialResponseBatchReadyTime = newPartialResponseBatchReadyTime

                    Timber.tag(TAG)
                        .d("Duration of collecting batch of partial responses = $batchReadyDuration")

                    if (isFirstBatchGot) {
                        messageRepository.updateUserMessageStatus(
                            messageId = userMessageId,
                            status = UserMessageEntity.Status.SENT,
                        )
                        isFirstBatchGot = true
                    }

                    val appendDuration = measureTimeMillis {
                        messageRepository.appendPartialResponseToAssistantMessage(
                            messageId = response.aiMessageId,
                            partialResponse = response.partialResponse,
                        )
                    }

                    Timber.tag(TAG)
                        .d("Duration of Appending partial responses to assistant message = $appendDuration")

                }
                .launchIn(this)
            Result.success()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun Flow<PartialResponse>.chunkPartialResponse(): Flow<PartialResponse> {
        return chunked(CHAT_TOKENS_BATCH_SIZE)
            .map { replies ->
                check(replies.isNotEmpty()) { "replies mustn't be empty" }
                PartialResponse(
                    aiMessageId = replies.first().aiMessageId,
                    partialResponse = buildString { replies.forEach { append(it.partialResponse) } }
                )
            }
    }

    private suspend fun generateTitle(chatId: Long, modelId: Long): Result {
        return runSuspendCatching {
            withContext(coroutineDispatchers.io) {
                ollieChatManager.getChatById(chatId).generateTitle(modelId)
                chatWorksRepository.deleteWork(workRequestId = id.toString())
                Result.success()
            }
        }.getOrElse { throwable ->
            Timber.tag(TAG).e(throwable, "Couldn't generate title")
            Result.failure()
        }
    }
}
