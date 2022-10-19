package worker.advanced

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.supervisorScope
import os.lab1.compfuncs.advanced.Concatenation
import util.coroutineName
import worker.Worker
import worker.WorkerResult
import worker.toResult
import java.util.*
import java.util.concurrent.TimeoutException


class AdvancedConcatenation(private val timeout: Long = 4_000L) : Worker {
    override suspend fun processF(getParameter: suspend () -> Int): WorkerResult {
        val parameter: Int = getParameter()
        return getResultFromFuture { Concatenation.trialF(parameter) }
    }

    override suspend fun processG(getParameter: suspend () -> Int): WorkerResult {
        val parameter: Int = getParameter()
        return getResultFromFuture { Concatenation.trialG(parameter) }
    }

    private suspend fun getResultFromFuture(getOptional: () -> Optional<Optional<String>>): WorkerResult {
        return supervisorScope {
            val def = async {
                delay(timeout)
                if (isActive) {
                    println("Coroutine $coroutineName is running for too long. The TimeoutException has been thrown!")
                    throw TimeoutException()
                }
            }

            try {
                println("Coroutine $coroutineName try to got the value from Concatenation (advanced).")
                def.await()
                val optionalResult = getOptional()
                def.cancel()
                println("Coroutine $coroutineName has got the value from Concatenation (advanced).")

                if (!optionalResult.isPresent) {
                    WorkerResult.SoftFailure(cause = IllegalArgumentException())
                }

                val result = optionalResult.get()
                if (!result.isPresent) {
                    WorkerResult.HardFailure(cause = IllegalArgumentException())
                }

                WorkerResult.Success(value = result.get())
            } catch (e: Exception) {
                e.toResult()
            }
        }
    }
}