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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val observeIsFavorite: ObserveIsFavorite,
    private val saveFavoriteArtist: SaveFavoriteArtist,
    private val removeFavoriteArtist: RemoveFavoriteArtist,
    private val getArtistReleaseGroups: GetArtistReleaseGroups,
    initialState: DetailsUiState = DetailsUiState(),
) : BaseViewModel<DetailsUiState, DetailsUiEffect>(initialState) {

    private var favoriteStatusJob: Job? = null
    private var releaseGroupsJob: Job? = null

    fun handleAction(action: DetailsUiAction) {
        when (action) {
            is DetailsUiAction.LoadArtistDetails -> loadArtistDetails(action.artist)
            is DetailsUiAction.ToggleFavorite -> toggleFavorite(action.artist)
            DetailsUiAction.OnBackRequest -> navigateBack()
        }
    }

    private fun loadArtistDetails(artist: Artist) {
        checkFavoriteStatus(artist.mbid)
        loadReleaseGroups(artist.mbid)
    }

    private fun checkFavoriteStatus(artistMbid: String) {
        favoriteStatusJob?.cancel()
        favoriteStatusJob = viewModelScope.launch {
            observeIsFavorite(artistMbid).collect { result ->
                result.fold(
                    onSuccess = { isFavorite -> updateState { it.copy(isFavorite = isFavorite) } },
                    onFailure = ::onDBAccessError,
                )
            }
        }
    }

    private fun loadReleaseGroups(artistMbid: String) {
        updateState { it.copy(releaseGroups = BaseUiState.Loading) }
        releaseGroupsJob?.cancel()
        releaseGroupsJob = viewModelScope.launch {
            getArtistReleaseGroups(artistMbid).fold(
                onSuccess = { releaseGroups ->
                    updateState { it.copy(releaseGroups = BaseUiState.Success(releaseGroups)) }
                },
                onFailure = { error ->
                    updateState { it.copy(releaseGroups = BaseUiState.Error(error.asUiText(), error.asUiIcon())) }
                },
            )
        }
    }

    private fun toggleFavorite(artist: Artist) {
        updateState { it.copy(isFavoriteActionInProgress = true) }
        if (uiState.value.isFavorite) {
            removeFavorite(artist)
        } else {
            saveFavorite(artist)
        }
    }

    private fun removeFavorite(artist: Artist) {
        viewModelScope.launch {
            removeFavoriteArtist(artist.mbid).fold(
                onSuccess = { updateState { it.copy(isFavorite = false, isFavoriteActionInProgress = false) } },
                onFailure = ::onFavoriteActionError,
            )
        }
    }

    private fun saveFavorite(artist: Artist) {
        viewModelScope.launch {
            saveFavoriteArtist(artist).fold(
                onSuccess = { updateState { it.copy(isFavorite = true, isFavoriteActionInProgress = false) } },
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
}
