package com.mercuriy94.olliechat.presentation.feature.chats.model

import com.mercuriy94.olliechat.presentation.feature.chat.model.ChatOllieMessageUi

internal data class ChatUiModel(
    val id: Long,
    val title: String,
    val latestMessage: ChatOllieMessageUi
)
