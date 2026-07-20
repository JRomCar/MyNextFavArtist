package com.jrom.mynextfavartist.data.repository

import com.jrom.mynextfavartist.data.db.ArtistDao
import com.jrom.mynextfavartist.data.entities.ArtistDbData
import com.jrom.mynextfavartist.data.util.toDb
import com.jrom.mynextfavartist.data.util.toDomain
import com.jrom.mynextfavartist.domain.EmptyResult
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

    // OnConflictStrategy.REPLACE means the insert only fails via a thrown exception, never
    // by returning -1, so a successful call here always means the artist is saved.
    override suspend fun saveFavoriteArtist(artist: Artist): EmptyResult<DataError.Local> =
        try {
            artistDao.saveArtist(artist.toDb())
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            Result.Error(DataError.Local.DB_WRITE_ERROR)
        }

    override suspend fun removeFavoriteArtist(artistMbid: String): EmptyResult<DataError.Local> =
        try {
            val rowsDeleted = artistDao.removeArtist(artistMbid)
            if (rowsDeleted > 0) Result.Success(Unit) else Result.Error(DataError.Local.DB_WRITE_ERROR)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            Result.Error(DataError.Local.DB_WRITE_ERROR)
        }

    override suspend fun clearArtists(): EmptyResult<DataError.Local> =
        try {
            val rowsDeleted = artistDao.clearArtists()
            if (rowsDeleted > 0) Result.Success(Unit) else Result.Error(DataError.Local.DB_WRITE_ERROR)
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
