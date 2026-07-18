package com.jrom.mynextfavartist.data.util

import com.jrom.mynextfavartist.data.entities.ArtistData
import com.jrom.mynextfavartist.data.entities.ArtistDbData
import com.jrom.mynextfavartist.data.entities.ReleaseGroupData
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.entities.ReleaseGroup
import com.jrom.mynextfavartist.domain.error.DataError
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException

fun ArtistData.toDomain() = Artist(
    mbid = id,
    name = name,
    type = type,
    country = country,
    disambiguation = disambiguation,
)

fun ArtistDbData.toDomain() = Artist(
    mbid = mbid,
    name = name,
    type = type,
    country = country,
    disambiguation = disambiguation,
)

fun Artist.toDb() = ArtistDbData(
    mbid = mbid,
    name = name,
    type = type,
    country = country,
    disambiguation = disambiguation,
)

fun ReleaseGroupData.toDomain() = ReleaseGroup(
    mbid = id,
    title = title,
    primaryType = primaryType,
    firstReleaseDate = firstReleaseDate,
)

fun mapThrowableToNetworkError(throwable: Throwable): DataError.Network {
    return when (throwable) {
        is HttpException -> when (throwable.code()) {
            HttpURLConnection.HTTP_BAD_REQUEST -> DataError.Network.BAD_REQUEST
            HttpURLConnection.HTTP_NOT_FOUND -> DataError.Network.NOT_FOUND
            // MusicBrainz returns 503 (and sometimes 429) when a client exceeds the
            // 1 req/sec limit, not just for generic server overload.
            429, HttpURLConnection.HTTP_UNAVAILABLE -> DataError.Network.RATE_LIMITED
            HttpURLConnection.HTTP_CLIENT_TIMEOUT, HttpURLConnection.HTTP_GATEWAY_TIMEOUT -> DataError.Network.TIMEOUT
            HttpURLConnection.HTTP_INTERNAL_ERROR, HttpURLConnection.HTTP_BAD_GATEWAY -> DataError.Network.SERVER_ERROR
            else -> DataError.Network.UNKNOWN
        }
        // Connectivity failures (no network, unknown host, read timeout) never reach the
        // HttpException branch, so they must be handled here to avoid a crash.
        is SocketTimeoutException -> DataError.Network.TIMEOUT
        is IOException -> DataError.Network.UNKNOWN
        else -> DataError.Network.UNKNOWN
    }
}
