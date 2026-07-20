package com.jrom.mynextfavartist.data.repository

import com.jrom.mynextfavartist.domain.EmptyResult
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
        suspend fun saveFavoriteArtist(artist: Artist): EmptyResult<DataError.Local>
        suspend fun removeFavoriteArtist(artistMbid: String): EmptyResult<DataError.Local>
        suspend fun clearArtists(): EmptyResult<DataError.Local>
        fun observeIsFavorite(artistMbid: String): Flow<Result<Boolean, DataError.Local>>
    }

    interface HomeCache {
        /** Cached home artists no older than [maxAgeMillis], or null if there's no fresh entry. */
        suspend fun getFreshHomeArtists(maxAgeMillis: Long): List<Artist>?

        /** Whatever's cached regardless of age, or null if the cache is empty. */
        suspend fun getStaleHomeArtists(): List<Artist>?

        suspend fun replaceHomeArtists(artists: List<Artist>)
    }
}
