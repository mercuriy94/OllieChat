package com.mercuriy94.olliechat.data.langchain4j.chat

import com.mercuriy94.olliechat.data.db.model.OllieChatMessageDbEntityWithRelationships
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.UserMessage

internal data class OllieChatUserMessage(
    val dbMessage: OllieChatMessageDbEntityWithRelationships,
) : UserMessage(dbMessage.message.text)

internal data class OllieChatAiMessage(
    val dbMessage: OllieChatMessageDbEntityWithRelationships,
) : AiMessage(dbMessage.message.text)
