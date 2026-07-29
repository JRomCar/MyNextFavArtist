package com.jrom.mynextfavartist.ui.details

import androidx.lifecycle.viewModelScope
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.error.DataError
import com.jrom.mynextfavartist.domain.fold
import com.jrom.mynextfavartist.domain.usecase.GetArtistReleaseGroups
import com.jrom.mynextfavartist.domain.usecase.ObserveIsFavorite
import com.jrom.mynextfavartist.domain.usecase.RemoveFavoriteArtist
import com.jrom.mynextfavartist.domain.usecase.SaveFavoriteArtist
import com.jrom.mynextfavartist.ui.error.asUiIcon
import com.jrom.mynextfavartist.ui.error.asUiText
import com.jrom.mynextfavartist.ui.states.BaseUiState
import com.jrom.mynextfavartist.ui.states.BaseViewModel
import com.jrom.mynextfavartist.ui.states.DetailsUiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

// Each ArtistDetails back-stack entry is scoped to its own ViewModelStore (see
// rememberViewModelStoreNavEntryDecorator in MyNextFavArtistApp), so a given instance only ever
// serves one artist - it's supplied once, via assisted injection, rather than threaded through
// every action.
@HiltViewModel(assistedFactory = DetailsViewModel.Factory::class)
class DetailsViewModel @AssistedInject constructor(
    @Assisted private val artist: Artist,
    private val observeIsFavorite: ObserveIsFavorite,
    private val saveFavoriteArtist: SaveFavoriteArtist,
    private val removeFavoriteArtist: RemoveFavoriteArtist,
    private val getArtistReleaseGroups: GetArtistReleaseGroups,
) : BaseViewModel<DetailsUiState, DetailsUiEffect>(DetailsUiState()) {

    @AssistedFactory
    interface Factory {
        fun create(artist: Artist): DetailsViewModel
    }

    fun handleAction(action: DetailsUiAction) {
        when (action) {
            DetailsUiAction.LoadArtistDetails -> loadArtistDetails()
            DetailsUiAction.ToggleFavorite -> toggleFavorite()
            DetailsUiAction.OnBackRequest -> navigateBack()
        }
    }

    private fun loadArtistDetails() {
        checkFavoriteStatus()
        loadReleaseGroups()
    }

    private fun checkFavoriteStatus() {
        launchExclusive(Key.FavoriteStatus) {
            observeIsFavorite(artist.mbid).collect { result ->
                result.fold(
                    onSuccess = { isFavorite -> updateState { it.copy(isFavorite = isFavorite) } },
                    onFailure = ::onDBAccessError,
                )
            }
        }
    }

    private fun loadReleaseGroups() {
        updateState { it.copy(releaseGroups = BaseUiState.Loading) }
        launchExclusive(Key.ReleaseGroups) {
            getArtistReleaseGroups(artist.mbid).fold(
                onSuccess = { releaseGroups ->
                    updateState { it.copy(releaseGroups = BaseUiState.Success(releaseGroups)) }
                },
                onFailure = { error ->
                    updateState { it.copy(releaseGroups = BaseUiState.Error(error.asUiText(), error.asUiIcon())) }
                },
            )
        }
    }

    private fun toggleFavorite() {
        updateState { it.copy(isFavoriteActionInProgress = true) }
        if (uiState.value.isFavorite) {
            removeFavorite()
        } else {
            saveFavorite()
        }
    }

    private fun removeFavorite() {
        viewModelScope.launch {
            // isFavorite isn't set here - the observeIsFavorite collector in
            // checkFavoriteStatus already owns it and will pick up the write via Room's
            // change invalidation shortly after.
            removeFavoriteArtist(artist.mbid).fold(
                onSuccess = { updateState { it.copy(isFavoriteActionInProgress = false) } },
                onFailure = ::onFavoriteActionError,
            )
        }
    }

    private fun saveFavorite() {
        viewModelScope.launch {
            saveFavoriteArtist(artist).fold(
                onSuccess = { updateState { it.copy(isFavoriteActionInProgress = false) } },
                onFailure = ::onFavoriteActionError,
            )
        }
    }

    private fun onDBAccessError(error: DataError) {
        sendEffect(DetailsUiEffect.ShowMessage(error.asUiText()))
    }

    private fun onFavoriteActionError(error: DataError) {
        updateState { it.copy(isFavoriteActionInProgress = false) }
        sendEffect(DetailsUiEffect.ShowMessage(error.asUiText()))
    }

    private fun navigateBack() {
        sendEffect(DetailsUiEffect.NavigateBack)
    }

    private enum class Key { FavoriteStatus, ReleaseGroups }
}
