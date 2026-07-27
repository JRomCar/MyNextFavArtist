package com.jrom.mynextfavartist.ui.details

import com.jrom.mynextfavartist.ui.error.UiText

// No longer carries the artist - DetailsViewModel now receives it once via assisted injection
// (see DetailsViewModel.Factory), so these actions don't need to thread it through.
sealed interface DetailsUiAction {
    data object LoadArtistDetails : DetailsUiAction
    data object ToggleFavorite : DetailsUiAction
    data object OnBackRequest : DetailsUiAction
}

sealed interface DetailsUiEffect {
    data object NavigateBack : DetailsUiEffect
    data class ShowMessage(val message: UiText) : DetailsUiEffect
}
