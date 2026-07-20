package com.jrom.mynextfavartist.ui.states

import com.jrom.mynextfavartist.domain.entities.Artist

/**
 * isRefreshing is tracked separately from [artists] because loadHomeArtists deliberately
 * skips setting Loading when a cached list is already on screen (stale-list preservation
 * on refresh/retry) - PullToRefresh still needs its own signal to know a refresh is in
 * flight, since state staying Success no longer implies "not refreshing".
 */
data class HomeUiState(
    val artists: BaseUiState<List<Artist>> = BaseUiState.Initial,
    val isRefreshing: Boolean = false,
)
