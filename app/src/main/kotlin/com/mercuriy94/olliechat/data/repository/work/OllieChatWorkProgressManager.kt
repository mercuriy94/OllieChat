package com.mercuriy94.olliechat.data.repository.work

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

internal class OllieChatWorkProgressManager {

    private val chatsProgresses =
        ConcurrentHashMap<String, WeakReference<MutableSharedFlow<OllieChatWorkPartialResponse>>>()

    private val chatsProgressesModificationMutex = Mutex()

    suspend fun getOrCreateChatProgressFlow(workRequestId: String): MutableSharedFlow<OllieChatWorkPartialResponse> {
        return chatsProgresses[workRequestId]?.get()
            ?: chatsProgressesModificationMutex.withLock {
                chatsProgresses[workRequestId]?.get()?.let { return@withLock it }
                MutableSharedFlow<OllieChatWorkPartialResponse>(
                    replay = 0,
                    extraBufferCapacity = 16
                ).also {
                    chatsProgresses[workRequestId] = WeakReference(it)
                }
            }
    }
}

data class OllieChatWorkPartialResponse(
    val aiMessageId: Long,
    val partialResponse: String,
)
