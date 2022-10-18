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
            println("The function F started working.")
            compute(0, "F") { worker.processF(getParameter = getParameter) }
        }
        val g: Deferred<String?> = scope.async {
            println("The function G started working.")
            compute(0, "G") { worker.processG(getParameter = getParameter) }
        }

        val pair = scope.async {
            var resultF: String? = null
            var resultG: String? = null

            val _f = scope.launch {
                val res = f.await()
                if (res == null) {
                    println("The function F has finished its work with the hard failure.")
                    if (g.isActive) {
                        g.cancel()
                        println("The function G has been canceled.")
                    }
                } else {
                    println("The function F has successfully finished its work with value $res.")
                    resultF = res
                }
            }
            val _g = scope.launch {
                val res = g.await()
                if (res == null) {
                    println("The function G has finished its work with the hard failure.")
                    if (g.isActive) {
                        f.cancel()
                        println("The function F has been canceled.")
                    }
                } else {
                    println("The function G has successfully finished its work with value $res.")
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
                println("The functions G and F has finished their work with the failure.")
            } else {
                println("The functions G and F has successfully finished its work.")
                println("The result of the function F is ${t.first}")
                println("The result of the function G is ${t.second}")
                println("The result of the binary Operation is ${binaryOperation(t.first, t.second)}")
            }
        }
    }

    private suspend fun compute(tries: Int, funcName: String, getResult: suspend () -> Result): String? {
        val result = getResult()
        return when (result) {
            is Result.Success -> result.value
            is Result.HardFailure -> null
            is Result.SoftFailure -> {
                println("The function $funcName has finished its work with the soft failure.")
                if (tries < 3) {
                    println("The function $funcName re-started working (${tries + 1} time).")
                    compute(tries + 1, funcName, getResult)
                } else {
                    println("The function $funcName has soft failed 3 times. Now it is considered to be a hard failure.")
                    null
                }
            }
        }
    }

    private fun binaryOperation(arg1: String, arg2: String): String {
        return arg1 + arg2
    }
}