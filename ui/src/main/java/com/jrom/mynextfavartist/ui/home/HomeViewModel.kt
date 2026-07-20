package com.jrom.mynextfavartist.ui.home

import androidx.lifecycle.viewModelScope
import com.jrom.mynextfavartist.domain.Result
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.usecase.GetHomeArtists
import com.jrom.mynextfavartist.ui.error.asUiIcon
import com.jrom.mynextfavartist.ui.error.asUiText
import com.jrom.mynextfavartist.ui.states.BaseUiEffect
import com.jrom.mynextfavartist.ui.states.BaseUiState
import com.jrom.mynextfavartist.ui.states.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeArtists: GetHomeArtists,
    // BaseUiState's declaration-site `out T` makes Dagger see the injected type as
    // BaseUiState<? extends List<Artist>>, which doesn't match the concrete
    // BaseUiState<List<Artist>> binding in UiStateModule without this annotation.
    initialState: @JvmSuppressWildcards BaseUiState<List<Artist>> = BaseUiState.Initial,
) : BaseViewModel<BaseUiState<List<Artist>>, BaseUiEffect>(initialState) {

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
        val cachedArtists = uiState.value as? BaseUiState.Success<List<Artist>>
        // Every navigation back to Home re-triggers this load. If we already have a list on
        // screen, keep showing it during the fetch and on failure - MusicBrainz has transient
        // outages, and re-showing a stale list beats replacing a working screen with an error.
        if (cachedArtists == null) {
            setState(BaseUiState.Loading)
        }
        viewModelScope.launch {
            when (val result = getHomeArtists()) {
                is Result.Success -> setState(BaseUiState.Success(result.data))
                is Result.Error -> {
                    if (cachedArtists == null) {
                        val error = result.error
                        setState(BaseUiState.Error(error.asUiText(), error.asUiIcon()))
                    }
                }
            }
        }
    }
}
