package com.mercuriy94.olliechat.presentation.feature.chat.model.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.ContentCopy
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity.AssistantMessageEntity
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity.UserMessageEntity
import com.mercuriy94.olliechat.presentation.feature.chat.model.ASSISTANT_MESSAGE_ACTION_COPY_ID
import com.mercuriy94.olliechat.presentation.feature.chat.model.ASSISTANT_MESSAGE_ACTION_INFO_ID
import com.mercuriy94.olliechat.presentation.feature.chat.model.ChatOllieMessageUi
import com.mercuriy94.olliechat.presentation.feature.chat.model.ChatOllieMessageUi.AssistantMessage
import com.mercuriy94.olliechat.presentation.feature.chat.model.ChatOllieMessageUi.UserMessage
import com.mercuriy94.olliechat.presentation.feature.chat.model.USER_MESSAGE_ACTION_COPY_ID

internal class OllieChatMessageDomainToChatUiEntityMapper {

    private companion object {

        private const val START_THINKING_TAG = "<think>"
        private const val END_THINKING_TAG = "</think>"
    }

    operator fun invoke(messages: List<OllieChatMessageEntity>): List<ChatOllieMessageUi> {
        return messages.map(::invoke)
    }

    operator fun invoke(message: OllieChatMessageEntity): ChatOllieMessageUi {
        return when (message) {
            is AssistantMessageEntity -> {
                mapAssistantMessageDomainToUiEntity(message = message)
            }

            is UserMessageEntity -> {
                UserMessage(
                    id = message.id,
                    text = message.text,
                    actions = createUserMessageActions()
                )
            }
        }
    }

    private fun mapAssistantMessageDomainToUiEntity(
        message: AssistantMessageEntity,
    ): AssistantMessage {
        val (think, response) = parseMessageText(message.text)
        return AssistantMessage(
            id = message.id,
            text = response,
            think = think,
            aiModel = AssistantMessage.AiModel(
                aiModelId = message.aiModel.aiModelId,
                name = message.aiModel.name
            ),
            actions = createAssistantMessageActions(message = message),
            tokenUsage = message.tokenUsage?.let { tokenUsage ->
                AssistantMessage.TokenUsage(
                    inputTokenCount = tokenUsage.inputTokenCount,
                    outputTokenCount = tokenUsage.outputTokenCount,
                    totalTokenCount = tokenUsage.totalTokenCount,
                )
            }
        )

    }

    fun parseMessageText(messageText: String): Pair<String?, String?> {
        val thinkingIndexes = findThinkingContent(messageText)

        val (think, response) = if (thinkingIndexes != null) {
            val (startThinkingIndex, endThinkingIndex) = thinkingIndexes

            if (endThinkingIndex != null) {
                messageText
                    .substring(startThinkingIndex, endThinkingIndex + END_THINKING_TAG.length) to
                        messageText.substring(endThinkingIndex + END_THINKING_TAG.length)
            } else {
                messageText.substring(START_THINKING_TAG.length) to null
            }
        } else {
            null to messageText
        }


        return think?.replace("\\n", "\n") to response?.replace("\\n", "\n")
    }


    fun findThinkingContent(text: String): Pair<Int, Int?>? {
        if (!text.startsWith(START_THINKING_TAG)) {
            return null
        }
        val firstOpenIndex = 0

        val openLen = START_THINKING_TAG.length
        val closeLen = END_THINKING_TAG.length

        val startIndex = firstOpenIndex

        var balance = 1
        var currentIndex = startIndex + openLen

        while (currentIndex < text.length) {
            if (text.startsWith(START_THINKING_TAG, currentIndex)) {
                balance++
                currentIndex += openLen
            } else if (text.startsWith(END_THINKING_TAG, currentIndex)) {
                balance--
                if (balance == 0) {
                    return Pair(startIndex + openLen, currentIndex)
                }
                currentIndex += closeLen
            } else {
                currentIndex++
            }
        }
        return Pair(startIndex + openLen, null)
    }

    private fun createAssistantMessageActions(
        message: AssistantMessageEntity,
    ): List<ChatOllieMessageUi.Action> {
        return if (message.status == AssistantMessageEntity.Status.COMPLETED) {
            listOfNotNull(
                message.tokenUsage?.let {
                    ChatOllieMessageUi.Action(
                        id = ASSISTANT_MESSAGE_ACTION_INFO_ID,
                        text = "Info",
                        icon = Icons.Outlined.Info,
                        contentDescription = "Token usage information"
                    )
                },
                ChatOllieMessageUi.Action(
                    id = ASSISTANT_MESSAGE_ACTION_COPY_ID,
                    text = "Copy",
                    icon = Icons.Rounded.ContentCopy,
                    contentDescription = "Copy message"
                ),

//                ChatOllieMessageUi.Action(
//                    id = ASSISTANT_MESSAGE_ACTION_SHARE_ID,
//                    text = "Share",
//                    icon = Icons.Rounded.Share,
//                    contentDescription = "Share message"
//                ),
//
//                ChatOllieMessageUi.Action(
//                    id = ASSISTANT_MESSAGE_ACTION_REPEAT_ID,
//                    text = "Repeat",
//                    icon = Icons.Rounded.Repeat,
//                    contentDescription = "Repeat"
//                ),
            )
        } else emptyList()
    }

    fun createUserMessageActions(): List<ChatOllieMessageUi.Action> {
        return listOf(
            ChatOllieMessageUi.Action(
                id = USER_MESSAGE_ACTION_COPY_ID,
                text = "Copy",
                icon = Icons.Rounded.ContentCopy,
                contentDescription = "Copy message"
            ),
//            ChatOllieMessageUi.Action(
//                id = USER_MESSAGE_ACTION_COPY_ID,
//                text = "Edit",
//                icon = Icons.Rounded.Edit,
//                contentDescription = "Edit message"
//            ),
        )
    }
}
