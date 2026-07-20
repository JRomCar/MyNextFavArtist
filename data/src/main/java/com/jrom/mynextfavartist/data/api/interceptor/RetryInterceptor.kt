package com.jrom.mynextfavartist.data.api.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import kotlin.random.Random

private const val MAX_ATTEMPTS = 3
private const val INITIAL_BACKOFF_MS = 500L
private const val BACKOFF_FACTOR = 2.0

// +/-20% randomization so multiple clients backing off from the same rate-limit window don't
// all retry in lockstep.
private const val JITTER_RATIO = 0.2

private val TRANSIENT_HTTP_CODES = setOf(408, 429, 500, 502, 503, 504)

/**
 * MusicBrainz occasionally resets connections mid-handshake (SSLHandshakeException) or returns
 * 429/503 under its rate limiter - both self-heal on retry. Runs as an interceptor - registered
 * before [RateLimitInterceptor], so every retried attempt is still paced by it - rather than
 * being called per-call-site, so any new endpoint gets retry behavior automatically. Honors a
 * Retry-After response header when the server sends one; otherwise backs off with jitter.
 */
class RetryInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var backoffMs = INITIAL_BACKOFF_MS
        repeat(MAX_ATTEMPTS - 1) {
            val response = try {
                chain.proceed(request)
            } catch (e: IOException) {
                null
            }
            if (response != null && !response.isTransientFailure()) return response
            val waitMs = response?.retryAfterMillis() ?: backoffMs.withJitter()
            response?.close()
            Thread.sleep(waitMs)
            backoffMs = (backoffMs * BACKOFF_FACTOR).toLong()
        }
        return chain.proceed(request)
    }

    private fun Response.isTransientFailure(): Boolean = code in TRANSIENT_HTTP_CODES

    // MusicBrainz sends Retry-After as delay-seconds, not an HTTP-date.
    private fun Response.retryAfterMillis(): Long? =
        header("Retry-After")?.toLongOrNull()?.times(1000)

    private fun Long.withJitter(): Long = this + (this * JITTER_RATIO * Random.nextDouble()).toLong()
}
