package com.jrom.mynextfavartist.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.jrom.mynextfavartist.testutils.TestBase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkMonitorImplTest : TestBase() {

    private val context: Context = mock()
    private val connectivityManager: ConnectivityManager = mock()
    private val network: Network = mock()
    private val onlineCapabilities: NetworkCapabilities = mock()

    private lateinit var sut: NetworkMonitorImpl

    @Before
    fun setUp() {
        whenever(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager)
        whenever(onlineCapabilities.hasCapability(any())).thenReturn(true)
        sut = NetworkMonitorImpl(context)
    }

    private fun captureNetworkCallback(): ConnectivityManager.NetworkCallback {
        val captor = argumentCaptor<ConnectivityManager.NetworkCallback>()
        verify(connectivityManager).registerDefaultNetworkCallback(captor.capture())
        return captor.firstValue
    }

    @Test
    fun `initial emission reports online when a validated network is active`() = runUnconfinedTest {
        whenever(connectivityManager.activeNetwork).thenReturn(network)
        whenever(connectivityManager.getNetworkCapabilities(network)).thenReturn(onlineCapabilities)

        val emissions = mutableListOf<Boolean>()
        val job = launch(unconfinedTestDispatcher) { sut.networkState.collect { emissions.add(it) } }
        advanceUntilIdle()

        assertEquals(listOf(true), emissions)
        job.cancel()
    }

    @Test
    fun `initial emission reports offline when there is no active network`() = runUnconfinedTest {
        whenever(connectivityManager.activeNetwork).thenReturn(null)

        val emissions = mutableListOf<Boolean>()
        val job = launch(unconfinedTestDispatcher) { sut.networkState.collect { emissions.add(it) } }
        advanceUntilIdle()

        assertEquals(listOf(false), emissions)
        job.cancel()
    }

    @Test
    fun `onLost emits offline`() = runUnconfinedTest {
        whenever(connectivityManager.activeNetwork).thenReturn(network)
        whenever(connectivityManager.getNetworkCapabilities(network)).thenReturn(onlineCapabilities)

        val emissions = mutableListOf<Boolean>()
        val job = launch(unconfinedTestDispatcher) { sut.networkState.collect { emissions.add(it) } }
        advanceUntilIdle()

        captureNetworkCallback().onLost(network)
        advanceUntilIdle()

        assertEquals(listOf(true, false), emissions)
        job.cancel()
    }

    @Test
    fun `onCapabilitiesChanged reports the validated online state`() = runUnconfinedTest {
        whenever(connectivityManager.activeNetwork).thenReturn(null)

        val emissions = mutableListOf<Boolean>()
        val job = launch(unconfinedTestDispatcher) { sut.networkState.collect { emissions.add(it) } }
        advanceUntilIdle()

        captureNetworkCallback().onCapabilitiesChanged(network, onlineCapabilities)
        advanceUntilIdle()

        assertEquals(listOf(false, true), emissions)
        job.cancel()
    }

    @Test
    fun `distinctUntilChanged suppresses duplicate consecutive states`() = runUnconfinedTest {
        whenever(connectivityManager.activeNetwork).thenReturn(network)
        whenever(connectivityManager.getNetworkCapabilities(network)).thenReturn(onlineCapabilities)

        val emissions = mutableListOf<Boolean>()
        val job = launch(unconfinedTestDispatcher) { sut.networkState.collect { emissions.add(it) } }
        advanceUntilIdle()

        // Same online state as the initial emission - should be deduplicated, not re-emitted.
        captureNetworkCallback().onCapabilitiesChanged(network, onlineCapabilities)
        advanceUntilIdle()

        assertEquals(listOf(true), emissions)
        job.cancel()
    }
}
