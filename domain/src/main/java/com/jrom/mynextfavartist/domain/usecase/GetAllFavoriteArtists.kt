package com.jrom.mynextfavartist.domain.usecase

import com.jrom.mynextfavartist.domain.Result
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.error.DataError
import com.jrom.mynextfavartist.domain.repository.ArtistRepository

class GetAllFavoriteArtists(private val artistRepository: ArtistRepository) {
    suspend operator fun invoke(): Result<List<Artist>, DataError.Local> =
        artistRepository.getAllFavoriteArtists()
}
