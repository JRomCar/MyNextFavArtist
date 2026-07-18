package com.jrom.mynextfavartist.domain.usecase

import com.jrom.mynextfavartist.domain.repository.ArtistRepository

class RemoveFavoriteArtist(private val artistRepository: ArtistRepository) {
    suspend operator fun invoke(artistMbid: String) = artistRepository.removeFavoriteArtist(artistMbid)
}
