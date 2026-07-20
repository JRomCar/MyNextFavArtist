package com.jrom.mynextfavartist.di

import com.jrom.mynextfavartist.data.BuildConfig
import com.jrom.mynextfavartist.data.api.MusicBrainzApi
import com.jrom.mynextfavartist.data.api.interceptor.RateLimitInterceptor
import com.jrom.mynextfavartist.data.api.interceptor.RetryInterceptor
import com.jrom.mynextfavartist.data.api.interceptor.UserAgentInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(UserAgentInterceptor("MyNextFavArtist/1.0 (${BuildConfig.MUSICBRAINZ_CONTACT})"))
        // RetryInterceptor must come before RateLimitInterceptor so each retried attempt -
        // not just the first one - is paced by the rate limiter too.
        .addInterceptor(RetryInterceptor())
        .addInterceptor(RateLimitInterceptor())
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
            .addConverterFactory(GsonConverterFactory.create())
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
