package worker

import util.Result

abstract class Worker(
    protected val timeout: Long = 4000L
) {
    abstract suspend fun processF(parameter: Int): Result
    abstract suspend fun processG(parameter: Int): Result
}