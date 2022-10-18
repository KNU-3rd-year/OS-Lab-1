package worker

import util.Result

interface Worker {
    suspend fun processF(getParameter: suspend () -> Int): Result
    suspend fun processG(getParameter: suspend () -> Int): Result
}