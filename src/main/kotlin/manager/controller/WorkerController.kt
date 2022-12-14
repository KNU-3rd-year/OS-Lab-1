package manager.controller

import kotlinx.coroutines.isActive
import util.coroutineName
import worker.WorkerResult
import kotlin.coroutines.coroutineContext

class WorkerController {

    suspend fun compute(tries: Int, getWorkerResult: suspend () -> WorkerResult): ControllerResult {
        if (!coroutineContext.isActive) return ControllerResult.Failure(cause = IllegalStateException())
        val result = getWorkerResult()
        if (!coroutineContext.isActive) return ControllerResult.Failure(cause = IllegalStateException())

        return when (result) {
            is WorkerResult.Success -> {
                println("The function $coroutineName has successfully finished its work with value \"${result.value}\".")
                ControllerResult.Success(value = result.value)
            }
            is WorkerResult.HardFailure -> {
                println("The function $coroutineName has finished its work with the hard failure. Reason: ${result.cause.localizedMessage}")
                ControllerResult.Failure(cause = result.cause)
            }
            is WorkerResult.SoftFailure -> handleSoftFailure(result, tries, getWorkerResult)
        }
    }

    private suspend fun handleSoftFailure(
        result: WorkerResult.SoftFailure,
        tries: Int,
        getWorkerResult: suspend () -> WorkerResult
    ): ControllerResult {
        if (!coroutineContext.isActive) return ControllerResult.Failure(cause = IllegalStateException())

        println("The function $coroutineName has finished its work with the soft failure. Reason: ${result.cause.localizedMessage}")
        return if (tries < 3) {
            println("The function $coroutineName re-started working (${tries + 1} time).")
            compute(tries + 1, getWorkerResult)
        } else {
            println("The function $coroutineName has soft failed 3 times. Now it is considered to be a hard failure.")
            ControllerResult.Failure(cause = Exception("Too many attempts to compute"))
        }
    }
}