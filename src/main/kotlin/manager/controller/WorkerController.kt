package manager.controller

import util.coroutineName
import worker.WorkerResult

class WorkerController {

    suspend fun compute(tries: Int, getWorkerResult: suspend () -> WorkerResult): ControllerResult {
        val result = getWorkerResult()
        return when (result) {
            is WorkerResult.Success -> {
                println("The function $coroutineName has successfully finished its work with value \"${result.value}\".")
                ControllerResult.Success(value = result.value)
            }
            is WorkerResult.HardFailure -> {
                println("The function $coroutineName has finished its work with the hard failure.")
                ControllerResult.Failure(cause = result.cause)
            }
            is WorkerResult.SoftFailure -> handleSoftFailure(tries, getWorkerResult)
        }
    }

    private suspend fun handleSoftFailure(tries: Int, getWorkerResult: suspend () -> WorkerResult): ControllerResult {
        println("The function $coroutineName has finished its work with the soft failure.")
        return if (tries < 3) {
            println("The function $coroutineName re-started working (${tries + 1} time).")
            compute(tries + 1, getWorkerResult)
        } else {
            println("The function $coroutineName has soft failed 3 times. Now it is considered to be a hard failure.")
            ControllerResult.Failure(cause = Exception("Too many attempts to compute"))
        }
    }
}