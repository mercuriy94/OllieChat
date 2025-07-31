package com.mercuriy94.olliechat.di

import kotlinx.serialization.json.Json

internal object SerializationModule {

    val json by lazy(LazyThreadSafetyMode.NONE) {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
}
