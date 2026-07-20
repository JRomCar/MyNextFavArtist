package com.jrom.mynextfavartist.ui.favorites

import androidx.lifecycle.viewModelScope
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.error.DataError
import com.jrom.mynextfavartist.domain.fold
import com.jrom.mynextfavartist.domain.usecase.ObserveFavoriteArtists
import com.jrom.mynextfavartist.domain.usecase.RemoveAllFavoriteArtists
import com.jrom.mynextfavartist.ui.error.asUiIcon
import com.jrom.mynextfavartist.ui.error.asUiText
import com.jrom.mynextfavartist.ui.states.BaseUiEffect
import com.jrom.mynextfavartist.ui.states.BaseUiState
import com.jrom.mynextfavartist.ui.states.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val observeFavoriteArtists: ObserveFavoriteArtists,
    private val removeAllFavoriteArtists: RemoveAllFavoriteArtists,
) : BaseViewModel<BaseUiState<List<Artist>>, BaseUiEffect>(BaseUiState.Initial) {

    fun handleAction(action: FavoritesUiAction) {
        when (action) {
            is FavoritesUiAction.ClearAllSavedArtists -> removeAllFavorites()
            is FavoritesUiAction.ArtistClicked -> onArtistClicked(action.artist)
            FavoritesUiAction.LoadArtists -> loadFavoriteArtists()
        }
    }

    private fun onArtistClicked(artist: Artist) {
        sendEffect(BaseUiEffect.NavigateToDetail(artist))
    }

    private fun loadFavoriteArtists() {
        setState(BaseUiState.Loading)
        // Cancel-and-relaunch so the screen's initial load and a manual retry after an
        // error never stack multiple collectors on the same observeFavoriteArtists() flow.
        launchExclusive(Key.Favorites) {
            observeFavoriteArtists().collect { result ->
                result.fold(
                    onSuccess = { artists ->
                        // An empty list is BaseUiState.Empty ("loaded, zero results"), not
                        // Initial ("not loaded yet"), so EmptyFavoritesContent renders instead
                        // of a bare "Delete All Favorites" button over nothing.
                        setState(if (artists.isEmpty()) BaseUiState.Empty else BaseUiState.Success(artists))
                    },
                    onFailure = ::onDBAccessError,
                )
            }
        }
    }

    private fun removeAllFavorites() {
        setState(BaseUiState.Loading)
        viewModelScope.launch {
            removeAllFavoriteArtists().fold(
                onSuccess = { setState(BaseUiState.Empty) },
                onFailure = ::onDBAccessError,
            )
        }
    }

    private fun onDBAccessError(error: DataError) {
        setState(BaseUiState.Error(error.asUiText(), error.asUiIcon()))
    }

    private enum class Key { Favorites }
}
