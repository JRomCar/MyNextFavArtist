package com.jrom.mynextfavartist.data.repository

import com.jrom.mynextfavartist.domain.EmptyResult
import com.jrom.mynextfavartist.domain.Result
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.entities.ReleaseGroup
import com.jrom.mynextfavartist.domain.error.DataError
import com.jrom.mynextfavartist.domain.repository.ArtistRepository
import kotlinx.coroutines.flow.Flow

class ArtistRepositoryImpl(
    private val remote: ArtistDataSource.Remote,
    private val local: ArtistDataSource.Local,
) : ArtistRepository {

    override suspend fun searchArtists(
        query: String,
        limit: Int,
        offset: Int,
    ): Result<List<Artist>, DataError.Network> = remote.searchArtists(query, limit, offset)

    override suspend fun getArtistReleaseGroups(
        artistMbid: String,
    ): Result<List<ReleaseGroup>, DataError.Network> = remote.getArtistReleaseGroups(artistMbid)

    override fun observeFavoriteArtists(): Flow<Result<List<Artist>, DataError.Local>> =
        local.observeAllArtists()

    override suspend fun saveFavoriteArtist(artist: Artist): EmptyResult<DataError.Local> =
        local.saveFavoriteArtist(artist)

    override suspend fun removeFavoriteArtist(artistMbid: String): EmptyResult<DataError.Local> =
        local.removeFavoriteArtist(artistMbid)

    override suspend fun removeAllFavoriteArtists(): EmptyResult<DataError.Local> =
        local.clearArtists()

    override fun observeIsFavorite(artistMbid: String): Flow<Result<Boolean, DataError.Local>> =
        local.observeIsFavorite(artistMbid)
}
