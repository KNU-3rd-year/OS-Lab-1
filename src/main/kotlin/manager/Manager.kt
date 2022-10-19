package manager

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import manager.controller.WorkerController
import util.CalculationResult
import util.coroutineName
import worker.Worker

class Manager {
    fun start(parameter: Int, worker: Worker): Job {
        val scope = CoroutineScope(context = Dispatchers.Default)

        return scope.launch(CoroutineName("manager")) {
            when (val result = coordinate(parameter, worker)) {
                is CalculationResult.Failure -> {
                    println("The functions G and F has finished their work with the failure.")
                }
                is CalculationResult.Success -> {
                    println("The functions G and F has successfully finished its work.")
                    println("The result of the function F is ${result.fValue}")
                    println("The result of the function G is ${result.gValue}")
                    println("The result of the binary Operation is ${binaryOperation(result.fValue, result.gValue)}")
                }
            }
        }
    }

    private suspend fun coordinate(parameter: Int, worker: Worker): CalculationResult {
        val scope = CoroutineScope(context = Dispatchers.IO)
        val mutex = Mutex()
        val getParameter: suspend () -> Int = { mutex.withLock { return@withLock parameter } }
        val fsm = FSM()

        scope.launch(CoroutineName("F")) {
            println("The function $coroutineName started working.")
            val res = WorkerController().compute(0) { worker.processF(getParameter = getParameter) }
            fsm.setF(result = res)
        }
        scope.launch(CoroutineName("G")) {
            println("The function $coroutineName started working.")
            val res = WorkerController().compute(0) { worker.processG(getParameter = getParameter) }
            fsm.setG(result = res)
        }

        while (true) {
            when (val state = fsm.getState()) {
                is FSM.State.BothFunctionsCompleted -> {
                    scope.cancel()
                    return CalculationResult.Success(fValue = state.fValue, gValue = state.gValue)
                }
                is FSM.State.Failure -> {
                    scope.cancel()
                    return CalculationResult.Failure(cause = state.cause)
                }
                is FSM.State.FunctionFCompleted,
                is FSM.State.FunctionGCompleted,
                is FSM.State.NoFunctionCompleted -> delay(1)
            }
        }
    }

    private fun binaryOperation(arg1: String, arg2: String): String {
        return arg1 + arg2
    }
}