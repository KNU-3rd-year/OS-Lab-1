package worker.custom

import worker.Worker
import worker.WorkerResult
import kotlin.random.Random

class CustomConcatenation : Worker {
    override suspend fun processF(getParameter: suspend () -> Int): WorkerResult {
        val parameter: Int = getParameter()
        val random = Random.Default
        return when (random.nextInt(4)) {
            0 -> WorkerResult.HardFailure(cause = IllegalStateException())
            1 -> WorkerResult.SoftFailure(cause = IllegalArgumentException())
            else -> {
                val ans = buildString {
                    while (this.length < parameter) {
                        this.append("a")
                    }
                }
                WorkerResult.Success(value = ans)
            }
        }
    }

    override suspend fun processG(getParameter: suspend () -> Int): WorkerResult {
        val parameter: Int = getParameter()
        val random = Random.Default
        return when (random.nextInt(4)) {
            0 -> WorkerResult.HardFailure(cause = IllegalStateException())
            1 -> WorkerResult.SoftFailure(cause = IllegalArgumentException())
            else -> {
                val ans = buildString {
                    while (this.length < parameter) {
                        this.append("A")
                    }
                }
                WorkerResult.Success(value = ans)
            }
        }
    }
}