package com.jrom.mynextfavartist.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.jrom.mynextfavartist.domain.network.NetworkMonitor
import com.jrom.mynextfavartist.domain.network.NetworkState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class NetworkMonitorImpl(
    appContext: Context
) : NetworkMonitor {

    private val connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override val networkState: Flow<NetworkState> = callbackFlow {
        // Local to this collection, not a class field - callbackFlow's producer block re-runs
        // per collector, and each collector needs its own "was offline" history rather than
        // racing with every other collector's callback over a shared field.
        var lastKnownStateWasOffline = isNetworkAvailable().not()

        fun createNetworkStatus(isOnline: Boolean): NetworkState {
            val shouldRefresh = lastKnownStateWasOffline && isOnline
            return NetworkState(isOnline = isOnline, shouldRefresh = shouldRefresh)
        }

        // trySend is non-suspending and runs synchronously on the callback thread, so it
        // preserves the order ConnectivityManager delivers onAvailable/onLost in - unlike
        // launch { send(...) }, which schedules a separate coroutine per event with no
        // ordering guarantee between them.
        trySend(createNetworkStatus(isNetworkAvailable()))

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                super.onLost(network)
                trySend(createNetworkStatus(false))
                lastKnownStateWasOffline = true
            }

            // onAvailable fires as soon as a network is selected, before it's confirmed to
            // actually have internet access (e.g. a captive-portal Wi-Fi). onCapabilitiesChanged
            // is the callback that reports the validated capabilities, so it's the accurate
            // "is this network actually usable" signal - it fires once with the initial
            // capabilities right after onAvailable, and again on every later change.
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, capabilities)
                val isOnline = capabilities.hasInternet()
                trySend(createNetworkStatus(isOnline))
                lastKnownStateWasOffline = !isOnline
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()

    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        return connectivityManager.getNetworkCapabilities(network)?.hasInternet() ?: false
    }

    private fun NetworkCapabilities.hasInternet(): Boolean =
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
