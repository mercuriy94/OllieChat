package com.mercuriy94.olliechat.data.repository.chat.manager

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal fun interface OllieChatAssistantSourceProvider : suspend (Long) -> OllieChatAssistantSource

internal class OllieChatAssistantSourceProviderImpl(
    private val ollieChatAssistantManager: OllieChatAssistantManager,
) : OllieChatAssistantSourceProvider {

    @Volatile
    private var cachedAssistantSource: OllieChatAssistantSource? = null
    private val mutex = Mutex()

    override suspend fun invoke(aiModelId: Long): OllieChatAssistantSource {
        return cachedAssistantSource?.let { source ->
            source.takeIf { source.aiModelId == aiModelId }
        } ?: mutex.withLock {
            cachedAssistantSource?.takeIf { source -> source.aiModelId == aiModelId }
                ?: run {
                    cachedAssistantSource?.invalidate()
                    ollieChatAssistantManager.getAssistantSource(aiModelId)
                        .also { cachedAssistantSource = it }
                }
        }
    }
}
