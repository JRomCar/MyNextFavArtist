package com.jrom.mynextfavartist.data.repository

import com.jrom.mynextfavartist.data.db.ArtistDao
import com.jrom.mynextfavartist.data.util.toDb
import com.jrom.mynextfavartist.data.util.toDomain
import com.jrom.mynextfavartist.domain.Result
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.error.DataError

class ArtistLocalDataSource(
    private val artistDao: ArtistDao,
) : ArtistDataSource.Local {

    override suspend fun getAllArtists(): Result<List<Artist>, DataError.Local> =
        try {
            Result.Success(artistDao.getAllArtists().map { it.toDomain() })
        } catch (_: Exception) {
            Result.Error(DataError.Local.DB_READ_ERROR)
        }

    override suspend fun saveFavoriteArtist(artist: Artist): Result<Boolean, DataError.Local> =
        try {
            val rowId = artistDao.saveArtist(artist.toDb())
            Result.Success(rowId != -1L)
        } catch (_: Exception) {
            Result.Error(DataError.Local.DB_WRITE_ERROR)
        }

    override suspend fun removeFavoriteArtist(artistMbid: String): Result<Boolean, DataError.Local> =
        try {
            val rowsDeleted = artistDao.removeArtist(artistMbid)
            Result.Success(rowsDeleted > 0)
        } catch (_: Exception) {
            Result.Error(DataError.Local.DB_WRITE_ERROR)
        }

    override suspend fun clearArtists(): Result<Boolean, DataError.Local> =
        try {
            artistDao.clearArtists()
            Result.Success(true)
        } catch (_: Exception) {
            Result.Error(DataError.Local.DB_WRITE_ERROR)
        }

    override suspend fun checkIfArtistIsFavorite(artistMbid: String): Result<Boolean, DataError.Local> =
        try {
            Result.Success(artistDao.getArtist(artistMbid) != null)
        } catch (_: Exception) {
            Result.Error(DataError.Local.DB_READ_ERROR)
        }
}
