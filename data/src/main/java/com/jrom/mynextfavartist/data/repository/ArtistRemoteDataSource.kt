package com.jrom.mynextfavartist.data.repository

import com.jrom.mynextfavartist.data.api.MusicBrainzApi
import com.jrom.mynextfavartist.data.entities.ReleaseGroupData
import com.jrom.mynextfavartist.data.util.mapThrowableToNetworkError
import com.jrom.mynextfavartist.data.util.toDomain
import com.jrom.mynextfavartist.domain.Result
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
    private val ioDispatcher: CoroutineDispatcher,
    private val musicBrainzApi: MusicBrainzApi,
) : ArtistDataSource.Remote {

    override suspend fun searchArtists(
        query: String,
        limit: Int,
        offset: Int,
    ): Result<List<Artist>, DataError.Network> = search(query.escapeLucene(), limit, offset)

    override suspend fun searchByArtistIds(
        artistMbids: List<String>,
    ): Result<List<Artist>, DataError.Network> {
        val query = artistMbids.joinToString(separator = " OR ", prefix = "arid:(", postfix = ")")
        return search(query, limit = artistMbids.size, offset = 0)
    }

    private suspend fun search(
        query: String,
        limit: Int,
        offset: Int,
    ): Result<List<Artist>, DataError.Network> = withContext(ioDispatcher) {
        return@withContext try {
            val response = musicBrainzApi.searchArtists(query = query, limit = limit, offset = offset)
            Result.Success(response.artists?.mapNotNull { it.toDomain() } ?: emptyList())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Failure(mapThrowableToNetworkError(e))
        }
    }

    override suspend fun getArtistReleaseGroups(
        artistMbid: String,
    ): Result<List<ReleaseGroup>, DataError.Network> = withContext(ioDispatcher) {
        return@withContext try {
            // Descending puts the newest release first and, as a side effect of Kotlin's null
            // handling in descending sorts, pushes releases with no known date to the end
            // instead of leading the discography.
            val releaseGroups = fetchAllReleaseGroupPages(artistMbid)
                .mapNotNull { it.toDomain() }
                .sortedByDescending { it.firstReleaseDate }
            Result.Success(releaseGroups)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Failure(mapThrowableToNetworkError(e))
        }
    }

    // The browse endpoint caps each response at `limit` entries, so a prolific artist's
    // discography would otherwise be silently truncated. Each page still goes through the
    // shared RateLimitInterceptor, so this stays within the 1 req/s budget.
    private suspend fun fetchAllReleaseGroupPages(artistMbid: String): List<ReleaseGroupData> {
        val allReleaseGroups = mutableListOf<ReleaseGroupData>()
        var pagesFetched = 0
        while (pagesFetched < MAX_RELEASE_GROUP_PAGES) {
            val response = musicBrainzApi.getReleaseGroupsForArtist(
                artistMbid = artistMbid,
                offset = allReleaseGroups.size,
            )
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

// MusicBrainz's `query` param is a Lucene expression, so free-text user input (e.g. "AC/DC",
// "Panic! At The Disco") must have Lucene's reserved characters escaped before being sent, or
// the API rejects it with a 400. This is specific to MusicBrainz's search backend, so it lives
// here rather than in the domain use case that originates the free-text query.
private val LUCENE_SPECIAL_CHARS = "+-&|!(){}[]^\"~*?:\\/".toSet()

private fun String.escapeLucene(): String = buildString {
    for (c in this@escapeLucene) {
        if (c in LUCENE_SPECIAL_CHARS) append('\\')
        append(c)
    }
}
