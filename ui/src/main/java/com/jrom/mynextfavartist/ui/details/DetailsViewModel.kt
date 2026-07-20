package com.jrom.mynextfavartist.ui.details

import androidx.lifecycle.viewModelScope
import com.jrom.mynextfavartist.domain.Result
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.error.DataError
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
                when (result) {
                    is Result.Success -> updateState { it.copy(isFavorite = result.data) }
                    is Result.Error -> onDBAccessError(result.error)
                }
            }
        }
    }

    private fun loadReleaseGroups(artistMbid: String) {
        updateState { it.copy(releaseGroups = BaseUiState.Loading) }
        viewModelScope.launch {
            when (val result = getArtistReleaseGroups(artistMbid)) {
                is Result.Success -> updateState { it.copy(releaseGroups = BaseUiState.Success(result.data)) }
                is Result.Error -> {
                    val error = result.error
                    updateState { it.copy(releaseGroups = BaseUiState.Error(error.asUiText(), error.asUiIcon())) }
                }
            }
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
            when (val result = removeFavoriteArtist(artist.mbid)) {
                is Result.Success -> {
                    if (result.data) {
                        updateState { it.copy(isFavorite = false, isFavoriteActionInProgress = false) }
                    } else {
                        onFavoriteActionError(DataError.Local.DB_WRITE_ERROR)
                    }
                }

                is Result.Error -> onFavoriteActionError(result.error)
            }
        }
    }

    private fun saveFavorite(artist: Artist) {
        viewModelScope.launch {
            when (val result = saveFavoriteArtist(artist)) {
                is Result.Success -> {
                    if (result.data) {
                        updateState { it.copy(isFavorite = true, isFavoriteActionInProgress = false) }
                    } else {
                        onFavoriteActionError(DataError.Local.DB_WRITE_ERROR)
                    }
                }

                is Result.Error -> onFavoriteActionError(result.error)
            }
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
