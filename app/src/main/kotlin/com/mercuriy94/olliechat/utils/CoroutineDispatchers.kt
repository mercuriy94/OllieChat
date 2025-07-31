package com.mercuriy94.olliechat.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface CoroutineDispatchers {

    val io: CoroutineDispatcher

    val main: CoroutineDispatcher

    val default: CoroutineDispatcher
}

internal class CoroutineDispatchersImpl : CoroutineDispatchers {

    override val io: CoroutineDispatcher
        get() = Dispatchers.IO

    override val main: CoroutineDispatcher
        get() = Dispatchers.Main

    override val default: CoroutineDispatcher
        get() = Dispatchers.Default
}
