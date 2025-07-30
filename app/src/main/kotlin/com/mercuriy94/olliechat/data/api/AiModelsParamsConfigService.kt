package com.mercuriy94.olliechat.data.api

import com.mercuriy94.olliechat.data.api.model.config.AiModelsParamsConfigApi
import retrofit2.http.GET

internal interface AiModelsParamsConfigService {

    @GET("ai/models/params")
    suspend fun getAiModelsParams(): AiModelsParamsConfigApi
}
