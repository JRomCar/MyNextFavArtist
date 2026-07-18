package com.jrom.mynextfavartist.domain.error

sealed interface DataError : Error {
    enum class Network : DataError {
        BAD_REQUEST,
        NOT_FOUND,
        RATE_LIMITED,
        TIMEOUT,
        SERVER_ERROR,
        UNKNOWN
    }
    enum class Local : DataError {
        DB_READ_ERROR,
        DB_WRITE_ERROR
    }
}
