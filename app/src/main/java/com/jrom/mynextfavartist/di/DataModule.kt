package com.jrom.mynextfavartist.di

import android.content.Context
import com.jrom.mynextfavartist.data.api.MusicBrainzApi
import com.jrom.mynextfavartist.data.db.ArtistDao
import com.jrom.mynextfavartist.data.network.NetworkMonitorImpl
import com.jrom.mynextfavartist.data.repository.ArtistDataSource
import com.jrom.mynextfavartist.data.repository.ArtistLocalDataSource
import com.jrom.mynextfavartist.data.repository.ArtistRemoteDataSource
import com.jrom.mynextfavartist.data.repository.ArtistRepositoryImpl
import com.jrom.mynextfavartist.domain.di.IoDispatcher
import com.jrom.mynextfavartist.domain.network.NetworkMonitor
import com.jrom.mynextfavartist.domain.repository.ArtistRepository
import com.jrom.mynextfavartist.domain.usecase.CheckIfArtistIsFavorite
import com.jrom.mynextfavartist.domain.usecase.GetAllFavoriteArtists
import com.jrom.mynextfavartist.domain.usecase.GetArtistReleaseGroups
import com.jrom.mynextfavartist.domain.usecase.GetHomeArtists
import com.jrom.mynextfavartist.domain.usecase.RemoveAllFavoriteArtists
import com.jrom.mynextfavartist.domain.usecase.RemoveFavoriteArtist
import com.jrom.mynextfavartist.domain.usecase.SaveFavoriteArtist
import com.jrom.mynextfavartist.domain.usecase.SearchArtists
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

    @Provides
    @Singleton
    fun provideArtistRemoteDataSource(
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
        musicBrainzApi: MusicBrainzApi,
    ): ArtistDataSource.Remote {
        return ArtistRemoteDataSource(ioDispatcher, musicBrainzApi)
    }

    @Provides
    @Singleton
    fun provideArtistLocalDataSource(artistDao: ArtistDao): ArtistDataSource.Local {
        return ArtistLocalDataSource(artistDao)
    }

    @Provides
    @Singleton
    fun provideArtistRepository(
        remote: ArtistDataSource.Remote,
        local: ArtistDataSource.Local,
    ): ArtistRepository {
        return ArtistRepositoryImpl(remote, local)
    }

    @Provides
    @Singleton
    fun provideSearchArtistsUseCase(artistRepository: ArtistRepository): SearchArtists {
        return SearchArtists(artistRepository)
    }

    @Provides
    @Singleton
    fun provideGetHomeArtistsUseCase(artistRepository: ArtistRepository): GetHomeArtists {
        return GetHomeArtists(artistRepository)
    }

    @Provides
    @Singleton
    fun provideGetArtistReleaseGroupsUseCase(artistRepository: ArtistRepository): GetArtistReleaseGroups {
        return GetArtistReleaseGroups(artistRepository)
    }

    @Provides
    @Singleton
    fun provideGetAllFavoriteArtistsUseCase(artistRepository: ArtistRepository): GetAllFavoriteArtists {
        return GetAllFavoriteArtists(artistRepository)
    }

    @Provides
    @Singleton
    fun provideSaveFavoriteArtistUseCase(artistRepository: ArtistRepository): SaveFavoriteArtist {
        return SaveFavoriteArtist(artistRepository)
    }

    @Provides
    @Singleton
    fun provideCheckIfArtistIsFavoriteUseCase(artistRepository: ArtistRepository): CheckIfArtistIsFavorite {
        return CheckIfArtistIsFavorite(artistRepository)
    }

    @Provides
    @Singleton
    fun provideRemoveFavoriteArtistUseCase(artistRepository: ArtistRepository): RemoveFavoriteArtist {
        return RemoveFavoriteArtist(artistRepository)
    }

    @Provides
    @Singleton
    fun provideRemoveAllFavoriteArtistsUseCase(artistRepository: ArtistRepository): RemoveAllFavoriteArtists {
        return RemoveAllFavoriteArtists(artistRepository)
    }

    @Provides
    fun provideNetworkMonitor(
        @ApplicationContext context: Context
    ): NetworkMonitor = NetworkMonitorImpl(context)
}
