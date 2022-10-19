package worker

import kotlinx.coroutines.TimeoutCancellationException
import java.util.NoSuchElementException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException

sealed interface WorkerResult {
    data class Success(val value: String) : WorkerResult
    data class SoftFailure(val cause: Exception) : WorkerResult
    data class HardFailure(val cause: Exception) : WorkerResult
}

fun Exception.toResult(): WorkerResult {
    return when (this) {
        is ExecutionException -> WorkerResult.HardFailure(cause = this)
        is InterruptedException -> WorkerResult.SoftFailure(cause = this)
        is TimeoutException -> WorkerResult.SoftFailure(cause = this)
        is NoSuchElementException -> WorkerResult.SoftFailure(cause = this)
        is TimeoutCancellationException -> WorkerResult.SoftFailure(cause = this)

        else -> WorkerResult.HardFailure(cause = this)
    }
}