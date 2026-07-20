package com.jrom.mynextfavartist.ui.home

import androidx.lifecycle.viewModelScope
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.fold
import com.jrom.mynextfavartist.domain.usecase.GetHomeArtists
import com.jrom.mynextfavartist.ui.error.asUiIcon
import com.jrom.mynextfavartist.ui.error.asUiText
import com.jrom.mynextfavartist.ui.states.BaseUiEffect
import com.jrom.mynextfavartist.ui.states.BaseUiState
import com.jrom.mynextfavartist.ui.states.BaseViewModel
import com.jrom.mynextfavartist.ui.states.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeArtists: GetHomeArtists,
    initialState: HomeUiState = HomeUiState(),
) : BaseViewModel<HomeUiState, BaseUiEffect>(initialState) {

    private var loadHomeArtistsJob: Job? = null

    fun handleAction(action: HomeUiAction) {
        when (action) {
            is HomeUiAction.ArtistClicked -> onArtistClicked(action.artist)
            HomeUiAction.LoadArtists -> loadHomeArtists()
        }
    }

    private fun onArtistClicked(artist: Artist) {
        sendEffect(BaseUiEffect.NavigateToDetail(artist))
    }

    private fun loadHomeArtists() {
        val cachedArtists = uiState.value.artists as? BaseUiState.Success<List<Artist>>
        // Every navigation back to Home re-triggers this load. If we already have a list on
        // screen, keep showing it during the fetch and on failure - MusicBrainz has transient
        // outages, and re-showing a stale list beats replacing a working screen with an error.
        // isRefreshing is set regardless, so PullToRefresh's indicator reflects the in-flight
        // request even when the cached list means `artists` itself doesn't move to Loading.
        updateState {
            it.copy(
                artists = if (cachedArtists == null) BaseUiState.Loading else it.artists,
                isRefreshing = true,
            )
        }
        // Cancel-and-relaunch so pull-to-refresh/retry never stack concurrent requests
        // against a 1 req/sec API with last-writer-wins.
        loadHomeArtistsJob?.cancel()
        loadHomeArtistsJob = viewModelScope.launch {
            getHomeArtists().fold(
                onSuccess = { artists ->
                    updateState { it.copy(artists = BaseUiState.Success(artists), isRefreshing = false) }
                },
                onFailure = { error ->
                    updateState {
                        it.copy(
                            artists = if (cachedArtists == null) {
                                BaseUiState.Error(error.asUiText(), error.asUiIcon())
                            } else {
                                it.artists
                            },
                            isRefreshing = false,
                        )
                    }
                },
            )
        }
    }
}
