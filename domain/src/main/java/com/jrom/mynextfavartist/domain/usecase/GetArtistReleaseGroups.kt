package com.jrom.mynextfavartist.domain.usecase

import com.jrom.mynextfavartist.domain.repository.ArtistRepository

class GetArtistReleaseGroups(private val artistRepository: ArtistRepository) {
    suspend operator fun invoke(artistMbid: String) = artistRepository.getArtistReleaseGroups(artistMbid)
}
