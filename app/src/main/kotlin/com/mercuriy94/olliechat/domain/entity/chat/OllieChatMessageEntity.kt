package com.mercuriy94.olliechat.domain.entity.chat

import java.time.OffsetDateTime

internal sealed interface OllieChatMessageEntity {
    val id: Long
    val createdAt: OffsetDateTime

    data class UserMessageEntity(
        override val id: Long,
        override val createdAt: OffsetDateTime,
        val status: Status,
        val text: String,
    ) : OllieChatMessageEntity {

        enum class Status {
            CREATED,
            SENDING,
            SENT,
            ERROR,
        }
    }

    data class AssistantMessageEntity(
        override val id: Long,
        override val createdAt: OffsetDateTime,
        val userMessageId: Long,
        val status: Status,
        val text: String,
        val aiModel: AiModel,
        val tokenUsage: OllieChatMessageTokenUsage?,
    ) : OllieChatMessageEntity {

        enum class Status {

            PENDING,
            PARTIAL,
            COMPLETED,
            ERROR,
        }

        data class AiModel(
            val id: Long = 0,
            val aiModelId: Long,
            val name: String,
        )
    }
}

internal sealed interface OllieChatMessageStreamingReply {

    data class PartialResponse(
        val aiMessageId: Long,
        val partialResponse: String,
    ) : OllieChatMessageStreamingReply

    data class CompleteResponse(
        val aiMessageId: Long,
        val response: String,
        val tokenUsage: OllieChatMessageTokenUsage?,
    ) : OllieChatMessageStreamingReply

    data class Error(
        val aiMessageId: Long,
        val cause: Throwable,
    ) : OllieChatMessageStreamingReply
}

data class OllieChatMessageTokenUsage(
    val id: Long = 0L,
    val inputTokenCount: Int?,
    val outputTokenCount: Int?,
    val totalTokenCount: Int?,
)
