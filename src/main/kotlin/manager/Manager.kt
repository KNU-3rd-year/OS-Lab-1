package manager

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import lc.kra.system.keyboard.GlobalKeyboardHook
import lc.kra.system.keyboard.event.GlobalKeyAdapter
import lc.kra.system.keyboard.event.GlobalKeyEvent
import manager.controller.WorkerController
import util.CalculationResult
import util.coroutineName
import worker.Worker

class Manager(
    private val managerScope: CoroutineScope = CoroutineScope(context = Dispatchers.Default),
    private val workersScope: CoroutineScope = CoroutineScope(context = Dispatchers.IO),
) {
    private val keyboardHook: GlobalKeyboardHook = GlobalKeyboardHook(true)
    private val globalKeyAdapter: GlobalKeyAdapter = object : GlobalKeyAdapter() {
        override fun keyPressed(event: GlobalKeyEvent) {
            if (event.virtualKeyCode == GlobalKeyEvent.VK_Q) {
                workersScope.cancel()
            }
        }
    }

    fun start(parameter: Int, worker: Worker): Job {
        addCancelListener()

        return managerScope.launch(CoroutineName("manager")) {
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
            if (workersScope.isActive) workersScope.cancel()
            removeCancelListener()
        }
    }

    private suspend fun coordinate(parameter: Int, worker: Worker): CalculationResult {
        val mutex = Mutex()
        val getParameter: suspend () -> Int = { mutex.withLock { return@withLock parameter } }
        val fsm = FSM()

        workersScope.launch(CoroutineName("F")) {
            println("The function $coroutineName started working.")
            val res = WorkerController().compute(0) { worker.processF(getParameter = getParameter) }
            fsm.setF(result = res)
        }
        workersScope.launch(CoroutineName("G")) {
            println("The function $coroutineName started working.")
            val res = WorkerController().compute(0) { worker.processG(getParameter = getParameter) }
            fsm.setG(result = res)
        }

        while (workersScope.isActive) {
            when (val state = fsm.getState()) {
                is FSM.State.BothFunctionsCompleted -> return CalculationResult.Success(fValue = state.fValue, gValue = state.gValue)
                is FSM.State.Failure -> return CalculationResult.Failure(cause = state.cause)
                is FSM.State.FunctionFCompleted,
                is FSM.State.FunctionGCompleted,
                is FSM.State.NoFunctionCompleted -> delay(1)
            }
        }
        return CalculationResult.Failure(cause = Exception("program was canceled"))
    }

    private fun binaryOperation(arg1: String, arg2: String): String {
        return arg1 + arg2
    }

    private fun addCancelListener() {
        keyboardHook.addKeyListener(globalKeyAdapter)
    }

    private fun removeCancelListener() {
        keyboardHook.removeKeyListener(globalKeyAdapter)
    }
}