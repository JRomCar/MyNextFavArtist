package com.jrom.mynextfavartist

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application(), SingletonImageLoader.Factory {

    // Cover Art Archive has no rate limit and needs no User-Agent (confirmed against its
    // API docs), so this uses a plain OkHttp engine rather than the MusicBrainz-specific
    // client (UserAgentInterceptor/RateLimitInterceptor) provided via Hilt for the API layer.
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory()) }
            .crossfade(true)
            .build()
    }
}
