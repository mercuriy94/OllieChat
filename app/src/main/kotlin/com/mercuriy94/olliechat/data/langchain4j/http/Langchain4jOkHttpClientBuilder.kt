package com.mercuriy94.olliechat.data.langchain4j.http

import dev.langchain4j.http.client.HttpClient
import dev.langchain4j.http.client.HttpClientBuilder
import okhttp3.OkHttpClient
import java.time.Duration

internal class Langchain4jOkHttpClientBuilder(
    private val okHttpClientBuilder: OkHttpClient.Builder = OkHttpClient.Builder(),
) : HttpClientBuilder {

    private var connectionTimeout: Duration? = null
    private var readTimeout: Duration? = null
    private var writeTimeout: Duration? = null
    private var requestTag: String? = null

    override fun connectTimeout(): Duration? = connectionTimeout

    override fun connectTimeout(timeout: Duration?): Langchain4jOkHttpClientBuilder {
        connectionTimeout = timeout
        return this
    }

    override fun readTimeout(): Duration? = readTimeout

    override fun readTimeout(timeout: Duration?): Langchain4jOkHttpClientBuilder {
        readTimeout = timeout
        return this
    }

    fun writeTimeout(): Duration? = writeTimeout

    fun writeTimeout(timeout: Duration?): Langchain4jOkHttpClientBuilder {
        writeTimeout = timeout
        return this
    }

    fun requestTag(): String? = requestTag

    fun requestTag(tag: String): Langchain4jOkHttpClientBuilder {
        requestTag = tag
        return this
    }

    override fun build(): HttpClient {
        val okHttpClient = okHttpClientBuilder.apply {
            connectionTimeout?.also(::connectTimeout)
            readTimeout?.also(::readTimeout)
            writeTimeout?.also(::writeTimeout)
        }.build()
        return Langchain4jOkHttpClient(
            okHttpClient = okHttpClient,
            requestTag = requestTag
        )
    }
}
