package com.mercuriy94.olliechat.data.mapper.model

import com.mercuriy94.olliechat.data.db.model.OllieDetailsModelDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieModelDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieModelDbEntityWithRelationships
import com.mercuriy94.olliechat.data.db.model.OllieModelParamDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieModelParamDbEntity.KeyDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieModelParamDbEntity.ValueTypeDbEntity
import com.mercuriy94.olliechat.domain.entity.model.OllieModel
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param

internal class OllieModelDomainToDbEntityWithRelationshipsMapper {

    operator fun invoke(domainModel: OllieModel): OllieModelDbEntityWithRelationships {
        return OllieModelDbEntityWithRelationships(
            model = OllieModelDbEntity(
                id = domainModel.id,
                name = domainModel.name,
                size = domainModel.size,
                digest = domainModel.digest,
            ),
            details = domainModel.details.let { domainDetails ->
                OllieDetailsModelDbEntity(
                    id = domainDetails.id,
                    parameterSize = domainDetails.parameterSize,
                    quantizationLevel = domainDetails.quantizationLevel,
                    modelId = domainModel.id,
                )
            },
            params = domainModel.params.map { domainParam ->
                val (valueDbEntity, valueTypeDbEntity) =
                    when (val domainValue = domainParam.value) {
                        is Param.Value.IntValue -> {
                            domainValue.value.toString() to ValueTypeDbEntity.INT
                        }

                        is Param.Value.FloatValue -> {
                            domainValue.value.toString() to ValueTypeDbEntity.FLOAT
                        }

                        is Param.Value.StringValue -> domainValue.value to ValueTypeDbEntity.STRING
                    }

                OllieModelParamDbEntity(
                    key = when (domainParam.key) {
                        Param.Key.TOP_K -> KeyDbEntity.TOP_K
                        Param.Key.TOP_P -> KeyDbEntity.TOP_P
                        Param.Key.MIN_P -> KeyDbEntity.MIN_P
                        Param.Key.TEMPERATURE -> KeyDbEntity.TEMPERATURE
                        Param.Key.NUM_CTX -> KeyDbEntity.NUM_CTX
                        Param.Key.REPEAT_PENALTY -> KeyDbEntity.REPEAT_PENALTY
                    },
                    value = valueDbEntity,
                    valueType = valueTypeDbEntity,
                    modelId = domainModel.id,
                )
            }
        )
    }
}
