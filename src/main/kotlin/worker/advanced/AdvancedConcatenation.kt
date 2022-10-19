package worker.advanced

import kotlinx.coroutines.withTimeout
import os.lab1.compfuncs.advanced.Concatenation
import util.coroutineName
import worker.Worker
import worker.WorkerResult
import worker.toResult
import java.util.*


class AdvancedConcatenation : Worker {
    override suspend fun processF(getParameter: suspend () -> Int): WorkerResult {
        return getResultFromFuture { Concatenation.trialF(getParameter()) }
    }

    override suspend fun processG(getParameter: suspend () -> Int): WorkerResult {
        return getResultFromFuture { Concatenation.trialG(getParameter()) }
    }

    private suspend fun getResultFromFuture(getOptional: suspend () -> Optional<Optional<String>>): WorkerResult {
        return try {
            println("Coroutine $coroutineName try to got the value from Concatenation (advanced).")
            val optionalResult = withTimeout(1000L) { getOptional() }
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