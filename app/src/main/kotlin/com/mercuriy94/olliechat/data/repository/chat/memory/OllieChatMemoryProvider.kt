package com.mercuriy94.olliechat.data.repository.chat.memory

import dev.langchain4j.memory.ChatMemory
import dev.langchain4j.memory.chat.ChatMemoryProvider
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.store.memory.chat.ChatMemoryStore

internal class OllieChatMemoryProvider(
    private val chatMemoryStore: ChatMemoryStore,
) : ChatMemoryProvider {

    override fun get(memoryId: Any?): ChatMemory? {
        return MessageWindowChatMemory.builder()
            .id(memoryId)
            .chatMemoryStore(chatMemoryStore)
            .maxMessages(Int.MAX_VALUE)
            .build()
    }
}
