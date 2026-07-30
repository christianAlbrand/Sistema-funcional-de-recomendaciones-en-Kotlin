package com.example.sistemafuncionalderecomendacionesenkotlin.result

sealed class AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>()
    data class Failure(val errors: List<String>) : AppResult<Nothing>()
}

fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(value))
    is AppResult.Failure -> this
}

fun <T, R> AppResult<T>.flatMap(transform: (T) -> AppResult<R>): AppResult<R> = when (this) {
    is AppResult.Success -> transform(value)
    is AppResult.Failure -> this
}

fun <T, R> AppResult<T>.fold(
    onSuccess: (T) -> R,
    onFailure: (List<String>) -> R
): R = when (this) {
    is AppResult.Success -> onSuccess(value)
    is AppResult.Failure -> onFailure(errors)
}

fun <T> AppResult<T>.getOrElse(defaultValue: () -> T): T = when (this) {
    is AppResult.Success -> value
    is AppResult.Failure -> defaultValue()
}