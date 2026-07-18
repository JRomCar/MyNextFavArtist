package com.jrom.mynextfavartist.domain.usecase

import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.repository.ArtistRepository

class SaveFavoriteArtist(private val artistRepository: ArtistRepository) {
    suspend operator fun invoke(artist: Artist) = artistRepository.saveFavoriteArtist(artist)
}
