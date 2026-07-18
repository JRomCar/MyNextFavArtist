package com.jrom.mynextfavartist.data.repository

import com.jrom.mynextfavartist.domain.Result
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.entities.ReleaseGroup
import com.jrom.mynextfavartist.domain.error.DataError

interface ArtistDataSource {

    interface Remote {
        suspend fun searchArtists(query: String, limit: Int, offset: Int): Result<List<Artist>, DataError.Network>
        suspend fun getArtistReleaseGroups(artistMbid: String): Result<List<ReleaseGroup>, DataError.Network>
    }

    interface Local {
        suspend fun getAllArtists(): Result<List<Artist>, DataError.Local>
        suspend fun saveFavoriteArtist(artist: Artist): Result<Boolean, DataError.Local>
        suspend fun removeFavoriteArtist(artistMbid: String): Result<Boolean, DataError.Local>
        suspend fun clearArtists(): Result<Boolean, DataError.Local>
        suspend fun checkIfArtistIsFavorite(artistMbid: String): Result<Boolean, DataError.Local>
    }
}
