package com.mercuriy94.olliechat.utils.ext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.cancellation.CancellationException

inline fun <R> runSuspendCatching(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (c: CancellationException) {
        throw c
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

inline fun <R, T : R> Result<T>.recoverSuspendCatching(transform: (exception: Throwable) -> R): Result<R> {
    return when (val exception = exceptionOrNull()) {
        null -> this
        else -> runSuspendCatching { transform(exception) }
    }
}

fun Throwable.ifNonCancellation(block: Throwable.() -> Unit) {
    if (this !is CancellationException) {
        block()
    }
}

suspend fun Mutex.waitUnlock() {
    lock()
    unlock()
}

/*** Guarantees delay before return data or throw error
 * @param delay Time in ms for delaying block() result sending
 * @param block Block to be executed **/
suspend inline fun <T> withMinExecutionTime(delay: Long, crossinline block: suspend () -> T): T {
    return coroutineScope {
        val minExecutionTime = async { delay(delay) }
        val result = runSuspendCatching { block() }
            .onFailure { minExecutionTime.await() }
            .getOrThrow()
        minExecutionTime.await()
        return@coroutineScope result
    }
}

suspend inline fun <T1, T2, R> asyncAwait(
    noinline s1: suspend CoroutineScope.() -> T1,
    noinline s2: suspend CoroutineScope.() -> T2,
    crossinline transform: suspend (T1, T2) -> R,
): R {
    return coroutineScope {
        val s1Async = async(block = s1)
        val s2Async = async(block = s2)
        transform(
            s1Async.await(),
            s2Async.await()
        )
    }
}

suspend inline fun <T1, T2, T3, R> asyncAwait(
    noinline s1: suspend CoroutineScope.() -> T1,
    noinline s2: suspend CoroutineScope.() -> T2,
    noinline s3: suspend CoroutineScope.() -> T3,
    crossinline transform: suspend (T1, T2, T3) -> R,
): R {
    return coroutineScope {
        val s1Async = async(block = s1)
        val s2Async = async(block = s2)
        val s3Async = async(block = s3)
        transform(
            s1Async.await(),
            s2Async.await(),
            s3Async.await(),
        )
    }
}
