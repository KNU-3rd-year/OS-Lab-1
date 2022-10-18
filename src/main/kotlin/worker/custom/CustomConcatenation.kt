package worker.custom

import util.Result
import worker.Worker
import kotlin.random.Random

class CustomConcatenation : Worker {
    override suspend fun processF(getParameter: suspend () -> Int): Result {
        val parameter: Int = getParameter()
        val random = Random.Default
        return when (random.nextInt(4)) {
            0 -> Result.HardFailure(cause = IllegalStateException())
            1 -> Result.SoftFailure(cause = IllegalArgumentException())
            else -> {
                val ans = buildString {
                    while (this.length < parameter) {
                        this.append("a")
                    }
                }
                Result.Success(value = ans)
            }
        }
    }

    override suspend fun processG(getParameter: suspend () -> Int): Result {
        val parameter: Int = getParameter()
        val random = Random.Default
        return when (random.nextInt(4)) {
            0 -> Result.HardFailure(cause = IllegalStateException())
            1 -> Result.SoftFailure(cause = IllegalArgumentException())
            else -> {
                val ans = buildString {
                    while (this.length < parameter) {
                        this.append("A")
                    }
                }
                Result.Success(value = ans)
            }
        }
    }
}