package util

import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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
    val scope = CoroutineScope(context = Job())
    var result: T? = null
    var isResultSet = false
    val mutex = Mutex()

    scope.launch(CoroutineName("block")) {
        val res = block()
        mutex.withLock {
            result = res
            isResultSet = true
        }
    }

    scope.launch(CoroutineName("timer")) {
        delay(timeMillis)
        mutex.withLock {
            if (!isResultSet) {
                result = null
                isResultSet = true
            }
        }
    }

    while (!isResultSet)
        delay(1L)
    scope.cancel()
    return result
}

suspend fun <T> withForceTimeoutOrNull2(
    timeMillis: Long,
    block: suspend CoroutineScope.() -> T
): T? {
    return select {
        suspendBlock(block).onAwait { it }
        suspendDelay<T>(timeMillis).onAwait { it }
    }
}

fun <T> suspendBlock(block: suspend CoroutineScope.() -> T): Deferred<T?> = CoroutineScope(Job()).async { block() }

fun <T> suspendDelay(timeMillis: Long): Deferred<T?> = CoroutineScope(Job()).async {
    delay(timeMillis)
    null
}