package com.jrom.mynextfavartist.data.api.interceptor

import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class RetryInterceptorTest {

    private val request = Request.Builder().url("https://musicbrainz.org/ws/2/artist").build()

    private fun responseOf(code: Int, retryAfterSeconds: Long? = null): Response =
        Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("")
            .apply { retryAfterSeconds?.let { header("Retry-After", it.toString()) } }
            .build()

    private class FakeChain(private val request: Request, private val steps: List<() -> Response>) : Interceptor.Chain {
        var callCount = 0
            private set

        override fun request(): Request = request

        override fun proceed(request: Request): Response {
            val step = steps[callCount]
            callCount++
            return step()
        }

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
    fun `succeeds immediately without retrying on a 200`() {
        val chain = FakeChain(request, listOf({ responseOf(200) }))

        val response = RetryInterceptor().intercept(chain)

        assertEquals(200, response.code)
        assertEquals(1, chain.callCount)
    }

    @Test
    fun `retries a transient 503 and returns the eventual success`() {
        val chain = FakeChain(
            request,
            listOf({ responseOf(503) }, { responseOf(200) }),
        )

        val response = RetryInterceptor().intercept(chain)

        assertEquals(200, response.code)
        assertEquals(2, chain.callCount)
    }

    @Test
    fun `does not retry a non-transient 400`() {
        val chain = FakeChain(request, listOf({ responseOf(400) }))

        val response = RetryInterceptor().intercept(chain)

        assertEquals(400, response.code)
        assertEquals(1, chain.callCount)
    }

    @Test
    fun `retries a connection failure and returns the eventual success`() {
        var attempt = 0
        val chain = FakeChain(
            request,
            listOf(
                { attempt++; throw IOException("connection reset") },
                { responseOf(200) },
            ),
        )

        val response = RetryInterceptor().intercept(chain)

        assertEquals(200, response.code)
        assertEquals(1, attempt)
        assertEquals(2, chain.callCount)
    }

    @Test
    fun `gives up after the max attempts and returns the last transient response`() {
        val chain = FakeChain(
            request,
            listOf({ responseOf(503) }, { responseOf(503) }, { responseOf(503) }, { responseOf(200) }),
        )

        val response = RetryInterceptor().intercept(chain)

        assertEquals(503, response.code)
        assertEquals(3, chain.callCount)
    }

    @Test
    fun `honors the Retry-After header instead of the computed backoff`() {
        val chain = FakeChain(
            request,
            listOf({ responseOf(503, retryAfterSeconds = 0) }, { responseOf(200) }),
        )

        val start = System.currentTimeMillis()
        val response = RetryInterceptor().intercept(chain)
        val elapsed = System.currentTimeMillis() - start

        assertEquals(200, response.code)
        assertTrue("expected a near-instant retry honoring Retry-After: 0, took ${elapsed}ms", elapsed < 400L)
    }
}
