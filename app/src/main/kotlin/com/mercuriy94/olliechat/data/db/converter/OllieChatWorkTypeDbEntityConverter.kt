package com.mercuriy94.olliechat.data.db.converter

import androidx.room.TypeConverter
import com.mercuriy94.olliechat.data.db.model.OllieChatWorkTaskDbEntity.TypeDbEntity

internal object OllieChatWorkTypeDbEntityConverter {

    private const val DO_CHAT = "DO_CHAT"
    private const val GENERATE_TITLE = "GENERATE_TITLE"

    @TypeConverter
    @JvmStatic
    fun toTypeDbEntity(dbValue: String): TypeDbEntity =
        when (dbValue) {
            DO_CHAT -> TypeDbEntity.DO_CHAT
            GENERATE_TITLE -> TypeDbEntity.GENERATE_TITLE
            else -> throw IllegalArgumentException("Unknown value: $dbValue")
        }

    @TypeConverter
    @JvmStatic
    fun fromTypeDbEntity(dbEntity: TypeDbEntity): String = when (dbEntity) {
        TypeDbEntity.DO_CHAT -> DO_CHAT
        TypeDbEntity.GENERATE_TITLE -> GENERATE_TITLE
    }
}
