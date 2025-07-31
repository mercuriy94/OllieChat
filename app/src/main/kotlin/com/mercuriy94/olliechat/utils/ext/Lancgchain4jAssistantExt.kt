package com.mercuriy94.olliechat.utils.ext

import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.service.AiServices

inline fun <reified T> createAiService(streamingChatModel: StreamingChatModel): T {
    return AiServices.create(T::class.java, streamingChatModel)
}

inline fun <reified T> buildAiServices(block: AiServices<T>.() -> Unit): T {
    val builder = AiServices.builder(T::class.java)
    builder.block()
    return builder.build()
}
