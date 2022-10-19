package manager.controller

sealed interface ControllerResult {
    data class Success(val value: String) : ControllerResult
    data class Failure(val cause: Exception) : ControllerResult
}