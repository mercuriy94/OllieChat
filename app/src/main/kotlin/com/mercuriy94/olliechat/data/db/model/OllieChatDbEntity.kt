package com.mercuriy94.olliechat.data.db.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.OffsetDateTime

@Entity(tableName = "ollie_chats")
internal class OllieChatDbEntity(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,
)

@Entity(
    tableName = "ollie_messages",
    indices = [
        Index("chat_id"),
        Index("created_at"),
        Index("status"),
        Index("author"),
        Index(
            value = ["chat_id", "status", "created_at", "author", "id"],
            name = "index_messages_chat_order"
        )

    ],
    foreignKeys = [
        ForeignKey(
            entity = OllieChatDbEntity::class,
            parentColumns = ["id"],
            childColumns = ["chat_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
)
internal class OllieChatMessageDbEntity(

    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "chat_id")
    val chatId: Long = 0,

    @ColumnInfo(name = "text")
    val text: String,

    @ColumnInfo(name = "author")
    val author: AuthorDb,

    @ColumnInfo(name = "created_at")
    val createdAt: OffsetDateTime,

    @ColumnInfo(name = "status")
    val status: StatusDb,

    @ColumnInfo(name = "user_massage_id")
    val userMessageId: Long? = null,

    ) {

    enum class AuthorDb {
        USER,
        ASSISTANT,
    }

    enum class StatusDb {

        // User
        SENDING,
        SENT,

        // Assistant
        PROCESSING,
        PARTIAL,
        COMPLETED,

        // Common
        PENDING,
        ERROR,
    }
}

@Entity(
    tableName = "ollie_messages_ai_models",
    indices = [Index("message_id")],
    foreignKeys = [
        ForeignKey(
            entity = OllieChatMessageDbEntity::class,
            parentColumns = ["id"],
            childColumns = ["message_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
)
data class OllieChatMessageAiModelDbEntity(

    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "message_id")
    val messageId: Long = 0,

    @ColumnInfo(name = "ai_model_id")
    val aiModelId: Long,

    @ColumnInfo(name = "name")
    val name: String,
)

@Entity(
    tableName = "token_usage",
    indices = [Index("message_id")],
    foreignKeys = [
        ForeignKey(
            entity = OllieChatMessageDbEntity::class,
            parentColumns = ["id"],
            childColumns = ["message_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
)
data class OllieChatMessageTokenUsageDbEntity(

    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    @ColumnInfo(name = "message_id")
    val messageId: Long,

    @ColumnInfo(name = "input_token_count")
    val inputTokenCount: Int? = null,

    @ColumnInfo(name = "output_token_count")
    val outputTokenCount: Int? = null,

    @ColumnInfo(name = "total_token_count")
    val totalTokenCount: Int? = null,
)

internal data class OllieChatMessageDbEntityWithRelationships(

    @Embedded val message: OllieChatMessageDbEntity,

    @Relation(parentColumn = "id", entityColumn = "message_id")
    val tokenUsage: OllieChatMessageTokenUsageDbEntity? = null,

    @Relation(parentColumn = "id", entityColumn = "message_id")
    val aiModel: OllieChatMessageAiModelDbEntity? = null,
)

internal data class OllieChatDbEntityWithLatestMessage(
    @Embedded val chat: OllieChatDbEntity,
    @Embedded(prefix = "latest_message_") val latestMessage: OllieChatMessageDbEntity,
    @Embedded(prefix = "message_ai_model_") val messageAiModel: OllieChatMessageAiModelDbEntity? = null,
)
