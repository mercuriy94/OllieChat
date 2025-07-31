package com.mercuriy94.olliechat.di

import com.mercuriy94.olliechat.data.api.AiModelsParamsConfigService
import com.mercuriy94.olliechat.data.api.serializer.BigDecimalNumericSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

internal object NetworkModule {

    //    const val OLLAMA_HOST = "http://192.168.3.3"
    const val OLLAMA_HOST = "http://192.168.3.18"

    //    const val OLLAMA_HOST = "http://127.0.0.1"
    const val OLLAMA_PORT = "11434"

    val okHttpClient: OkHttpClient by lazy(LazyThreadSafetyMode.NONE) {
        OkHttpClient.Builder().build()
    }

    val configService by lazy(LazyThreadSafetyMode.NONE) {
        Retrofit.Builder()
            .baseUrl("$OLLAMA_HOST:$OLLAMA_PORT")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create<AiModelsParamsConfigService>()
    }



    val json by lazy(LazyThreadSafetyMode.NONE) {
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            coerceInputValues = true
            serializersModule = SerializersModule {
                contextual(BigDecimalNumericSerializer())
            }
        }
    }
}
