package com.jrom.mynextfavartist.ui.favorites

import com.jrom.mynextfavartist.domain.entities.Artist

sealed interface FavoritesUiAction {
    data class ArtistClicked(val artist: Artist) : FavoritesUiAction
    data object ClearAllSavedArtists : FavoritesUiAction
    data object LoadArtists : FavoritesUiAction
}
