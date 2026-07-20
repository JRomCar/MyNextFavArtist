package com.jrom.mynextfavartist.domain.network

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    val networkState: Flow<Boolean>
}
