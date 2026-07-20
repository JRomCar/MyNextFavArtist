package com.jrom.mynextfavartist.data.repository

import com.jrom.mynextfavartist.data.api.MusicBrainzApi
import com.jrom.mynextfavartist.data.entities.ReleaseGroupData
import com.jrom.mynextfavartist.data.util.mapThrowableToNetworkError
import com.jrom.mynextfavartist.data.util.retryOnTransientFailure
import com.jrom.mynextfavartist.data.util.toDomain
import com.jrom.mynextfavartist.domain.Result
import com.jrom.mynextfavartist.domain.di.IoDispatcher
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.entities.ReleaseGroup
import com.jrom.mynextfavartist.domain.error.DataError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

// Real artist discographies stay well under this; it only exists to bound the loop below
// against a malformed/looping response instead of relying purely on the API's own count.
private const val MAX_RELEASE_GROUP_PAGES = 20

class ArtistRemoteDataSource(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val musicBrainzApi: MusicBrainzApi,
) : ArtistDataSource.Remote {

    override suspend fun searchArtists(
        query: String,
        limit: Int,
        offset: Int,
    ): Result<List<Artist>, DataError.Network> = withContext(ioDispatcher) {
        return@withContext try {
            val response = retryOnTransientFailure {
                musicBrainzApi.searchArtists(query = query, limit = limit, offset = offset)
            }
            Result.Success(response.artists?.mapNotNull { it.toDomain() } ?: emptyList())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(mapThrowableToNetworkError(e))
        }
    }

    override suspend fun getArtistReleaseGroups(
        artistMbid: String,
    ): Result<List<ReleaseGroup>, DataError.Network> = withContext(ioDispatcher) {
        return@withContext try {
            val releaseGroups = fetchAllReleaseGroupPages(artistMbid)
                .mapNotNull { it.toDomain() }
                .sortedBy { it.firstReleaseDate ?: "" }
            Result.Success(releaseGroups)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(mapThrowableToNetworkError(e))
        }
    }

    // The browse endpoint caps each response at `limit` entries, so a prolific artist's
    // discography would otherwise be silently truncated. Each page still goes through the
    // shared RateLimitInterceptor, so this stays within the 1 req/s budget.
    private suspend fun fetchAllReleaseGroupPages(artistMbid: String): List<ReleaseGroupData> {
        val allReleaseGroups = mutableListOf<ReleaseGroupData>()
        var pagesFetched = 0
        while (pagesFetched < MAX_RELEASE_GROUP_PAGES) {
            val response = retryOnTransientFailure {
                musicBrainzApi.getReleaseGroupsForArtist(artistMbid = artistMbid, offset = allReleaseGroups.size)
            }
            pagesFetched++
            val page = response.releaseGroups.orEmpty()
            if (page.isEmpty()) break
            allReleaseGroups += page
            val total = response.releaseGroupCount ?: allReleaseGroups.size
            if (allReleaseGroups.size >= total) break
        }
        return allReleaseGroups
    }
}
