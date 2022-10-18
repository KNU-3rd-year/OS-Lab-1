package worker.basic

import os.lab1.compfuncs.basic.Concatenation
import util.Result
import util.toResult
import worker.Worker
import java.lang.IllegalArgumentException
import java.util.*
import java.util.concurrent.*

class ConcatenationAdapter(
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
) : Worker() {
    override suspend fun processF(parameter: Int): Result {
        val future = executor.submit { Concatenation.trialF(parameter) }
            as? Future<Optional<String>>
            ?: return Result.HardFailure(cause = IllegalArgumentException())

        return getResultFromFuture(future = future)
    }

    override suspend fun processG(parameter: Int): Result {
        val future = executor.submit { Concatenation.trialG(parameter) }
            as? Future<Optional<String>>
            ?: return Result.HardFailure(cause = IllegalArgumentException())

        return getResultFromFuture(future = future)
    }

    private fun getResultFromFuture(future: Future<Optional<String>>): Result {
        return try {
            val result = future.get(timeout, TimeUnit.MILLISECONDS)
            if (!result.isPresent) {
                Result.HardFailure(cause = IllegalArgumentException())
            }

            Result.Success(value = result.get())
        } catch (e: Exception) {
            e.toResult()
        }
    }
}