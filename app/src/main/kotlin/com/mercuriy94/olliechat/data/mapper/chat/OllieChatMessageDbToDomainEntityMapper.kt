package com.mercuriy94.olliechat.data.mapper.chat

import com.mercuriy94.olliechat.data.db.model.OllieChatMessageDbEntity.AuthorDb
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageDbEntity.StatusDb
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageDbEntityWithRelationships
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity.AssistantMessageEntity
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity.UserMessageEntity
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageTokenUsage

internal class OllieChatMessageDbToDomainEntityMapper {

    operator fun invoke(
        dbMessages: List<OllieChatMessageDbEntityWithRelationships>,
    ): List<OllieChatMessageEntity> {
        return dbMessages.map { invoke(it) }
    }

    operator fun invoke(
        dbMessage: OllieChatMessageDbEntityWithRelationships,
    ): OllieChatMessageEntity {
        return when (dbMessage.message.author) {
            AuthorDb.USER -> dbMessage.toUserMessageDomainEntity()
            AuthorDb.ASSISTANT -> dbMessage.toAssistantMessageDomainEntity()
        }
    }

    private fun OllieChatMessageDbEntityWithRelationships.toUserMessageDomainEntity(): UserMessageEntity {
        return UserMessageEntity(
            id = message.id,
            text = message.text,
            createdAt = message.createdAt,
            status = when (message.status) {
                StatusDb.PENDING -> UserMessageEntity.Status.PENDING
                StatusDb.ERROR -> UserMessageEntity.Status.ERROR
                StatusDb.SENDING -> UserMessageEntity.Status.SENDING
                StatusDb.SENT -> UserMessageEntity.Status.SENT
                else -> throw IllegalArgumentException(
                    "Unsupported user message status: ${message.status}"
                )
            }
        )
    }

    private fun OllieChatMessageDbEntityWithRelationships.toAssistantMessageDomainEntity(): AssistantMessageEntity {
        return AssistantMessageEntity(
            id = message.id,
            text = message.text,
            createdAt = message.createdAt,
            status = when (message.status) {
                StatusDb.PROCESSING -> AssistantMessageEntity.Status.PROCESSING
                StatusDb.PARTIAL -> AssistantMessageEntity.Status.PARTIAL
                StatusDb.COMPLETED -> AssistantMessageEntity.Status.COMPLETED
                StatusDb.PENDING -> AssistantMessageEntity.Status.PENDING
                StatusDb.ERROR -> AssistantMessageEntity.Status.ERROR
                else -> throw IllegalArgumentException(
                    "Unsupported assistant message status: ${message.status}"
                )
            },
            userMessageId = requireNotNull(message.userMessageId) {
                "userMessageId must be set for assistant messages"
            },
            aiModel = requireNotNull(aiModel) {
                "aiModel must be set for assistant messages"
            }.let { aiModelDbEntity ->
                AssistantMessageEntity.AiModel(
                    id = aiModelDbEntity.id,
                    aiModelId = aiModelDbEntity.aiModelId,
                    name = aiModelDbEntity.name,
                )
            },
            tokenUsage = tokenUsage?.let { tokenUsageDbEntity ->
                OllieChatMessageTokenUsage(
                    id = tokenUsageDbEntity.id,
                    inputTokenCount = tokenUsageDbEntity.inputTokenCount,
                    outputTokenCount = tokenUsageDbEntity.outputTokenCount,
                    totalTokenCount = tokenUsageDbEntity.totalTokenCount,
                )
            }
        )
    }
}
