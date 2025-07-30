package com.mercuriy94.olliechat.data.api.model.config

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
internal data class AiModelsParamsConfigApi(

    @SerialName("models")
    val models: List<AiModelParamsConfigApi>,
)


@Serializable
data class AiModelParamsConfigApi(

    @SerialName("name")
    val name: String,

    @SerialName("model")
    val model: String,

    @SerialName("family")
    val family: String,

    @SerialName("params")
    val params: AiModelParamsApi,
)

@Serializable
data class AiModelParamsApi(

    @SerialName("top_k")
    val topK: Long? = null,

    @SerialName("temperature")
    val temperature: @Contextual BigDecimal? = null,

    @SerialName("top_p")
    val topP: @Contextual BigDecimal? = null,

    @SerialName("min_p")
    val minP: @Contextual BigDecimal? = null,

    @SerialName("num_ctx")
    val numCtx: Long? = null,

    @SerialName("repeat_penalty")
    val repeatPenalty: @Contextual BigDecimal? = null,
)
