package com.mercuriy94.olliechat.data.repository.chat.manager

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class OllieChatAssistantHolder(
    override val aiModelId: Long,
    private val assistantProvider: suspend (Long) -> OllieChatAssistant,
) : OllieChatAssistantSource {

    @Volatile
    private var currentAssistant: OllieChatAssistant? = null
    private val mutex = Mutex()

    override suspend fun getAssistant(): OllieChatAssistant {
        currentAssistant?.let { return it }
        return mutex.withLock {
            currentAssistant?.let { return it }
            assistantProvider(aiModelId).also { currentAssistant = it }
        }
    }

    override suspend fun invalidate(updateBlock: suspend () -> Unit) {
        mutex.withLock {
            updateBlock()
            currentAssistant = null
        }
    }
}
