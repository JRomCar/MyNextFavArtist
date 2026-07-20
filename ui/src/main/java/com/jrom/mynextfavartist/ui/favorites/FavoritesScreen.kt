package com.jrom.mynextfavartist.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.ui.R
import com.jrom.mynextfavartist.ui.components.ArtistList
import com.jrom.mynextfavartist.ui.components.ErrorView
import com.jrom.mynextfavartist.ui.components.LoadingView
import com.jrom.mynextfavartist.ui.favorites.FavoritesUiAction.ArtistClicked
import com.jrom.mynextfavartist.ui.favorites.FavoritesUiAction.ClearAllSavedArtists
import com.jrom.mynextfavartist.ui.favorites.FavoritesUiAction.LoadArtists
import com.jrom.mynextfavartist.ui.states.BaseUiEffect
import com.jrom.mynextfavartist.ui.states.BaseUiState
import com.jrom.mynextfavartist.ui.utils.Dimensions
import com.jrom.mynextfavartist.ui.utils.collectWithEffect

@Composable
fun FavoritesScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: FavoritesViewModel = hiltViewModel(),
    onDetailClick: (Artist) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val screenDescription = stringResource(R.string.favorites_screen_description)

    LaunchedEffect(Unit) {
        viewModel.handleAction(LoadArtists)
    }

    viewModel.uiEffect.collectWithEffect { effect ->
        when (effect) {
            is BaseUiEffect.NavigateToDetail -> onDetailClick(effect.artist)
        }
    }

    Box(
        modifier = modifier.semantics { contentDescription = screenDescription }
    ) {
        FavoritesContent(
            contentPadding = contentPadding,
            state = uiState,
            onAction = viewModel::handleAction
        )
    }
}

@Composable
fun FavoritesContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    state: BaseUiState<List<Artist>>,
    onAction: (FavoritesUiAction) -> Unit
) {
    when (state) {
        is BaseUiState.Success -> {
            FavoritesListContent(
                modifier = modifier,
                contentPadding = contentPadding,
                artists = state.data,
                onArtistClick = { artist -> onAction(ArtistClicked(artist)) },
                onDeleteAllClick = { onAction(ClearAllSavedArtists) }
            )
        }

        BaseUiState.Loading, BaseUiState.Initial -> LoadingView(modifier = modifier)

        is BaseUiState.Error -> ErrorView(
            modifier = modifier,
            error = state,
            onRetryClick = { onAction(LoadArtists) }
        )

        BaseUiState.Empty -> EmptyFavoritesContent(modifier = modifier)
    }
}

@Composable
private fun EmptyFavoritesContent(
    modifier: Modifier = Modifier,
) {
    val emptyContentDescription = stringResource(R.string.no_favorites_content_description)
    val emptyMessageDescription = stringResource(R.string.empty_favorites_message_description)

    Box(
        modifier = modifier
            .padding(Dimensions.paddingMedium)
            .semantics { contentDescription = emptyContentDescription },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.semantics { contentDescription = emptyMessageDescription }
        ) {
            Text(
                text = stringResource(R.string.no_favorites_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
            Text(
                text = stringResource(R.string.no_favorites_description),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FavoritesListContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    artists: List<Artist>,
    onArtistClick: (Artist) -> Unit,
    onDeleteAllClick: () -> Unit
) {
    val listDescription = stringResource(R.string.favorites_list_description)
    val deleteAllDescription = stringResource(R.string.delete_all_favorites_description)

    Column(
        modifier = modifier.semantics { contentDescription = listDescription }
    ) {
        OutlinedButton(
            onClick = onDeleteAllClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingMedium)
                .semantics { contentDescription = deleteAllDescription },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_delete),
                contentDescription = null // Description handled by button semantics
            )
            Spacer(modifier = Modifier.width(Dimensions.paddingMedium))
            Text(stringResource(R.string.delete_all_favorites))
        }

        ArtistList(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            artists = artists,
            onArtistClick = onArtistClick
        )
    }
}
