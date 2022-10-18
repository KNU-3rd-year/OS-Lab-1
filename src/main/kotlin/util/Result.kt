package util

sealed interface Result {
    data class Success(val value: String) : Result
    data class SoftFailure(val cause: Any?) : Result
    data class HardFailure(val cause: Any?) : Result
}