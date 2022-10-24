package util

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.selects.select
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
    val scope = CoroutineScope(context = coroutineContext)
    return select {
        scope.async(CoroutineName("async-block")) { block() }.onAwait { it }
        scope.async(CoroutineName("timer")) {
            delay(timeMillis)
            null
        }.onAwait { it }
    }
}