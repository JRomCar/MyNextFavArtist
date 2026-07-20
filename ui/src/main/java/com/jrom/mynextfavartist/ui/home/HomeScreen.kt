package com.jrom.mynextfavartist.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.ui.R
import com.jrom.mynextfavartist.ui.components.ArtistList
import com.jrom.mynextfavartist.ui.components.ErrorView
import com.jrom.mynextfavartist.ui.components.LoadingView
import com.jrom.mynextfavartist.ui.components.PullToRefresh
import com.jrom.mynextfavartist.ui.states.BaseUiEffect
import com.jrom.mynextfavartist.ui.states.BaseUiState
import com.jrom.mynextfavartist.ui.states.HomeUiState
import com.jrom.mynextfavartist.ui.utils.PreviewWrapper
import com.jrom.mynextfavartist.ui.utils.collectWithEffect
import com.jrom.mynextfavartist.ui.utils.previewArtists

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: HomeViewModel = hiltViewModel(),
    onDetailClick: (Artist) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val screenDescription = stringResource(R.string.home_screen_description)

    LaunchedEffect(Unit) {
        viewModel.handleAction(HomeUiAction.LoadArtists)
    }

    viewModel.uiEffect.collectWithEffect { effect ->
        when (effect) {
            is BaseUiEffect.NavigateToDetail -> onDetailClick(effect.artist)
        }
    }

    Box(modifier = modifier.semantics { contentDescription = screenDescription }) {
        HomeContent(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            state = uiState,
            onAction = viewModel::handleAction,
        )
    }
}

@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    state: HomeUiState,
    onAction: (HomeUiAction) -> Unit,
) {
    PullToRefresh(
        modifier = modifier,
        isRefreshing = state.isRefreshing,
        onRefresh = { onAction(HomeUiAction.LoadArtists) },
    ) {
        when (val artists = state.artists) {
            is BaseUiState.Success -> ArtistList(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
                artists = artists.data,
                onArtistClick = { artist -> onAction(HomeUiAction.ArtistClicked(artist)) },
            )

            is BaseUiState.Error -> ErrorView(
                modifier = Modifier.fillMaxSize(),
                error = artists,
                onRetryClick = { onAction(HomeUiAction.LoadArtists) },
            )

            BaseUiState.Loading, BaseUiState.Initial, BaseUiState.Empty ->
                LoadingView(modifier = Modifier.fillMaxSize())
        }
    }
}

@Preview(showSystemUi = true, name = "Light")
@Preview(showSystemUi = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeContentPreview() {
    PreviewWrapper {
        HomeContent(
            state = HomeUiState(artists = BaseUiState.Success(previewArtists)),
            onAction = {},
        )
    }
}

@Preview(showSystemUi = true, name = "Light")
@Preview(showSystemUi = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeContentLoadingPreview() {
    PreviewWrapper {
        HomeContent(
            state = HomeUiState(artists = BaseUiState.Loading),
            onAction = {},
        )
    }
}
