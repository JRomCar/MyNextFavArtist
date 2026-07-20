package com.jrom.mynextfavartist.data.repository

import com.jrom.mynextfavartist.data.db.HomeArtistCacheDao
import com.jrom.mynextfavartist.data.util.toDomain
import com.jrom.mynextfavartist.data.util.toHomeCacheData
import com.jrom.mynextfavartist.domain.entities.Artist

class HomeArtistsCacheDataSource(
    private val homeArtistCacheDao: HomeArtistCacheDao,
) : ArtistDataSource.HomeCache {

    override suspend fun getFreshHomeArtists(maxAgeMillis: Long): List<Artist>? {
        val minCachedAt = System.currentTimeMillis() - maxAgeMillis
        val cached = homeArtistCacheDao.getFreshArtists(minCachedAt)
        return cached.ifEmpty { null }?.map { it.toDomain() }
    }

    override suspend fun getStaleHomeArtists(): List<Artist>? =
        homeArtistCacheDao.getAllArtists().ifEmpty { null }?.map { it.toDomain() }

    override suspend fun replaceHomeArtists(artists: List<Artist>) {
        val cachedAt = System.currentTimeMillis()
        homeArtistCacheDao.replaceAll(artists.map { it.toHomeCacheData(cachedAt) })
    }
}
