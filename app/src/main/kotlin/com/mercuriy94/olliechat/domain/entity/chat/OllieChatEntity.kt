package com.mercuriy94.olliechat.domain.entity.chat

internal data class OllieChatEntity(
    val id: Long,
    val title: String,
)

internal data class OllieChatWithLatestMessage(
    val chat: OllieChatEntity,
    val latestMessage: OllieChatMessageEntity,
)
