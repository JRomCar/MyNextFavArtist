package com.jrom.mynextfavartist

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jrom.mynextfavartist.domain.network.NetworkMonitor
import com.jrom.mynextfavartist.ui.components.NoConnectionBanner
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            MyNextFavArtistTheme {
                val networkStatus by networkMonitor.networkState.collectAsStateWithLifecycle(null)

                Column(modifier = Modifier.fillMaxSize()) {
                    if (networkStatus?.isOnline == false) {
                        NoConnectionBanner()
                    }
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Replaced with MyNextFavArtistApp() once Home/Search/Favorites/
                        // Details screens and the nav graph land.
                        Text("MyNextFavArtist")
                    }
                }
            }
        }
    }
}
