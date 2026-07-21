package com.jrom.mynextfavartist.ui.error

import androidx.annotation.DrawableRes
import com.jrom.mynextfavartist.domain.error.DataError
import com.jrom.mynextfavartist.ui.R

/**
 * Same purpose as [asUiText] but for the icon shown alongside it in
 * [com.jrom.mynextfavartist.ui.states.BaseUiState.Error].
 */
@DrawableRes
fun DataError.asUiIcon(): Int {
    return when (this) {
        is DataError.Network -> R.drawable.ic_warning
        is DataError.Local -> R.drawable.ic_info
    }
}
