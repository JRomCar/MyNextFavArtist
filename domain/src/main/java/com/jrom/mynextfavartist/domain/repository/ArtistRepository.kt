package com.jrom.mynextfavartist.domain.repository

import com.jrom.mynextfavartist.domain.Result
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.entities.ReleaseGroup
import com.jrom.mynextfavartist.domain.error.DataError

interface ArtistRepository {

    suspend fun searchArtists(
        query: String,
        limit: Int = 30,
        offset: Int = 0,
    ): Result<List<Artist>, DataError.Network>

    suspend fun getArtistReleaseGroups(artistMbid: String): Result<List<ReleaseGroup>, DataError.Network>

    suspend fun getAllFavoriteArtists(): Result<List<Artist>, DataError.Local>
    suspend fun saveFavoriteArtist(artist: Artist): Result<Boolean, DataError.Local>
    suspend fun removeFavoriteArtist(artistMbid: String): Result<Boolean, DataError.Local>
    suspend fun removeAllFavoriteArtists(): Result<Boolean, DataError.Local>
    suspend fun checkIfArtistIsFavorite(artistMbid: String): Result<Boolean, DataError.Local>
}
