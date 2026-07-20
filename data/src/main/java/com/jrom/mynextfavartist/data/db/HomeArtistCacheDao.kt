package com.jrom.mynextfavartist.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.jrom.mynextfavartist.data.entities.HomeArtistCacheData

@Dao
interface HomeArtistCacheDao {
    /**
     * Cached home artists no older than [minCachedAt] (epoch millis).
     */
    @Query("SELECT * FROM home_artists_cache WHERE cachedAt >= :minCachedAt ORDER BY name ASC")
    suspend fun getFreshArtists(minCachedAt: Long): List<HomeArtistCacheData>

    /**
     * Every cached home artist regardless of age, for the offline/network-failure fallback.
     */
    @Query("SELECT * FROM home_artists_cache ORDER BY name ASC")
    suspend fun getAllArtists(): List<HomeArtistCacheData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(artists: List<HomeArtistCacheData>)

    @Query("DELETE FROM home_artists_cache")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(artists: List<HomeArtistCacheData>) {
        clear()
        insertAll(artists)
    }
}
