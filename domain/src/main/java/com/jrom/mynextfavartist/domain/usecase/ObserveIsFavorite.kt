package com.jrom.mynextfavartist.domain.usecase

import com.jrom.mynextfavartist.domain.repository.ArtistRepository

class ObserveIsFavorite(private val artistRepository: ArtistRepository) {
    operator fun invoke(artistMbid: String) = artistRepository.observeIsFavorite(artistMbid)
}
