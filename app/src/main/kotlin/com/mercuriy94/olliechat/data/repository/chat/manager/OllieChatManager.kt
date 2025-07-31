package com.mercuriy94.olliechat.data.repository.chat.manager

import com.mercuriy94.olliechat.data.repository.chat.OllieChat
import com.mercuriy94.olliechat.data.repository.chat.OllieChatImpl
import com.mercuriy94.olliechat.data.repository.chat.title.TitleGenerator
import com.mercuriy94.olliechat.domain.repository.chat.OllieChatRepository
import com.mercuriy94.olliechat.domain.repository.chat.OllieMessageRepository

internal interface OllieChatManager {

    suspend fun getChatById(id: Long): OllieChat
}

internal class OllieChatManagerImpl(
    private val ollieChatAssistantManager: OllieChatAssistantManager,
    private val persistentOllieChatRepository: OllieChatRepository,
    private val messageRepository: OllieMessageRepository,
    private val olleChatTitleGenerator: TitleGenerator,
) : OllieChatManager {

    override suspend fun getChatById(id: Long): OllieChat {
        return OllieChatImpl(
            chatId = persistentOllieChatRepository.getChatById(id).id,
            ollieChatAssistantManager = ollieChatAssistantManager,
            chatRepository = persistentOllieChatRepository,
            titleGenerator = olleChatTitleGenerator,
            messageRepository = messageRepository,
        )
    }
}
