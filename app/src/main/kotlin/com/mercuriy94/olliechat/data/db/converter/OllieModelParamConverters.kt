package com.mercuriy94.olliechat.data.db.converter

import androidx.room.TypeConverter
import com.mercuriy94.olliechat.data.db.model.OllieModelParamDbEntity.KeyDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieModelParamDbEntity.ValueTypeDbEntity

internal object OllieModelParamKeyDbEntityConverter {

    private const val TOP_K = "top_k"
    private const val TOP_P = "top_p"
    private const val MIN_P = "min_p"
    private const val TEMPERATURE = "temperature"
    private const val NUM_CTX = "num_ctx"
    private const val REPEAT_PENALTY = "repeat_penalty"

    @TypeConverter
    @JvmStatic
    fun toKeyDbEntity(dbValue: String): KeyDbEntity =
        when (dbValue) {
            TOP_K -> KeyDbEntity.TOP_K
            TOP_P -> KeyDbEntity.TOP_P
            MIN_P -> KeyDbEntity.MIN_P
            TEMPERATURE -> KeyDbEntity.TEMPERATURE
            NUM_CTX -> KeyDbEntity.NUM_CTX
            REPEAT_PENALTY -> KeyDbEntity.REPEAT_PENALTY
            else -> throw IllegalArgumentException("Unknown key: $dbValue")
        }

    @TypeConverter
    @JvmStatic
    fun fromKeyDbEntity(dbEntity: KeyDbEntity): String = when (dbEntity) {
        KeyDbEntity.TOP_K -> TOP_K
        KeyDbEntity.TOP_P -> TOP_P
        KeyDbEntity.MIN_P -> MIN_P
        KeyDbEntity.TEMPERATURE -> TEMPERATURE
        KeyDbEntity.NUM_CTX -> NUM_CTX
        KeyDbEntity.REPEAT_PENALTY -> REPEAT_PENALTY
    }
}

internal class OllieModelParamValueDbEntityConverter {

    private companion object {
        const val INT = "int"
        const val FLOAT = "float"
        const val STRING = "string"
    }

    @TypeConverter
    fun toValueTypeDbEntity(dbValue: String): ValueTypeDbEntity =
        when (dbValue) {
            INT -> ValueTypeDbEntity.INT
            FLOAT -> ValueTypeDbEntity.FLOAT
            STRING -> ValueTypeDbEntity.STRING
            else -> throw IllegalArgumentException("Unknown key: $dbValue")
        }

    @TypeConverter
    fun fromValueTypeDbEntity(dbEntity: ValueTypeDbEntity): String = when (dbEntity) {
        ValueTypeDbEntity.INT -> INT
        ValueTypeDbEntity.FLOAT -> FLOAT
        ValueTypeDbEntity.STRING -> STRING
    }
}
