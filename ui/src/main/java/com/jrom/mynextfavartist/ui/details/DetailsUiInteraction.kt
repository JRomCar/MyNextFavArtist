package com.jrom.mynextfavartist.ui.details

import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.ui.error.UiText

sealed interface DetailsUiAction {
    data class LoadArtistDetails(val artist: Artist) : DetailsUiAction
    data class ToggleFavorite(val artist: Artist) : DetailsUiAction
    data object OnBackRequest : DetailsUiAction
}

sealed interface DetailsUiEffect {
    data object NavigateBack : DetailsUiEffect
    data class ShowMessage(val message: UiText) : DetailsUiEffect
}
