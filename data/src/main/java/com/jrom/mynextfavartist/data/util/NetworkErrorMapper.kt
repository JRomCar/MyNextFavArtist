package com.jrom.mynextfavartist.data.util

import com.jrom.mynextfavartist.domain.error.DataError
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun mapThrowableToNetworkError(throwable: Throwable): DataError.Network {
    return when (throwable) {
        is HttpException -> when (throwable.code()) {
            HttpURLConnection.HTTP_BAD_REQUEST -> DataError.Network.BAD_REQUEST
            HttpURLConnection.HTTP_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN -> DataError.Network.UNAUTHORIZED
            HttpURLConnection.HTTP_NOT_FOUND -> DataError.Network.NOT_FOUND
            // MusicBrainz returns 503 (and sometimes 429) when a client exceeds the
            // 1 req/sec limit, not just for generic server overload.
            429, HttpURLConnection.HTTP_UNAVAILABLE -> DataError.Network.RATE_LIMITED
            HttpURLConnection.HTTP_CLIENT_TIMEOUT, HttpURLConnection.HTTP_GATEWAY_TIMEOUT -> DataError.Network.TIMEOUT
            HttpURLConnection.HTTP_INTERNAL_ERROR, HttpURLConnection.HTTP_BAD_GATEWAY -> DataError.Network.SERVER_ERROR
            else -> DataError.Network.UNKNOWN
        }
        // Connectivity failures (no network, unknown host, read timeout) never reach the
        // HttpException branch, so they must be handled here to avoid a crash.
        is SocketTimeoutException -> DataError.Network.TIMEOUT
        // Thrown when there's no route to the host at all - no DNS resolution (offline, or the
        // host doesn't exist) or the connection was refused - as opposed to a request that
        // reached the server but failed there.
        is UnknownHostException, is ConnectException -> DataError.Network.NO_INTERNET
        is IOException -> DataError.Network.UNKNOWN
        else -> DataError.Network.UNKNOWN
    }
}
