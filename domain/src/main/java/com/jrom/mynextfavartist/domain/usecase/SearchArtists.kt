package com.jrom.mynextfavartist.domain.usecase

import com.jrom.mynextfavartist.domain.Result
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.error.DataError
import com.jrom.mynextfavartist.domain.repository.ArtistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Wraps a single suspend call in a cold [Flow] so the ViewModel can drive it with
 * `flatMapLatest`, cancelling any in-flight search when a newer query arrives.
 */
class SearchArtists(private val artistRepository: ArtistRepository) {
    operator fun invoke(query: String): Flow<Result<List<Artist>, DataError.Network>> =
        flow { emit(artistRepository.searchArtists(query.escapeLucene())) }
}

// MusicBrainz's `query` param is a Lucene expression, so free-text user input (e.g. "AC/DC",
// "Panic! At The Disco") must have Lucene's reserved characters escaped before being sent, or
// the API rejects it with a 400. ArtistRepositoryImpl.getHomeArtists() deliberately builds its
// own Lucene expression (`arid:(id1 OR id2)`) and calls the remote data source directly,
// bypassing this escaping on purpose.
private val LUCENE_SPECIAL_CHARS = "+-&|!(){}[]^\"~*?:\\/".toSet()

private fun String.escapeLucene(): String = buildString {
    for (c in this@escapeLucene) {
        if (c in LUCENE_SPECIAL_CHARS) append('\\')
        append(c)
    }
}
