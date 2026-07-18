package com.jrom.mynextfavartist.ui.states

import androidx.annotation.DrawableRes
import com.jrom.mynextfavartist.ui.error.UiText

/**
 * Generic over its success payload [T] since this app has two independently-loading lists
 * (artists on Home/Search/Favorites, an artist's release groups on Details) - unlike
 * PlanetFinder, which only ever listed one entity type and could hardcode
 * `Success(val planets: List<Planet>)`.
 */
sealed class BaseUiState<out T> {
    data object Initial : BaseUiState<Nothing>()
    data object Loading : BaseUiState<Nothing>()
    data class Error(val errorText: UiText, @DrawableRes val errorIcon: Int) : BaseUiState<Nothing>()
    data class Success<T>(val data: T) : BaseUiState<T>()
}

val <T> BaseUiState<T>.isLoading: Boolean
    get() = this is BaseUiState.Loading
