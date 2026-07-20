package com.jrom.mynextfavartist.data.api.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * MusicBrainz allows at most 1 request/second per client and will start blocking IPs that
 * exceed it. OkHttp interceptors run on a background dispatcher thread and are expected to
 * be synchronous, so blocking with [Thread.sleep] here is the standard approach - it only
 * ever stalls the request thread, never the caller's coroutine dispatcher.
 */
class RateLimitInterceptor(private val minIntervalMs: Long = 1_000L) : Interceptor {
    private val lock = Any()
    // nanoTime is monotonic and only meaningful for measuring elapsed time, unlike
    // currentTimeMillis, which can jump backward on an NTP correction or manual clock
    // change and stall every request until wall-clock time catches back up.
    private var lastRequestAtNanos = 0L

    override fun intercept(chain: Interceptor.Chain): Response {
        synchronized(lock) {
            val elapsedMs = (System.nanoTime() - lastRequestAtNanos) / 1_000_000
            val waitMs = minIntervalMs - elapsedMs
            if (waitMs > 0) {
                Thread.sleep(waitMs)
            }
            lastRequestAtNanos = System.nanoTime()
        }
        return chain.proceed(chain.request())
    }
}
