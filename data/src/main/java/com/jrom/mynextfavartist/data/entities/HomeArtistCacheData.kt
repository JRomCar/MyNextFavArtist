package com.jrom.mynextfavartist.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// cachedAt lets a freshness query (WHERE cachedAt >= :minCachedAt) express the TTL in SQL rather
// than pulling every row and comparing in Kotlin.
@Entity(tableName = "home_artists_cache")
data class HomeArtistCacheData(
    @PrimaryKey val mbid: String,
    val name: String,
    val type: String?,
    val country: String?,
    val disambiguation: String?,
    val cachedAt: Long,
)
