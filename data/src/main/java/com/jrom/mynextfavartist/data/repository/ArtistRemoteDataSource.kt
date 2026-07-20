package com.jrom.mynextfavartist.data.repository

import com.jrom.mynextfavartist.data.api.MusicBrainzApi
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
            val response = retryOnTransientFailure {
                musicBrainzApi.getReleaseGroupsForArtist(artistMbid = artistMbid)
            }
            val releaseGroups = response.releaseGroups
                ?.mapNotNull { it.toDomain() }
                ?.sortedBy { it.firstReleaseDate ?: "" }
                ?: emptyList()
            Result.Success(releaseGroups)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(mapThrowableToNetworkError(e))
        }
    }
}
