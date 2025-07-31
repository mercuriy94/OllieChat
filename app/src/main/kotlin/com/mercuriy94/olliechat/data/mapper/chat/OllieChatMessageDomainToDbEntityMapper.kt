package com.mercuriy94.olliechat.data.mapper.chat

import com.mercuriy94.olliechat.data.db.model.OllieChatMessageAiModelDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageDbEntity.AuthorDb
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageDbEntity.StatusDb
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageDbEntityWithRelationships
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageTokenUsageDbEntity
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity.AssistantMessageEntity
import com.mercuriy94.olliechat.domain.entity.chat.OllieChatMessageEntity.UserMessageEntity

internal class OllieChatMessageDomainToDbEntityMapper {

    fun invoke(
        chatId: Long,
        domainMessage: OllieChatMessageEntity,
    ): OllieChatMessageDbEntityWithRelationships {
        return when (domainMessage) {
            is AssistantMessageEntity ->
                OllieChatMessageDbEntityWithRelationships(
                    message = OllieChatMessageDbEntity(
                        id = domainMessage.id,
                        chatId = chatId,
                        text = domainMessage.text,
                        createdAt = domainMessage.createdAt,
                        author = AuthorDb.ASSISTANT,
                        status = mapDomainStatusToDbStatus(domainMessage.status),
                        userMessageId = domainMessage.userMessageId,
                    ),
                    tokenUsage = domainMessage.tokenUsage?.let { tokenUsage ->
                        OllieChatMessageTokenUsageDbEntity(
                            id = tokenUsage.id,
                            messageId = domainMessage.id,
                            inputTokenCount = tokenUsage.inputTokenCount,
                            outputTokenCount = tokenUsage.outputTokenCount,
                            totalTokenCount = tokenUsage.totalTokenCount,
                        )
                    },
                    aiModel = OllieChatMessageAiModelDbEntity(
                        id = domainMessage.aiModel.id,
                        messageId = domainMessage.id,
                        aiModelId = domainMessage.aiModel.aiModelId,
                        name = domainMessage.aiModel.name
                    )
                )

            is UserMessageEntity -> OllieChatMessageDbEntityWithRelationships(
                OllieChatMessageDbEntity(
                    id = domainMessage.id,
                    chatId = chatId,
                    text = domainMessage.text,
                    createdAt = domainMessage.createdAt,
                    author = AuthorDb.USER,
                    status = mapDomainStatusToDbStatus(domainMessage.status)
                ),
                tokenUsage = null
            )
        }
    }

    fun mapDomainStatusToDbStatus(status: AssistantMessageEntity.Status): StatusDb {
        return when (status) {
            AssistantMessageEntity.Status.PENDING -> StatusDb.PENDING
            AssistantMessageEntity.Status.PARTIAL -> StatusDb.PARTIAL
            AssistantMessageEntity.Status.COMPLETED -> StatusDb.COMPLETED
            AssistantMessageEntity.Status.ERROR -> StatusDb.ERROR
        }
    }

    fun mapDomainStatusToDbStatus(status: UserMessageEntity.Status): StatusDb {
        return when (status) {
            UserMessageEntity.Status.CREATED -> StatusDb.CREATED
            UserMessageEntity.Status.SENDING -> StatusDb.SENDING
            UserMessageEntity.Status.SENT -> StatusDb.SENT
            UserMessageEntity.Status.ERROR -> StatusDb.ERROR
        }
    }
}
