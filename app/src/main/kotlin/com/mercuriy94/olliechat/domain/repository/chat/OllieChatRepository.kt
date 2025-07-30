package com.mercuriy94.olliechat.domain.repository.chat

import com.mercuriy94.olliechat.domain.entity.chat.OllieChatEntity
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatWithLatestMessage
import kotlinx.coroutines.flow.Flow

internal interface OllieChatRepository {

    suspend fun createNewChat(): Long

    suspend fun getChatById(id: Long): OllieChatEntity

    fun observeChats(): Flow<List<OllieChatWithLatestMessage>>

    fun observeChatTitle(chatId: Long): Flow<String>

    suspend fun updateChatTitle(chatId: Long, title: String)

}
