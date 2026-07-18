package com.jrom.mynextfavartist.ui.favorites

import androidx.lifecycle.viewModelScope
import com.jrom.mynextfavartist.domain.Result
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.error.DataError
import com.jrom.mynextfavartist.domain.usecase.GetAllFavoriteArtists
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
    private val getAllFavoriteArtists: GetAllFavoriteArtists,
    private val removeAllFavoriteArtists: RemoveAllFavoriteArtists,
    initialState: @JvmSuppressWildcards BaseUiState<List<Artist>> = BaseUiState.Initial,
) : BaseViewModel<BaseUiState<List<Artist>>, BaseUiEffect>(initialState) {

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
        viewModelScope.launch {
            when (val result = getAllFavoriteArtists()) {
                is Result.Success -> setState(BaseUiState.Success(result.data))
                is Result.Error -> onDBAccessError(result.error)
            }
        }
    }

    private fun removeAllFavorites() {
        setState(BaseUiState.Loading)
        viewModelScope.launch {
            when (val result = removeAllFavoriteArtists()) {
                is Result.Success -> {
                    if (result.data) {
                        setState(BaseUiState.Initial)
                    } else onDBAccessError(DataError.Local.DB_WRITE_ERROR)
                }

                is Result.Error -> onDBAccessError(result.error)
            }
        }
    }

    private fun onDBAccessError(error: DataError) {
        setState(BaseUiState.Error(error.asUiText(), error.asUiIcon()))
    }
}
