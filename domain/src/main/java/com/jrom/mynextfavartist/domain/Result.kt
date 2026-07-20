package com.jrom.mynextfavartist.domain

import com.jrom.mynextfavartist.domain.error.Error

sealed interface Result<out D, out E : Error> {
    val isSuccess: Boolean get() = this is Success<*, *>
    val isFailure: Boolean get() = this is Failure<*, *>

    data class Success<out D, out E : Error>(val data: D) : Result<D, E>
    data class Failure<out D, out E : Error>(val error: E) : Result<D, E>
}

typealias EmptyResult<E> = Result<Unit, E>

inline val <D, E : Error> Result<D, E>.dataOrNull: D?
    get() = when (this) {
        is Result.Success -> this.data
        is Result.Failure -> null
    }

inline val <D, E : Error> Result<D, E>.errorOrNull: E?
    get() = when (this) {
        is Result.Success -> null
        is Result.Failure -> this.error
    }

inline fun <D, E : Error, R> Result<D, E>.map(transform: (D) -> R): Result<R, E> =
    when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Failure -> Result.Failure(error)
    }

fun <D, E : Error> Result<D, E>.asEmptyDataResult(): EmptyResult<E> = map {}

inline fun <D, E : Error, R> Result<D, E>.fold(onSuccess: (D) -> R, onFailure: (E) -> R): R =
    when (this) {
        is Result.Success -> onSuccess(data)
        is Result.Failure -> onFailure(error)
    }

inline fun <D, E : Error> Result<D, E>.onSuccess(action: (D) -> Unit): Result<D, E> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <D, E : Error> Result<D, E>.onFailure(action: (E) -> Unit): Result<D, E> {
    if (this is Result.Failure) action(error)
    return this
}
