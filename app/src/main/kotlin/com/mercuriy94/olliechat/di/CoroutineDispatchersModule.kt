package com.mercuriy94.olliechat.di

import com.mercuriy94.olliechat.utils.CoroutineDispatchers
import com.mercuriy94.olliechat.utils.CoroutineDispatchersImpl

object CoroutineDispatchersModule {

    val coroutineDispatchers: CoroutineDispatchers = CoroutineDispatchersImpl()
}
