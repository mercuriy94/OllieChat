package com.mercuriy94.olliechat.data.repository.chat.manager

import com.mercuriy94.olliechat.data.langchain4j.http.Langchain4jOkHttpClientBuilder
import com.mercuriy94.olliechat.di.NetworkModule.OLLAMA_HOST
import com.mercuriy94.olliechat.di.NetworkModule.OLLAMA_PORT
import com.mercuriy94.olliechat.domain.entity.model.OllieModel
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param.Key.MIN_P
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param.Key.NUM_CTX
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param.Key.REPEAT_PENALTY
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param.Key.TEMPERATURE
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param.Key.TOP_K
import com.mercuriy94.olliechat.domain.entity.model.OllieModel.Param.Key.TOP_P
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import dev.langchain4j.model.ollama.OllamaStreamingChatModel.OllamaStreamingChatModelBuilder
import okhttp3.OkHttpClient
import java.time.Duration

private const val DEFAULT_HTTP_TIMEOUT_SEC = 1200L

internal interface OllamaChatModelFactory {

    fun createStreamingChatModel(
        tag: String,
        model: OllieModel,
        okHttpClient: OkHttpClient? = null,
    ): OllamaStreamingChatModel

}

internal class OllamaChatModelFactoryImpl : OllamaChatModelFactory {

    override fun createStreamingChatModel(
        tag: String,
        model: OllieModel,
        okHttpClient: OkHttpClient?,
    ): OllamaStreamingChatModel {
        val okHttpClientBuilder = okHttpClient?.newBuilder() ?: OkHttpClient.Builder()
        return OllamaStreamingChatModel.builder()
            .baseUrl("$OLLAMA_HOST:$OLLAMA_PORT")
            .timeout(Duration.ofSeconds(DEFAULT_HTTP_TIMEOUT_SEC))
            .httpClientBuilder(
                Langchain4jOkHttpClientBuilder(okHttpClientBuilder)
                    .requestTag(tag)
            )
            .modelName(model.name)
            .addModelParameters(model)
            .build()
    }

    private fun OllamaStreamingChatModelBuilder.addModelParameters(
        model: OllieModel,
    ): OllamaStreamingChatModelBuilder {
        return apply {
            model.params.forEach { param ->
                when (param.key) {
                    TOP_K -> {
                        (param.value as? OllieModel.Param.Value.IntValue)
                            ?.value?.also(::topK)
                    }

                    TOP_P -> {
                        (param.value as? OllieModel.Param.Value.FloatValue)
                            ?.value?.toBigDecimal()?.toDouble()?.also(::topP)
                    }

                    MIN_P -> {
                        (param.value as? OllieModel.Param.Value.FloatValue)
                            ?.value?.toBigDecimal()?.toDouble()?.also(::minP)
                    }

                    TEMPERATURE -> {
                        (param.value as? OllieModel.Param.Value.FloatValue)
                            ?.value?.toBigDecimal()?.toDouble()?.also(::temperature)
                    }

                    NUM_CTX -> {
                        (param.value as? OllieModel.Param.Value.IntValue)
                            ?.value?.also(::numCtx)
                    }

                    REPEAT_PENALTY -> {
                        (param.value as? OllieModel.Param.Value.FloatValue)
                            ?.value?.toBigDecimal()?.toDouble()?.also(::repeatPenalty)
                    }
                }
            }
        }
    }
}
