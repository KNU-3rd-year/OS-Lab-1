package manager

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import manager.controller.ControllerResult

class FSM {
    private var state: State = State.NoFunctionCompleted
    val mutex = Mutex()

    suspend fun getState(): State = mutex.withLock { return@withLock state }

    suspend fun setF(result: ControllerResult) = mutex.withLock {
        state = when (result) {
            is ControllerResult.Failure -> State.Failure(cause = result.cause)
            is ControllerResult.Success -> {
                when (val s = state) {
                    is State.FunctionGCompleted -> State.BothFunctionsCompleted(fValue = result.value, gValue = s.gValue)
                    is State.NoFunctionCompleted -> State.FunctionFCompleted(fValue = result.value)
                    is State.BothFunctionsCompleted,
                    is State.Failure,
                    is State.FunctionFCompleted -> s
                }
            }
        }
    }

    suspend fun setG(result: ControllerResult) = mutex.withLock {
        state = when (result) {
            is ControllerResult.Failure -> State.Failure(cause = result.cause)
            is ControllerResult.Success -> {
                when (val s = state) {
                    is State.FunctionFCompleted -> State.BothFunctionsCompleted(fValue = result.value, gValue = s.fValue)
                    is State.NoFunctionCompleted -> State.FunctionGCompleted(gValue = result.value)
                    is State.BothFunctionsCompleted,
                    is State.Failure,
                    is State.FunctionGCompleted -> s
                }
            }
        }
    }

    sealed interface State {
        object NoFunctionCompleted : State
        data class FunctionFCompleted(val fValue: String) : State
        data class FunctionGCompleted(val gValue: String) : State
        data class BothFunctionsCompleted(val fValue: String, val gValue: String) : State
        data class Failure(val cause: Exception) : State
    }
}