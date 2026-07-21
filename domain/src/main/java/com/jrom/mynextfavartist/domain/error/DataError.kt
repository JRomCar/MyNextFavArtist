package com.jrom.mynextfavartist.domain.error

/**
 * The closed set of failures a data source can actually produce, split by where they come from
 * ([Network] vs [Local]) so a repository never has to translate a raw [java.io.IOException] or
 * SQL exception itself - see [com.jrom.mynextfavartist.data.repository.ArtistRepositoryImpl]
 * and its data sources for where these are mapped. Being an enum-backed sealed interface (not
 * a generic Exception) means the UI layer's `when` over a failure is exhaustive - see
 * [com.jrom.mynextfavartist.ui.error.asUiText] and
 * [com.jrom.mynextfavartist.ui.error.asUiIcon].
 */
sealed interface DataError : Error {
    enum class Network : DataError {
        BAD_REQUEST,
        UNAUTHORIZED,
        NOT_FOUND,
        RATE_LIMITED,
        TIMEOUT,
        SERVER_ERROR,
        NO_INTERNET,
        UNKNOWN
    }
    enum class Local : DataError {
        DB_READ_ERROR,
        DB_WRITE_ERROR
    }
}
