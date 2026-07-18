package com.jrom.mynextfavartist.ui.search

import com.jrom.mynextfavartist.domain.entities.Artist

sealed interface SearchUiAction {
    data class SearchRequest(val query: String) : SearchUiAction
    data class ArtistClicked(val artist: Artist) : SearchUiAction
}
