package com.jrom.mynextfavartist.ui.home

import com.jrom.mynextfavartist.domain.entities.Artist

sealed interface HomeUiAction {
    data class ArtistClicked(val artist: Artist) : HomeUiAction
    data object LoadArtists : HomeUiAction
}
