package worker.advanced

import kotlinx.coroutines.withTimeout
import os.lab1.compfuncs.advanced.Concatenation
import util.Result
import util.toResult
import worker.Worker
import java.util.*


class AdvancedConcatenation : Worker {
    override suspend fun processF(getParameter: suspend () -> Int): Result {
        val parameter: Int = getParameter()
        return getResultFromFuture { Concatenation.trialF(parameter) }
    }

    override suspend fun processG(getParameter: suspend () -> Int): Result {
        val parameter: Int = getParameter()
        return getResultFromFuture { Concatenation.trialG(parameter) }
    }

    private suspend fun getResultFromFuture(getOptional: () -> Optional<Optional<String>>): Result {
        return try {
            val optionalResult = withTimeout(4000L) { getOptional() }
            if (!optionalResult.isPresent) {
                Result.SoftFailure(cause = IllegalArgumentException())
            }

            val result = optionalResult.get()
            if (!result.isPresent) {
                Result.HardFailure(cause = IllegalArgumentException())
            }

            Result.Success(value = result.get())
        } catch (e: Exception) {
            e.toResult()
        }
    }
}