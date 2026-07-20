package com.jrom.mynextfavartist.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.jrom.mynextfavartist.domain.network.NetworkState
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
import org.mockito.kotlin.times
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

        val emissions = mutableListOf<NetworkState>()
        val job = launch(unconfinedTestDispatcher) { sut.networkState.collect { emissions.add(it) } }
        advanceUntilIdle()

        assertEquals(listOf(NetworkState(isOnline = true, shouldRefresh = false)), emissions)
        job.cancel()
    }

    @Test
    fun `initial emission reports offline when there is no active network`() = runUnconfinedTest {
        whenever(connectivityManager.activeNetwork).thenReturn(null)

        val emissions = mutableListOf<NetworkState>()
        val job = launch(unconfinedTestDispatcher) { sut.networkState.collect { emissions.add(it) } }
        advanceUntilIdle()

        assertEquals(listOf(NetworkState(isOnline = false, shouldRefresh = false)), emissions)
        job.cancel()
    }

    @Test
    fun `onLost emits offline without requesting a refresh`() = runUnconfinedTest {
        whenever(connectivityManager.activeNetwork).thenReturn(network)
        whenever(connectivityManager.getNetworkCapabilities(network)).thenReturn(onlineCapabilities)

        val emissions = mutableListOf<NetworkState>()
        val job = launch(unconfinedTestDispatcher) { sut.networkState.collect { emissions.add(it) } }
        advanceUntilIdle()

        captureNetworkCallback().onLost(network)
        advanceUntilIdle()

        assertEquals(
            listOf(
                NetworkState(isOnline = true, shouldRefresh = false),
                NetworkState(isOnline = false, shouldRefresh = false),
            ),
            emissions,
        )
        job.cancel()
    }

    @Test
    fun `onCapabilitiesChanged transitioning from offline to online requests a refresh`() = runUnconfinedTest {
        whenever(connectivityManager.activeNetwork).thenReturn(null)

        val emissions = mutableListOf<NetworkState>()
        val job = launch(unconfinedTestDispatcher) { sut.networkState.collect { emissions.add(it) } }
        advanceUntilIdle()

        captureNetworkCallback().onCapabilitiesChanged(network, onlineCapabilities)
        advanceUntilIdle()

        assertEquals(
            listOf(
                NetworkState(isOnline = false, shouldRefresh = false),
                NetworkState(isOnline = true, shouldRefresh = true),
            ),
            emissions,
        )
        job.cancel()
    }

    @Test
    fun `distinctUntilChanged suppresses duplicate consecutive states`() = runUnconfinedTest {
        whenever(connectivityManager.activeNetwork).thenReturn(network)
        whenever(connectivityManager.getNetworkCapabilities(network)).thenReturn(onlineCapabilities)

        val emissions = mutableListOf<NetworkState>()
        val job = launch(unconfinedTestDispatcher) { sut.networkState.collect { emissions.add(it) } }
        advanceUntilIdle()

        // Same online state as the initial emission - should be deduplicated, not re-emitted.
        captureNetworkCallback().onCapabilitiesChanged(network, onlineCapabilities)
        advanceUntilIdle()

        assertEquals(listOf(NetworkState(isOnline = true, shouldRefresh = false)), emissions)
        job.cancel()
    }

    @Test
    fun `two concurrent collectors track their own offline history independently`() = runUnconfinedTest {
        whenever(connectivityManager.activeNetwork).thenReturn(null)

        val emissions1 = mutableListOf<NetworkState>()
        val emissions2 = mutableListOf<NetworkState>()
        val job1 = launch(unconfinedTestDispatcher) { sut.networkState.collect { emissions1.add(it) } }
        advanceUntilIdle()
        val job2 = launch(unconfinedTestDispatcher) { sut.networkState.collect { emissions2.add(it) } }
        advanceUntilIdle()

        val captor = argumentCaptor<ConnectivityManager.NetworkCallback>()
        verify(connectivityManager, times(2)).registerDefaultNetworkCallback(captor.capture())
        val (callback1, callback2) = captor.allValues

        // If "was offline" were a shared field instead of per-collection state, only the first
        // callback to fire would see shouldRefresh = true, and the second would incorrectly see
        // false because the first already flipped the shared flag.
        callback1.onCapabilitiesChanged(network, onlineCapabilities)
        advanceUntilIdle()
        callback2.onCapabilitiesChanged(network, onlineCapabilities)
        advanceUntilIdle()

        assertEquals(NetworkState(isOnline = true, shouldRefresh = true), emissions1.last())
        assertEquals(NetworkState(isOnline = true, shouldRefresh = true), emissions2.last())

        job1.cancel()
        job2.cancel()
    }
}
