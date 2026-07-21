package com.jrom.mynextfavartist.ui.states

import androidx.annotation.DrawableRes
import com.jrom.mynextfavartist.ui.error.UiText

/**
 * One field standing in for what would otherwise be several independent booleans/nullables
 * (isLoading, error, data) on every screen's state - which allows invalid combinations (loading
 * *and* showing stale data, an error with no message) that this sealed hierarchy can't express.
 * A `when` over it is exhaustive, so the compiler catches an unhandled case at every call site.
 * Generic over its success payload [T] since this app has two independently-loading lists
 * (artists on Home/Search/Favorites, an artist's release groups on Details).
 */
sealed class BaseUiState<out T> {
    data object Initial : BaseUiState<Nothing>()
    data object Loading : BaseUiState<Nothing>()
    data object Empty : BaseUiState<Nothing>()
    data class Error(val errorText: UiText, @DrawableRes val errorIcon: Int) : BaseUiState<Nothing>()
    data class Success<T>(val data: T) : BaseUiState<T>()
}
