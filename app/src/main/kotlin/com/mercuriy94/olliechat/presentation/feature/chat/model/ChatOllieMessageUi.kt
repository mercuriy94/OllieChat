package com.mercuriy94.olliechat.presentation.feature.chat.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

internal const val ASSISTANT_MESSAGE_ACTION_COPY_ID = "assistant_message_action_copy"
internal const val ASSISTANT_MESSAGE_ACTION_SHARE_ID = "assistant_message_action_share"
internal const val ASSISTANT_MESSAGE_ACTION_REPEAT_ID = "assistant_message_action_repeat"
internal const val ASSISTANT_MESSAGE_ACTION_INFO_ID = "assistant_message_action_info"
internal const val USER_MESSAGE_ACTION_COPY_ID = "user_message_action_copy"
internal const val USER_MESSAGE_ACTION_EDIT_ID = "user_message_action_edit"

@Immutable
internal sealed interface ChatOllieMessageUi {

    val id: Long
    val text: String?

    @Immutable
    data class UserMessage(
        override val id: Long,
        override val text: String,
        val actions: List<Action> = emptyList(),
    ) : ChatOllieMessageUi

    @Immutable
    data class AssistantMessage(
        override val id: Long,
        override val text: String?,
        val think: String?,
        val actions: List<Action> = emptyList(),
        val tokenUsage: TokenUsage? = null,
        val aiModel: AiModel,
    ) : ChatOllieMessageUi {

        data class TokenUsage(
            val inputTokenCount: Int?,
            val outputTokenCount: Int?,
            val totalTokenCount: Int?,
        )

        data class AiModel(
            val aiModelId: Long,
            val name: String,
        )
    }

    data class Action(
        val id: String,
        val text: String,
        val icon: ImageVector,
        val contentDescription: String,
    )
}
