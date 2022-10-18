package worker.basic

import os.lab1.compfuncs.basic.Concatenation
import util.Result
import util.toResult
import worker.Worker
import java.util.*

class BasicConcatenation : Worker {
    override suspend fun processF(getParameter: suspend () -> Int): Result {
        val parameter: Int = getParameter()
        return getResultFromFuture { Concatenation.trialF(parameter) }
    }

    override suspend fun processG(getParameter: suspend () -> Int): Result {
        val parameter: Int = getParameter()
        return getResultFromFuture { Concatenation.trialG(parameter) }
    }

    private fun getResultFromFuture(getOptional: () -> Optional<String>): Result {
        return try {
            val result = getOptional()
            if (!result.isPresent) {
                Result.HardFailure(cause = IllegalArgumentException())
            }

            Result.Success(value = result.get())
        } catch (e: Exception) {
            e.toResult()
        }
    }
}