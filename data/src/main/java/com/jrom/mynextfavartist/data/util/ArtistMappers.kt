package com.jrom.mynextfavartist.data.util

import com.jrom.mynextfavartist.data.entities.ArtistData
import com.jrom.mynextfavartist.data.entities.ArtistDbData
import com.jrom.mynextfavartist.data.entities.HomeArtistCacheData
import com.jrom.mynextfavartist.data.entities.ReleaseGroupData
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.entities.ReleaseGroup

// Returns null when id/name are missing, so the caller can drop the malformed entry instead
// of propagating a null into Artist's non-null fields.
fun ArtistData.toDomain(): Artist? {
    val artistMbid = id ?: return null
    val artistName = name ?: return null
    return Artist(
        mbid = artistMbid,
        name = artistName,
        type = type,
        country = country,
        disambiguation = disambiguation,
    )
}

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

fun HomeArtistCacheData.toDomain() = Artist(
    mbid = mbid,
    name = name,
    type = type,
    country = country,
    disambiguation = disambiguation,
)

fun Artist.toHomeCacheData(cachedAt: Long) = HomeArtistCacheData(
    mbid = mbid,
    name = name,
    type = type,
    country = country,
    disambiguation = disambiguation,
    cachedAt = cachedAt,
)

// Returns null when id/title are missing - see ArtistData.toDomain() above.
fun ReleaseGroupData.toDomain(): ReleaseGroup? {
    val releaseGroupMbid = id ?: return null
    val releaseGroupTitle = title ?: return null
    return ReleaseGroup(
        mbid = releaseGroupMbid,
        title = releaseGroupTitle,
        primaryType = primaryType,
        firstReleaseDate = firstReleaseDate,
    )
}
