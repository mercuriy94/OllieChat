package com.mercuriy94.olliechat.data.db.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "ollie_models")
internal data class OllieModelDbEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "size")
    val size: Long,

    @ColumnInfo(name = "digest")
    val digest: String,

    )

@Entity(
    tableName = "ollie_model_details",
    indices = [Index("model_id")],
    foreignKeys = [
        ForeignKey(
            entity = OllieModelDbEntity::class,
            parentColumns = ["id"],
            childColumns = ["model_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
)
internal data class OllieDetailsModelDbEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "parameter_size")
    val parameterSize: String,

    @ColumnInfo(name = "quantization_level")
    val quantizationLevel: String,

    @ColumnInfo(name = "model_id")
    val modelId: Long = 0,
)

@Entity(
    tableName = "model_params",
    primaryKeys = ["key", "model_id"],
    indices = [Index("model_id")],
    foreignKeys = [
        ForeignKey(
            entity = OllieModelDbEntity::class,
            parentColumns = ["id"],
            childColumns = ["model_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
)
internal data class OllieModelParamDbEntity(

    @ColumnInfo(name = "key")
    val key: KeyDbEntity,

    @ColumnInfo(name = "value")
    val value: String,

    @ColumnInfo(name = "value_type")
    val valueType: ValueTypeDbEntity,

    @ColumnInfo(name = "model_id")
    val modelId: Long = 0,
) {

    enum class KeyDbEntity {
        TOP_K,
        TOP_P,
        MIN_P,
        TEMPERATURE,
        NUM_CTX,
        REPEAT_PENALTY,
    }

    enum class ValueTypeDbEntity {
        INT,
        FLOAT,
        STRING,
    }
}

internal data class OllieModelDbEntityWithRelationships(

    @Embedded val model: OllieModelDbEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "model_id"
    )
    val details: OllieDetailsModelDbEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "model_id"
    )
    val params: List<OllieModelParamDbEntity>,
)
