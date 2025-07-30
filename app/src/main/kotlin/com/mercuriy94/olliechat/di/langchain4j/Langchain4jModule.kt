package com.mercuriy94.olliechat.di.langchain4j

import com.mercuriy94.olliechat.data.langchain4j.http.Langchain4jOkHttpClientBuilder
import com.mercuriy94.olliechat.di.NetworkModule
import com.mercuriy94.olliechat.di.NetworkModule.OLLAMA_HOST
import com.mercuriy94.olliechat.di.NetworkModule.OLLAMA_PORT
import dev.langchain4j.model.ollama.OllamaModels

internal object Langchain4jModule {

    val ollamaModels: OllamaModels by lazy {
        OllamaModels.builder()
            .baseUrl("$OLLAMA_HOST:$OLLAMA_PORT")
            .httpClientBuilder(
                Langchain4jOkHttpClientBuilder(
                    NetworkModule.okHttpClient.newBuilder()
                )
            )
            .maxRetries(0)
            .logRequests(false)
            .logResponses(false)
            .build()
    }
}
