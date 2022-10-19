package util

sealed interface CalculationResult {
    data class Success(val fValue: String, val gValue: String) : CalculationResult
    data class Failure(val cause: Exception) : CalculationResult
}