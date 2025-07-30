package com.mercuriy94.olliechat.data.repository.chat.manager

internal interface OllieChatAssistantSource {

    val aiModelId: Long

    suspend fun getAssistant(): OllieChatAssistant
    suspend fun invalidate(updateBlock: suspend () -> Unit = {})
}
