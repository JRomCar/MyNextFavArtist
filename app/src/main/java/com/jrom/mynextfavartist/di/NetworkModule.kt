package com.jrom.mynextfavartist.di

import android.content.Context
import com.jrom.mynextfavartist.BuildConfig as AppBuildConfig
import com.jrom.mynextfavartist.data.BuildConfig
import com.jrom.mynextfavartist.data.api.MusicBrainzApi
import com.jrom.mynextfavartist.data.api.interceptor.RateLimitInterceptor
import com.jrom.mynextfavartist.data.api.interceptor.RetryInterceptor
import com.jrom.mynextfavartist.data.api.interceptor.UserAgentInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

private const val HTTP_CACHE_SIZE_BYTES = 5L * 1024 * 1024

// MusicBrainz responses carry fields this app doesn't model (e.g. relations, tags when
// requested); ignoreUnknownKeys keeps adding an unmapped field from being a crash.
private val musicBrainzJson = Json { ignoreUnknownKeys = true }

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            UserAgentInterceptor(
                "MyNextFavArtist/${AppBuildConfig.VERSION_NAME} (${BuildConfig.MUSICBRAINZ_CONTACT})"
            )
        )
        // RetryInterceptor must come before RateLimitInterceptor so each retried attempt -
        // not just the first one - is paced by the rate limiter too.
        .addInterceptor(RetryInterceptor())
        .addInterceptor(RateLimitInterceptor())
        .connectTimeout(10.seconds)
        .readTimeout(10.seconds)
        .writeTimeout(10.seconds)
        // callTimeout bounds the whole call, including time spent blocked inside
        // RateLimitInterceptor/RetryInterceptor. Worst case is ~3 attempts at up to
        // (1s rate-limit wait + 10s read) each, plus ~2s of retry backoff between them -
        // comfortably under 45s.
        .callTimeout(45.seconds)
        .cache(Cache(File(context.cacheDir, "http_cache"), HTTP_CACHE_SIZE_BYTES))
        .apply {
            // Only log HTTP traffic in debug builds.
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)
                )
            }
        }
        .build()

    @Singleton
    @Provides
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(musicBrainzJson.asConverterFactory("application/json".toMediaType()))
            .client(client)
            .baseUrl(BuildConfig.MUSICBRAINZ_BASE_URL)
            .build()
    }

    @Singleton
    @Provides
    fun provideMusicBrainzApi(retrofit: Retrofit): MusicBrainzApi {
        return retrofit.create(MusicBrainzApi::class.java)
    }
}
