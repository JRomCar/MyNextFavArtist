package com.jrom.mynextfavartist.domain.usecase

import com.jrom.mynextfavartist.domain.repository.ArtistRepository

/**
 * Fetches the curated [SeedArtists] list in a single MusicBrainz request via a Lucene
 * OR query over the `arid` (artist id) field, e.g. `arid:(id1 OR id2 OR ...)`. Looking each
 * MBID up individually would take 20+ seconds at MusicBrainz's 1 request/second limit.
 */
class GetHomeArtists(private val artistRepository: ArtistRepository) {
    suspend operator fun invoke() = artistRepository.searchArtists(
        query = SeedArtists.mbids.joinToString(separator = " OR ", prefix = "arid:(", postfix = ")"),
        limit = SeedArtists.mbids.size,
    )
}
