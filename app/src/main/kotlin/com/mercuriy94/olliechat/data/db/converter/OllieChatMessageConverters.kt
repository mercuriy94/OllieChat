package com.mercuriy94.olliechat.data.db.converter

import androidx.room.TypeConverter
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageDbEntity.AuthorDb
import com.mercuriy94.olliechat.data.db.model.OllieChatMessageDbEntity.StatusDb

internal object OllieChatMessageAuthorDbEntityConverter {

    private const val USER = "user"
    private const val ASSISTANT = "assistant"
//        const val SYSTEM = "system"

    @TypeConverter
    @JvmStatic
    fun toAuthorDb(dbValue: String): AuthorDb =
        when (dbValue) {
            USER -> AuthorDb.USER
            ASSISTANT -> AuthorDb.ASSISTANT
//            SYSTEM -> AuthorDb.SYSTEM
            else -> throw IllegalArgumentException("Unknown value: $dbValue")
        }

    @TypeConverter
    @JvmStatic
    fun fromAuthorDb(dbEntity: AuthorDb): String = when (dbEntity) {
        AuthorDb.USER -> USER
        AuthorDb.ASSISTANT -> ASSISTANT
//        AuthorDb.SYSTEM -> SYSTEM
    }

}

internal class OllieChatMessageStatusDbEntityConverter {
    private companion object {
        const val PENDING = "PENDING"
        const val SENDING = "SENDING"
        const val SENT = "SENT"
        const val PROCESSING = "PROCESSING"
        const val PARTIAL = "PARTIAL"
        const val COMPLETED = "COMPLETED"
        const val ERROR = "ERROR"
    }

    @TypeConverter
    fun toStatusDb(dbValue: String): StatusDb =
        when (dbValue) {
            PENDING -> StatusDb.PENDING
            SENDING -> StatusDb.SENDING
            SENT -> StatusDb.SENT
            PROCESSING -> StatusDb.PROCESSING
            PARTIAL -> StatusDb.PARTIAL
            COMPLETED -> StatusDb.COMPLETED
            ERROR -> StatusDb.ERROR
            else -> throw IllegalArgumentException("Unknown value: $dbValue")
        }

    @TypeConverter
    fun fromStatusDb(dbEntity: StatusDb): String = when (dbEntity) {
        StatusDb.PENDING -> PENDING
        StatusDb.SENDING -> SENDING
        StatusDb.SENT -> SENT
        StatusDb.PROCESSING -> PROCESSING
        StatusDb.PARTIAL -> PARTIAL
        StatusDb.COMPLETED -> COMPLETED
        StatusDb.ERROR -> ERROR
    }
}
