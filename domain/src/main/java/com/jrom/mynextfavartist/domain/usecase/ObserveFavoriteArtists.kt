package com.jrom.mynextfavartist.domain.usecase

import com.jrom.mynextfavartist.domain.Result
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.error.DataError
import com.jrom.mynextfavartist.domain.repository.ArtistRepository
import kotlinx.coroutines.flow.Flow

class ObserveFavoriteArtists(private val artistRepository: ArtistRepository) {
    operator fun invoke(): Flow<Result<List<Artist>, DataError.Local>> =
        artistRepository.observeFavoriteArtists()
}
