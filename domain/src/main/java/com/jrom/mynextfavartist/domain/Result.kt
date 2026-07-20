package com.jrom.mynextfavartist.domain

import com.jrom.mynextfavartist.domain.error.Error

typealias RootError = Error

sealed interface Result<out D, out E : RootError> {
    val isSuccess: Boolean
    val isFailure: Boolean
    data class Success<out D, out E : RootError>(val data: D) : Result<D, E> {
        override val isSuccess = true
        override val isFailure = false
    }
    data class Error<out D, out E : RootError>(val error: E) : Result<D, E> {
        override val isSuccess = false
        override val isFailure = true
    }
}

typealias EmptyResult<E> = Result<Unit, E>

inline val <D, E : RootError> Result<D, E>.dataOrNull: D?
    get() = when (this) {
        is Result.Success -> this.data
        is Result.Error -> null
    }

inline val <D, E : RootError> Result<D, E>.errorOrNull: E?
    get() = when (this) {
        is Result.Success -> null
        is Result.Error -> this.error
    }
