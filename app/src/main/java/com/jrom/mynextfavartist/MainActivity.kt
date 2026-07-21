package com.jrom.mynextfavartist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jrom.mynextfavartist.domain.network.NetworkMonitor
import com.jrom.mynextfavartist.ui.MyNextFavArtistApp
import com.jrom.mynextfavartist.ui.theme.MyNextFavArtistTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        setContent {
            MyNextFavArtistTheme {
                val isOnline by networkMonitor.networkState.collectAsStateWithLifecycle(null)

                MyNextFavArtistApp(isOffline = isOnline == false)
            }
        }
    }
}
