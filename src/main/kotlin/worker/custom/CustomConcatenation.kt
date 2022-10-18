package worker.custom

import util.Result
import worker.Worker
import kotlin.random.Random

class CustomConcatenation : Worker {
    override suspend fun processF(getParameter: suspend () -> Int): Result {
        val parameter: Int = getParameter()
        val random = Random.Default
        return when (random.nextInt(10)) {
            0 -> Result.HardFailure(cause = IllegalStateException())
            1 -> Result.SoftFailure(cause = IllegalArgumentException())
            else -> {
                val ans = ""
                repeat(parameter) {
                    ans.plus("a")
                }
                Result.Success(value = ans)
            }
        }
    }

    override suspend fun processG(getParameter: suspend () -> Int): Result {
        val parameter: Int = getParameter()
        val random = Random.Default
        return when (random.nextInt(10)) {
            0 -> Result.HardFailure(cause = IllegalStateException())
            1 -> Result.SoftFailure(cause = IllegalArgumentException())
            else -> {
                val ans = ""
                repeat(parameter) {
                    ans.plus("A")
                }
                Result.Success(value = ans)
            }
        }
    }
}