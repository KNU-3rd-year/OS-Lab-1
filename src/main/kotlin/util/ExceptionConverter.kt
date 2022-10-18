package util

import kotlinx.coroutines.TimeoutCancellationException
import java.util.NoSuchElementException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException

fun Exception.toResult(): Result {
    return when (this) {
        is ExecutionException -> Result.HardFailure(cause = this)
        is InterruptedException -> Result.SoftFailure(cause = this)
        is TimeoutException -> Result.SoftFailure(cause = this)
        is NoSuchElementException -> Result.SoftFailure(cause = this)
        is TimeoutCancellationException -> Result.SoftFailure(cause = this)

        else -> Result.HardFailure(cause = this)
    }
}