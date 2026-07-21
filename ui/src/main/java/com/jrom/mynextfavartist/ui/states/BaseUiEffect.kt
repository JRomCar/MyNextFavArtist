package com.jrom.mynextfavartist.ui.states

import com.jrom.mynextfavartist.domain.entities.Artist

/**
 * One-time events a screen's [BaseUiState] can't represent: by the time navigation happens,
 * the state that triggered it may already be gone (e.g. Details clears on back). Sent through
 * BaseViewModel's Channel rather than folded into state, so it fires once and isn't replayed.
 */
sealed class BaseUiEffect {
    data class NavigateToDetail(val artist: Artist) : BaseUiEffect()
}
