package com.jrom.mynextfavartist.di

import android.content.Context
import androidx.room.Room
import com.jrom.mynextfavartist.data.db.ArtistDao
import com.jrom.mynextfavartist.data.db.ArtistDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun provideArtistDatabase(
        @ApplicationContext context: Context,
    ): ArtistDatabase {
        return Room
            .databaseBuilder(context, ArtistDatabase::class.java, "artist.db")
            .build()
    }

    @Provides
    fun provideArtistDao(database: ArtistDatabase): ArtistDao {
        return database.artistDao()
    }
}
