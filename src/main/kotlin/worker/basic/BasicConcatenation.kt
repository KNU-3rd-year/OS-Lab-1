package worker.basic

import kotlinx.coroutines.withTimeout
import os.lab1.compfuncs.basic.Concatenation
import worker.Worker
import worker.WorkerResult
import worker.toResult
import java.util.*

class BasicConcatenation : Worker {
    override suspend fun processF(getParameter: suspend () -> Int): WorkerResult {
        val parameter: Int = getParameter()
        return getResultFromFuture { Concatenation.trialF(parameter) }
    }

    override suspend fun processG(getParameter: suspend () -> Int): WorkerResult {
        val parameter: Int = getParameter()
        return getResultFromFuture { Concatenation.trialG(parameter) }
    }

    private suspend fun getResultFromFuture(getOptional: () -> Optional<String>): WorkerResult {
        return try {
            val result = withTimeout(1_000L) { getOptional() }
            if (!result.isPresent) {
                WorkerResult.HardFailure(cause = IllegalArgumentException())
            }

            WorkerResult.Success(value = result.get())
        } catch (e: Exception) {
            e.toResult()
        }
    }
}