package util

import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * In order to use it you have to add the `-Dkotlinx.coroutines.debug` line to the VM options in the configuration.
 * For more visit [kotlinx-coroutines-debug](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-debug/)
 */
val coroutineName: String
    get() {
        val regex = Regex(".+ @(.+)#.+")
        val matches = regex.find(Thread.currentThread().name)
        return matches?.groups?.get(1)?.value ?: Thread.currentThread().name
    }

suspend fun <T> withForceTimeoutOrNull(
    timeMillis: Long,
    block: suspend CoroutineScope.() -> T
): T? {
    val blockScope = CoroutineScope(context = coroutineContext)
    val timerScope = CoroutineScope(context = Dispatchers.IO)
    return select {
        blockScope.async(CoroutineName("async-block")) { block() }.onAwait { it }
        timerScope.async(CoroutineName("timer")) {
            delay(timeMillis)
            null
        }.onAwait { it }
    }
}

fun <T> runBlocking(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
): T {
    var result: T? = null
    CoroutineScope(context).launch { result = block() }
    while (result == null) {
        Thread.sleep(1)
    }
    return result!!
}