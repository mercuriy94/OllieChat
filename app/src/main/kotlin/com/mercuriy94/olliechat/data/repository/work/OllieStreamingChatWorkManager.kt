package com.mercuriy94.olliechat.data.repository.work

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

internal class OllieStreamingChatWorkManager {

    private val streamingChats =
        ConcurrentHashMap<String, WeakReference<MutableSharedFlow<OllieChatWorkPartialResponse>>>()

    private val mutex = Mutex()

    suspend fun getOrCreateStreamingChatFlow(workRequestId: String): MutableSharedFlow<OllieChatWorkPartialResponse> {
        return streamingChats[workRequestId]?.get()
            ?: mutex.withLock {
                streamingChats[workRequestId]?.get()?.let { return@withLock it }
                MutableSharedFlow<OllieChatWorkPartialResponse>(
                    replay = 1,
                    extraBufferCapacity = 16
                ).also {
                    streamingChats[workRequestId] = WeakReference(it)
                }
            }
    }

    suspend fun removeStreamingChatFlow(workRequestId: String) {
        mutex.withLock {
            streamingChats.remove(workRequestId)
        }
    }
}

internal data class OllieChatWorkPartialResponse(
    val aiMessageId: Long,
    val partialResponse: String,
)
