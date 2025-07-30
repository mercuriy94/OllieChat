package com.mercuriy94.olliechat.data.mapper.model

import com.mercuriy94.olliechat.data.api.model.config.AiModelParamsConfigApi
import com.mercuriy94.olliechat.data.api.model.config.AiModelsParamsConfigApi
import com.mercuriy94.olliechat.data.db.model.OllieDetailsModelDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieModelDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieModelDbEntityWithRelationships
import com.mercuriy94.olliechat.data.db.model.OllieModelParamDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieModelParamDbEntity.KeyDbEntity
import com.mercuriy94.olliechat.data.db.model.OllieModelParamDbEntity.ValueTypeDbEntity
import dev.langchain4j.model.ollama.OllamaModel

internal class OllieModelApiToDbEntityWithRelationshipsMapper {

    operator fun invoke(
        apiModels: List<OllamaModel>,
        modelsParamsConfigApi: AiModelsParamsConfigApi?,
    ): List<OllieModelDbEntityWithRelationships> =
        apiModels.map({ ollamaModel ->
            invoke(
                apiModel = ollamaModel,
                modelParamsConfigApi = modelsParamsConfigApi?.models
                    ?.firstOrNull {
                        it.model == ollamaModel.model &&
                                it.name == ollamaModel.name &&
                                it.family == ollamaModel.details.family
                    }
            )
        })

    operator fun invoke(
        apiModel: OllamaModel,
        modelParamsConfigApi: AiModelParamsConfigApi?,
    ): OllieModelDbEntityWithRelationships {
        return OllieModelDbEntityWithRelationships(
            model = OllieModelDbEntity(
                name = apiModel.name,
                digest = apiModel.digest,
                size = apiModel.size
            ),
            details = OllieDetailsModelDbEntity(
                parameterSize = apiModel.details.parameterSize,
                quantizationLevel = apiModel.details.quantizationLevel,
            ),
            params = modelParamsConfigApi?.params?.let { params ->
                buildList {
                    params.temperature?.let {
                        add(
                            OllieModelParamDbEntity(
                                key = KeyDbEntity.TEMPERATURE,
                                value = it.toString(),
                                valueType = ValueTypeDbEntity.FLOAT,
                            )
                        )
                    }

                    params.topK?.let {
                        add(
                            OllieModelParamDbEntity(
                                key = KeyDbEntity.TOP_K,
                                value = it.toString(),
                                valueType = ValueTypeDbEntity.INT,
                            )
                        )
                    }

                    params.topP?.let {
                        add(
                            OllieModelParamDbEntity(
                                key = KeyDbEntity.TOP_P,
                                value = it.toString(),
                                valueType = ValueTypeDbEntity.FLOAT,
                            )
                        )
                    }

                    params.minP?.let {
                        add(
                            OllieModelParamDbEntity(
                                key = KeyDbEntity.MIN_P,
                                value = it.toString(),
                                valueType = ValueTypeDbEntity.FLOAT,
                            )
                        )
                    }

                    params.numCtx?.let {
                        add(
                            OllieModelParamDbEntity(
                                key = KeyDbEntity.NUM_CTX,
                                value = it.toString(),
                                valueType = ValueTypeDbEntity.INT,
                            )
                        )
                    }

                    params.repeatPenalty?.let {
                        add(
                            OllieModelParamDbEntity(
                                key = KeyDbEntity.REPEAT_PENALTY,
                                value = it.toString(),
                                valueType = ValueTypeDbEntity.FLOAT,
                            )
                        )
                    }
                }
            } ?: emptyList()
        )
    }
}
