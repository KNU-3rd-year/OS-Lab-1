package manager

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import lc.kra.system.keyboard.GlobalKeyboardHook
import lc.kra.system.keyboard.event.GlobalKeyAdapter
import lc.kra.system.keyboard.event.GlobalKeyEvent
import manager.controller.WorkerController
import util.*
import worker.Worker

class Manager(
    private val managerScope: CoroutineScope = CoroutineScope(context = Dispatchers.Default),
    workerDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val pausingDispatchQueue: PausingDispatchQueue = PausingDispatchQueue()
    private val workersScope: CoroutineScope = CoroutineScope(
        context = PausingDispatcher(
            queue = pausingDispatchQueue,
            baseDispatcher = workerDispatcher
        )
    )
    private val keyboardHook: GlobalKeyboardHook = GlobalKeyboardHook(true)
    private val globalKeyAdapter: GlobalKeyAdapter = object : GlobalKeyAdapter() {
        override fun keyPressed(event: GlobalKeyEvent) {
            when (event.virtualKeyCode) {
                GlobalKeyEvent.VK_Q -> workersScope.cancel()
                GlobalKeyEvent.VK_P -> pauseManager()
            }
        }
    }

    fun exe(parameter: Int, worker: Worker) = managerScope.launch(CoroutineName("manager")) {
        addCancelListener()

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
        finishCancelListener()
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

    private fun pauseManager() {
        val pauseTimeout = 5_000L
        pausingDispatchQueue.pause()
        removeCancelListener()
        managerScope.launch(CoroutineName("manager-timeout")) {
            val shouldCancel = withForceTimeoutOrNull(pauseTimeout) {
                println()
                println("Timed out waiting for $pauseTimeout ms.")
                print("Please, confirm that the computation should be stopped (y): ")
                readlnOrNull()?.equals("y") ?: false
            }

            when (shouldCancel) {
                true -> workersScope.cancel()
                false -> {
                    println("Proceeding the program...")
                    addCancelListener()
                    pausingDispatchQueue.resume()
                }
                null -> {
                    println()
                    println("The action is not confirmed within 5 seconds.")
                    println("Proceeding the program...")
                    addCancelListener()
                    pausingDispatchQueue.resume()
                }
            }
        }
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

    private fun finishCancelListener() {
        keyboardHook.shutdownHook()
    }
}