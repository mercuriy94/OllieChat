@file:Suppress("TooGenericExceptionThrown")

package com.mercuriy94.olliechat.data.langchain4j.http

import dev.langchain4j.exception.HttpException
import dev.langchain4j.exception.TimeoutException
import dev.langchain4j.http.client.HttpClient
import dev.langchain4j.http.client.HttpRequest
import dev.langchain4j.http.client.SuccessfulHttpResponse
import dev.langchain4j.http.client.sse.ServerSentEventListener
import dev.langchain4j.http.client.sse.ServerSentEventListenerUtils.ignoringExceptions
import dev.langchain4j.http.client.sse.ServerSentEventParser
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException

internal class Langchain4jOkHttpClient(
    private val okHttpClient: OkHttpClient,
    private val requestTag: String? = null,
) : HttpClient {

    override fun execute(request: HttpRequest): SuccessfulHttpResponse {
        return try {
            val okHttpResponse = okHttpClient.newCall(request.toOkHttpRequest()).execute()
            if (!okHttpResponse.isSuccessful) {
                throw HttpException(okHttpResponse.code, okHttpResponse.body.toString())
            }
            okHttpResponse.toLangchain4jResponse(okHttpResponse.body.string())
        } catch (e: SocketTimeoutException) {
            throw TimeoutException(e)
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

    override fun execute(
        request: HttpRequest,
        parser: ServerSentEventParser,
        listener: ServerSentEventListener,
    ) {
        okHttpClient.newCall(request.toOkHttpRequest())
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        val exception = HttpException(response.code, response.body.string())
                        ignoringExceptions { listener.onError(exception) }
                        return
                    }
                    val langchain4jResponse = response.toLangchain4jResponse()
                    ignoringExceptions { listener.onOpen(langchain4jResponse) }
                    try {
                        response.body.byteStream().use { inputStream ->
                            parser.parse(inputStream, listener)
                            ignoringExceptions(listener::onClose)
                        }
                    } catch (e: IOException) {
                        throw RuntimeException(e)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    if (e is SocketTimeoutException) {
                        ignoringExceptions { listener.onError(TimeoutException(e)) }
                    } else {
                        ignoringExceptions { listener.onError(e) }
                    }
                }
            })
    }

    private fun HttpRequest.toOkHttpRequest(): Request {
        val okHttpRequestBuilder = Request.Builder()
        okHttpRequestBuilder.url(url())
        headers()?.forEach { (name, values) ->
            values?.forEach { value -> okHttpRequestBuilder.addHeader(name, value) }
        }
        okHttpRequestBuilder.method(method().name, body()?.toRequestBody())
        okHttpRequestBuilder.tag(requestTag)
        return okHttpRequestBuilder.build()
    }

    private fun Response.toLangchain4jResponse(body: String? = null): SuccessfulHttpResponse {
        return SuccessfulHttpResponse.builder()
            .statusCode(code)
            .headers(headers.toMultimap())
            .body(body)
            .build()
    }
}
