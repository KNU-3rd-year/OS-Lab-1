import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import manager.Manager
import util.withForceTimeoutOrNull
import worker.advanced.AdvancedConcatenation
import kotlin.system.exitProcess

fun main() {
    val manager = Manager()
    runBlocking {
        manager.exe(
            parameter = getParameter(),
            worker = AdvancedConcatenation()
        ).join()
    }
    startAgain()
}

private fun getParameter(): Int {
    printDetails()
    return realIntArg()
}

private fun printDetails() {
    println("You will be asked to enter the \'x\' parameter as the input parameter to this program.")
    println("You may enter many numbers or words, but the first int-type value would be accepted.")
    println("If there will be no int-type value typed then you will be given one more try, but not more than 2 times.")
    println()
}

private fun realIntArg(tries: Int = 0): Int {
    if (tries >= 3) {
        println("\nWe am sorry, but the input is still not correct. The program will be stopped.")
        exitProcess(1)
    } else if (tries > 0) {
        println(" Please, try one more time.")
    }

    print("Please, enter the x parameter: ")
    val x = readlnOrNull()
        ?.split(' ')
        ?.mapNotNull { it.toIntOrNull() }
        ?.firstOrNull()
        ?: run {
            print("Ooops, seems like your input is not correct.")
            realIntArg(tries = tries + 1)
        }

    println("Your input is $x")
    return x
}

fun startAgain() {
    val timeout = 4_000L
    print("\nDo you want to try again with another parameter? To proceed enter \"y\": ")

    val responce = util.runBlocking(context = Dispatchers.IO) {
        withForceTimeoutOrNull(timeout) {
            readlnOrNull()?.equals("y")
        } ?: run {
            println("\nNo answer in $timeout ms.")
            println("The answer is thought to be \"no\"")
            false
        }
    }

    if (responce) {
        println("Let's start again!\n")
        main()
    } else {
        println("See you next time!")
    }
}