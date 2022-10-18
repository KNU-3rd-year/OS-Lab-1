package worker

import util.Result

abstract class Worker(
    protected val timeout: Long = 4000L
) {
    abstract suspend fun processF(getParameter: suspend () -> Int): Result
    abstract suspend fun processG(getParameter: suspend () -> Int): Result
}