package com.jrom.mynextfavartist.domain.usecase

import com.jrom.mynextfavartist.domain.repository.ArtistRepository

class RemoveAllFavoriteArtists(private val artistRepository: ArtistRepository) {
    suspend operator fun invoke() = artistRepository.removeAllFavoriteArtists()
}
