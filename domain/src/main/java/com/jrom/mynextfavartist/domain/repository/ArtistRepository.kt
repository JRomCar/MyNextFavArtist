package com.jrom.mynextfavartist.domain.repository

import com.jrom.mynextfavartist.domain.EmptyResult
import com.jrom.mynextfavartist.domain.Result
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.entities.ReleaseGroup
import com.jrom.mynextfavartist.domain.error.DataError
import kotlinx.coroutines.flow.Flow

interface ArtistRepository {

    suspend fun searchArtists(
        query: String,
        limit: Int = 30,
        offset: Int = 0,
    ): Result<List<Artist>, DataError.Network>

    suspend fun getArtistReleaseGroups(artistMbid: String): Result<List<ReleaseGroup>, DataError.Network>

    fun observeFavoriteArtists(): Flow<Result<List<Artist>, DataError.Local>>
    suspend fun saveFavoriteArtist(artist: Artist): EmptyResult<DataError.Local>
    suspend fun removeFavoriteArtist(artistMbid: String): EmptyResult<DataError.Local>
    suspend fun removeAllFavoriteArtists(): EmptyResult<DataError.Local>
    fun observeIsFavorite(artistMbid: String): Flow<Result<Boolean, DataError.Local>>
}
