package com.jrom.mynextfavartist.domain.usecase

import com.jrom.mynextfavartist.domain.repository.ArtistRepository

/**
 * Fetches the curated [SeedArtists] list, cached in Room with a TTL by the repository - see
 * [ArtistRepository.getHomeArtists]. The underlying network request (when the cache is stale)
 * is a single Lucene OR query over the `arid` (artist id) field, e.g. `arid:(id1 OR id2 OR ...)`;
 * looking each MBID up individually would take 20+ seconds at MusicBrainz's 1 request/second
 * limit.
 */
class GetHomeArtists(private val artistRepository: ArtistRepository) {
    suspend operator fun invoke() = artistRepository.getHomeArtists()
}
