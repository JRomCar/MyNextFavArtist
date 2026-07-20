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
        flow { emit(artistRepository.searchArtists(query)) }
}
