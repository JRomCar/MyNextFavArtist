package com.jrom.mynextfavartist.ui.details

import com.jrom.mynextfavartist.ui.error.UiText

// No longer carries the artist, and no longer has a "load" action - DetailsViewModel receives
// the artist once via assisted injection (see DetailsViewModel.Factory) and triggers its own
// initial load on first subscription, so neither needs to be threaded through an action.
sealed interface DetailsUiAction {
    data object ToggleFavorite : DetailsUiAction
    data object RetryReleaseGroups : DetailsUiAction
    data object OnBackRequest : DetailsUiAction
}

sealed interface DetailsUiEffect {
    data object NavigateBack : DetailsUiEffect
    data class ShowMessage(val message: UiText) : DetailsUiEffect
}
