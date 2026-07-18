package com.jrom.mynextfavartist.data.api.interceptor

import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertTrue
import org.junit.Test

class RateLimitInterceptorTest {

    private val chain = object : Interceptor.Chain {
        private val request = Request.Builder().url("https://musicbrainz.org/ws/2/artist").build()

        override fun request(): Request = request

        override fun proceed(request: Request): Response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .build()

        override fun connection() = null
        override fun call() = throw UnsupportedOperationException()
        override fun connectTimeoutMillis() = 0
        override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
        override fun readTimeoutMillis() = 0
        override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
        override fun writeTimeoutMillis() = 0
        override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
    }

    @Test
    fun `consecutive requests are spaced at least minIntervalMs apart`() {
        val interceptor = RateLimitInterceptor(minIntervalMs = 200L)

        val start = System.currentTimeMillis()
        interceptor.intercept(chain)
        interceptor.intercept(chain)
        val elapsed = System.currentTimeMillis() - start

        assertTrue("expected at least 200ms between requests, was ${elapsed}ms", elapsed >= 200L)
    }

    @Test
    fun `does not wait before the first request`() {
        val interceptor = RateLimitInterceptor(minIntervalMs = 5_000L)

        val start = System.currentTimeMillis()
        interceptor.intercept(chain)
        val elapsed = System.currentTimeMillis() - start

        assertTrue("first request should not wait, took ${elapsed}ms", elapsed < 1_000L)
    }
}
