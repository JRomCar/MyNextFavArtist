package com.jrom.mynextfavartist.data.repository

import com.jrom.mynextfavartist.domain.Result
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.entities.ReleaseGroup
import com.jrom.mynextfavartist.domain.error.DataError
import kotlinx.coroutines.flow.Flow

interface ArtistDataSource {

    interface Remote {
        suspend fun searchArtists(query: String, limit: Int, offset: Int): Result<List<Artist>, DataError.Network>
        suspend fun getArtistReleaseGroups(artistMbid: String): Result<List<ReleaseGroup>, DataError.Network>
    }

    interface Local {
        fun observeAllArtists(): Flow<Result<List<Artist>, DataError.Local>>
        suspend fun saveFavoriteArtist(artist: Artist): Result<Boolean, DataError.Local>
        suspend fun removeFavoriteArtist(artistMbid: String): Result<Boolean, DataError.Local>
        suspend fun clearArtists(): Result<Boolean, DataError.Local>
        fun observeIsFavorite(artistMbid: String): Flow<Result<Boolean, DataError.Local>>
    }
}
