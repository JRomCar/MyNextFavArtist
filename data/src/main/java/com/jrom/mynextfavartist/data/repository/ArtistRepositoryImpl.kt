package com.jrom.mynextfavartist.data.repository

import com.jrom.mynextfavartist.domain.EmptyResult
import com.jrom.mynextfavartist.domain.Result
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.entities.ReleaseGroup
import com.jrom.mynextfavartist.domain.error.DataError
import com.jrom.mynextfavartist.domain.repository.ArtistRepository
import kotlinx.coroutines.flow.Flow

// The seed list is a fixed set of well-known MBIDs - their metadata (name, type, country)
// essentially never changes, so a day-long cache avoids a live request on every Home visit
// without risking noticeably stale data.
private const val HOME_ARTISTS_CACHE_TTL_MILLIS = 24 * 60 * 60 * 1000L

class ArtistRepositoryImpl(
    private val remote: ArtistDataSource.Remote,
    private val local: ArtistDataSource.Local,
    private val homeCache: ArtistDataSource.HomeCache,
) : ArtistRepository {

    override suspend fun searchArtists(
        query: String,
        limit: Int,
        offset: Int,
    ): Result<List<Artist>, DataError.Network> = remote.searchArtists(query, limit, offset)

    // OkHttp's HTTP cache never applies here (or to any endpoint in this app): heuristic
    // freshness requires a query-string-free request URL, and every MusicBrainzApi call is
    // @Query-based. A Room-backed cache is the only way to actually skip the network call.
    override suspend fun getHomeArtists(): Result<List<Artist>, DataError.Network> {
        homeCache.getFreshHomeArtists(HOME_ARTISTS_CACHE_TTL_MILLIS)?.let { return Result.Success(it) }

        return when (val result = remote.searchByArtistIds(SeedArtists.mbids)) {
            is Result.Success -> {
                homeCache.replaceHomeArtists(result.data)
                result
            }
            is Result.Failure -> homeCache.getStaleHomeArtists()?.let { Result.Success(it) } ?: result
        }
    }

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
