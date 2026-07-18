package com.jrom.mynextfavartist.ui.error

import androidx.annotation.DrawableRes
import com.jrom.mynextfavartist.domain.error.DataError
import com.jrom.mynextfavartist.ui.R

@DrawableRes
fun DataError.asUiIcon(): Int {
    return when (this) {
        is DataError.Network -> R.drawable.ic_warning
        is DataError.Local -> R.drawable.ic_info
    }
}
