package com.jrom.mynextfavartist.ui.states

import com.jrom.mynextfavartist.domain.entities.ReleaseGroup

/**
 * This screen has two independent async concerns:
 * the favorite toggle, and fetching the artist's release groups. Sharing one BaseUiState
 * between them would mean a favorite-toggle blanking the already-loaded release-group list
 * with a full-screen Loading state, so they get separate fields instead.
 */
data class DetailsUiState(
    val isFavorite: Boolean = false,
    val isFavoriteActionInProgress: Boolean = false,
    val releaseGroups: BaseUiState<List<ReleaseGroup>> = BaseUiState.Initial,
)
