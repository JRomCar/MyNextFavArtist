package com.jrom.mynextfavartist.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jrom.mynextfavartist.data.entities.ArtistDbData
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {
    /**
     * Delete favorite artist by mbid.
     * @return number of rows deleted
     */
    @Query("DELETE FROM artists WHERE mbid=:artistMbid")
    suspend fun removeArtist(artistMbid: String): Int

    /**
     * Insert favorite artist.
     * @return row ID of inserted artist, -1 if failed
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveArtist(artist: ArtistDbData): Long

    /**
     * Observe all saved favorite artists.
     */
    @Query("SELECT * FROM artists ORDER BY name ASC")
    fun observeAllArtists(): Flow<List<ArtistDbData>>

    /**
     * Delete all favorite artists.
     * @return number of rows deleted
     */
    @Query("DELETE FROM artists")
    suspend fun clearArtists(): Int

    /**
     * Observe whether an artist is a saved favorite.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM artists WHERE mbid = :artistMbid)")
    fun observeIsFavorite(artistMbid: String): Flow<Boolean>
}
