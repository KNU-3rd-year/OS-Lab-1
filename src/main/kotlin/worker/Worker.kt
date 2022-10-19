package worker

interface Worker {
    suspend fun processF(getParameter: suspend () -> Int): WorkerResult
    suspend fun processG(getParameter: suspend () -> Int): WorkerResult
}