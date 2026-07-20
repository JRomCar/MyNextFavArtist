package com.jrom.mynextfavartist.data.repository

import com.jrom.mynextfavartist.data.db.ArtistDao
import com.jrom.mynextfavartist.data.entities.ArtistDbData
import com.jrom.mynextfavartist.data.util.toDb
import com.jrom.mynextfavartist.data.util.toDomain
import com.jrom.mynextfavartist.domain.Result
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.error.DataError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class ArtistLocalDataSource(
    private val artistDao: ArtistDao,
) : ArtistDataSource.Local {

    override fun observeAllArtists(): Flow<Result<List<Artist>, DataError.Local>> =
        artistDao.observeAllArtists()
            .map<List<ArtistDbData>, Result<List<Artist>, DataError.Local>> { artists ->
                Result.Success(artists.map { it.toDomain() })
            }
            .catch { emit(Result.Error(DataError.Local.DB_READ_ERROR)) }

    override suspend fun saveFavoriteArtist(artist: Artist): Result<Boolean, DataError.Local> =
        try {
            val rowId = artistDao.saveArtist(artist.toDb())
            Result.Success(rowId != -1L)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            Result.Error(DataError.Local.DB_WRITE_ERROR)
        }

    override suspend fun removeFavoriteArtist(artistMbid: String): Result<Boolean, DataError.Local> =
        try {
            val rowsDeleted = artistDao.removeArtist(artistMbid)
            Result.Success(rowsDeleted > 0)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            Result.Error(DataError.Local.DB_WRITE_ERROR)
        }

    override suspend fun clearArtists(): Result<Boolean, DataError.Local> =
        try {
            artistDao.clearArtists()
            Result.Success(true)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            Result.Error(DataError.Local.DB_WRITE_ERROR)
        }

    override fun observeIsFavorite(artistMbid: String): Flow<Result<Boolean, DataError.Local>> =
        artistDao.observeIsFavorite(artistMbid)
            .map<Boolean, Result<Boolean, DataError.Local>> { Result.Success(it) }
            .catch { emit(Result.Error(DataError.Local.DB_READ_ERROR)) }
}
