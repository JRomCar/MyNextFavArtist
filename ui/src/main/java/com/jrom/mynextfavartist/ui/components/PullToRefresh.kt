package com.jrom.mynextfavartist.ui.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Thin wrapper around Material3's native PullToRefreshBox - PlanetFinder used the older,
 * experimental Material2 `androidx.compose.material.pullrefresh` API (requiring the separate
 * Material2 dependency); this Compose BOM ships a stable Material3 equivalent, so there's no
 * reason to pull in Material2 just to port the older API.
 */
@Composable
fun PullToRefresh(
    modifier: Modifier = Modifier,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    PullToRefreshBox(
        modifier = modifier,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        content = content,
    )
}
