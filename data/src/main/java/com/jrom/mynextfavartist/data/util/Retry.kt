package com.jrom.mynextfavartist.data.util

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds

private const val MAX_ATTEMPTS = 3
private const val INITIAL_BACKOFF_MS = 500L
private const val BACKOFF_FACTOR = 2.0

/**
 * MusicBrainz occasionally resets connections mid-handshake (SSLHandshakeException) or
 * returns 429/503 under its rate limiter - both self-heal on retry. Runs [block] up to
 * [MAX_ATTEMPTS] times with exponential backoff, retrying only on failures that are likely
 * transient; anything else (4xx, malformed responses) is rethrown immediately.
 */
suspend fun <T> retryOnTransientFailure(block: suspend () -> T): T {
    var backoffMs = INITIAL_BACKOFF_MS
    repeat(MAX_ATTEMPTS - 1) {
        try {
            return block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (!e.isTransient()) throw e
        }
        delay(backoffMs.milliseconds)
        backoffMs = (backoffMs * BACKOFF_FACTOR).toLong()
    }
    return block()
}

private fun Exception.isTransient(): Boolean = when (this) {
    is HttpException -> code() in TRANSIENT_HTTP_CODES
    // Covers SSLHandshakeException, SocketTimeoutException, ConnectException,
    // UnknownHostException and other connection-level failures.
    is IOException -> true
    else -> false
}

private val TRANSIENT_HTTP_CODES = setOf(408, 429, 500, 502, 503, 504)
