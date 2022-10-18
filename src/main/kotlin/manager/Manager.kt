package manager

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import util.Result
import worker.Worker

class Manager {
    fun start(parameter: Int, worker: Worker): Deferred<Unit> {
        val scope = CoroutineScope(context = Dispatchers.IO)
        val mutex = Mutex()
        val getParameter: suspend () -> Int = { mutex.withLock { return@withLock parameter } }

        val f: Deferred<String?> = scope.async {
            compute(0) { worker.processF(getParameter = getParameter) }
        }
        val g: Deferred<String?> = scope.async {
            compute(0) { worker.processG(getParameter = getParameter) }
        }

        val pair = scope.async {
            var resultF: String? = null
            var resultG: String? = null

            val _f = scope.launch {
                val res = f.await()
                if (res == null) {
                    g.cancel()
                    // TODO: write comment that this was canceled
                } else {
                    resultF = res
                }
            }
            val _g = scope.launch {
                val res = g.await()
                if (res == null) {
                    f.cancel()
                    // TODO: write comment that this was canceled
                } else {
                    resultG = res
                }
            }

            while (!(_f.isCompleted && _g.isCompleted)) {
                delay(1)
            }
            if (resultF == null || resultG == null) {
                return@async null
            } else {
                return@async Pair(resultF!!, resultG!!)
            }
        }

        return scope.async {
            val t = pair.await()
            if (t == null) {
                println("NULL")
            } else {
                println(binaryOperation(t.first, t.second))
            }
        }
    }

    private suspend fun compute(tries: Int, getResult: suspend () -> Result): String? {
        val result = getResult()
        return when (result) {
            is Result.Success -> result.value
            is Result.HardFailure -> null
            is Result.SoftFailure -> {
                if (tries < 3) {
                    compute(tries + 1, getResult)
                } else {
                    null
                }
            }
        }
    }

    private fun binaryOperation(arg1: String, arg2: String): String {
        return arg1 + arg2
    }
}