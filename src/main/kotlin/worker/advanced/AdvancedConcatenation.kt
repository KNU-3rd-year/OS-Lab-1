package worker.advanced

import os.lab1.compfuncs.advanced.Concatenation
import util.withForceTimeoutOrNull
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
        return withForceTimeoutOrNull(timeout) {
            try {
                val optionalResult = getOptional()

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
        } ?: WorkerResult.SoftFailure(cause = TimeoutException("Timed out waiting for $timeout ms."))
    }
}