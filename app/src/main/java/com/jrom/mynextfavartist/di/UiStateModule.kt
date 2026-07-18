package com.jrom.mynextfavartist.di

import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.ui.states.BaseUiState
import com.jrom.mynextfavartist.ui.states.DetailsUiState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UiStateModule {

    @Provides
    fun provideInitialArtistListUiState(): BaseUiState<List<Artist>> = BaseUiState.Initial

    @Provides
    fun provideInitialDetailsUiState(): DetailsUiState = DetailsUiState()
}
