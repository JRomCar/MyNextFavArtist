package com.jrom.mynextfavartist.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
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
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                trySend(createNetworkStatus(true))
                lastKnownStateWasOffline = false
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                trySend(createNetworkStatus(false))
                lastKnownStateWasOffline = true
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()

    fun isNetworkAvailable(): Boolean = connectivityManager.activeNetwork != null
}
