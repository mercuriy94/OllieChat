package com.mercuriy94.olliechat.data.repository.config

import android.content.Context
import com.mercuriy94.olliechat.R
import com.mercuriy94.olliechat.data.api.model.config.AiModelsParamsConfigApi
import com.mercuriy94.olliechat.utils.CoroutineDispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

internal class AiModelsParamsConfigRepositoryImpl(
    private val coroutineDispatchers: CoroutineDispatchers,
    private val appContext: Context,
    private val json: Json,
) {

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun getConfig(): AiModelsParamsConfigApi {
        return withContext(coroutineDispatchers.io) {
            appContext.resources.openRawResource(R.raw.ai_models_params_config)
                .use { json.decodeFromStream<AiModelsParamsConfigApi>(it) }
        }
    }
}
