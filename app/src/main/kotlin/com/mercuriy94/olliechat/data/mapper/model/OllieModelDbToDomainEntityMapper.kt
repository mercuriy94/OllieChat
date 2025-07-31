package com.mercuriy94.olliechat.data.mapper.model

import com.mercuriy94.olliechat.data.db.model.OllieModelDbEntityWithRelationships
import com.mercuriy94.olliechat.data.db.model.OllieModelParamDbEntity.KeyDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieModelParamDbEntity.ValueTypeDbEntity
import com.mercuriy94.olliechat.domain.entity.model.OllieModel
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Details
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param.Value.FloatValue
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param.Value.IntValue
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param.Value.StringValue

internal class OllieModelDbToDomainEntityMapper {

    operator fun invoke(dbModels: List<OllieModelDbEntityWithRelationships>): List<OllieModel> =
        dbModels.map(::invoke)

    operator fun invoke(dbModel: OllieModelDbEntityWithRelationships): OllieModel {
        val dbModelEntity = dbModel.model
        val dbDetailsEntity = dbModel.details
        val dbParamEntities = dbModel.params
        return OllieModel(
            id = dbModelEntity.id,
            name = dbModelEntity.name,
            size = dbModelEntity.size,
            digest = dbModelEntity.digest,
            details = Details(
                id = dbDetailsEntity.id,
                parameterSize = dbDetailsEntity.parameterSize,
                quantizationLevel = dbDetailsEntity.quantizationLevel,
            ),
            params = dbParamEntities.map { dbParamEntity ->
                Param(
                    key = when (dbParamEntity.key) {
                        KeyDbEntity.TOP_K -> Param.Key.TOP_K
                        KeyDbEntity.TOP_P -> Param.Key.TOP_P
                        KeyDbEntity.MIN_P -> Param.Key.MIN_P
                        KeyDbEntity.TEMPERATURE -> Param.Key.TEMPERATURE
                        KeyDbEntity.NUM_CTX -> Param.Key.NUM_CTX
                        KeyDbEntity.REPEAT_PENALTY -> Param.Key.REPEAT_PENALTY
                    },
                    value = when (dbParamEntity.valueType) {
                        ValueTypeDbEntity.INT -> {
                            IntValue(dbParamEntity.value.toIntOrNull() ?: 0)
                        }

                        ValueTypeDbEntity.FLOAT -> {
                            FloatValue(dbParamEntity.value.toFloatOrNull() ?: 0f)
                        }

                        ValueTypeDbEntity.STRING -> StringValue(dbParamEntity.value)

                    },
                )
            },
        )
    }
}
