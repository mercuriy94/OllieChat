package com.mercuriy94.olliechat.data.db.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "ollie_chat_works",
    indices = [Index("chat_id")],
    foreignKeys = [
        ForeignKey(
            entity = OllieChatDbEntity::class,
            parentColumns = ["id"],
            childColumns = ["chat_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
)
internal data class OllieChatWorkDbEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "chat_id")
    val chatId: Long,

    @ColumnInfo(name = "work_request_id")
    val workRequestId: String,

    @ColumnInfo(name = "work_request_tag")
    val workRequestTag: String,
)

@Entity(
    tableName = "ollie_chat_work_tasks",
    indices = [Index("work_id")],
    foreignKeys = [
        ForeignKey(
            entity = OllieChatWorkDbEntity::class,
            parentColumns = ["id"],
            childColumns = ["work_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
)
internal data class OllieChatWorkTaskDbEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "work_id")
    val workId: Long = 0,

    @ColumnInfo(name = "type")
    val type: TypeDbEntity,

    @ColumnInfo(name = "model_id")
    val modelId: Long,

    @ColumnInfo(name = "user_message_id")
    val userMessageId: Long? = null,

    @ColumnInfo(name = "assistant_message_id")
    val assistantMessageId: Long? = null,
) {

    enum class TypeDbEntity {
        DO_CHAT,
        GENERATE_TITLE,
    }

}

internal data class OllieChatWorkDbEntityWithTask(

    @Embedded val work: OllieChatWorkDbEntity,

    @Relation(parentColumn = "id", entityColumn = "work_id")
    val task: OllieChatWorkTaskDbEntity,
)
