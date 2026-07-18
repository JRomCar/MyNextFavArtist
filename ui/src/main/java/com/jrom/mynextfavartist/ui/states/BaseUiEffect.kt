package com.jrom.mynextfavartist.ui.states

import com.jrom.mynextfavartist.domain.entities.Artist

sealed class BaseUiEffect {
    data class NavigateToDetail(val artist: Artist) : BaseUiEffect()
}
