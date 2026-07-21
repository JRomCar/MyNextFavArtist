package com.jrom.mynextfavartist.ui.favorites

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
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
import com.jrom.mynextfavartist.ui.components.ArtistListSkeleton
import com.jrom.mynextfavartist.ui.components.EmptyStateView
import com.jrom.mynextfavartist.ui.components.ErrorView
import com.jrom.mynextfavartist.ui.components.SectionHeader
import com.jrom.mynextfavartist.ui.favorites.FavoritesUiAction.ArtistClicked
import com.jrom.mynextfavartist.ui.favorites.FavoritesUiAction.ClearAllSavedArtists
import com.jrom.mynextfavartist.ui.favorites.FavoritesUiAction.LoadArtists
import com.jrom.mynextfavartist.ui.states.BaseUiEffect
import com.jrom.mynextfavartist.ui.states.BaseUiState
import com.jrom.mynextfavartist.ui.utils.Dimensions
import com.jrom.mynextfavartist.ui.utils.PreviewWrapper
import com.jrom.mynextfavartist.ui.utils.collectWithEffect
import com.jrom.mynextfavartist.ui.utils.previewArtists

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
        is BaseUiState.Success -> FavoritesListContent(
            modifier = modifier,
            contentPadding = contentPadding,
            artists = state.data,
            onArtistClick = { artist -> onAction(ArtistClicked(artist)) },
            onDeleteAllConfirmed = { onAction(ClearAllSavedArtists) }
        )

        BaseUiState.Loading, BaseUiState.Initial -> ArtistListSkeleton(
            modifier = modifier.fillMaxSize(),
            contentPadding = contentPadding,
        )

        is BaseUiState.Error -> ErrorView(
            modifier = modifier.fillMaxSize(),
            error = state,
            onRetryClick = { onAction(LoadArtists) }
        )

        BaseUiState.Empty -> EmptyStateView(
            modifier = modifier.fillMaxSize(),
            icon = R.drawable.ic_favorite,
            title = stringResource(R.string.no_favorites_title),
            description = stringResource(R.string.no_favorites_description),
        )
    }
}

@Composable
private fun FavoritesListContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    artists: List<Artist>,
    onArtistClick: (Artist) -> Unit,
    onDeleteAllConfirmed: () -> Unit
) {
    val listDescription = stringResource(R.string.favorites_list_description)
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    ArtistList(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = listDescription },
        contentPadding = contentPadding,
        artists = artists,
        header = {
            SectionHeader(
                title = stringResource(R.string.favorites_header_title),
                subtitle = pluralStringResource(
                    R.plurals.favorites_count,
                    artists.size,
                    artists.size,
                ),
                trailing = { DeleteAllButton(onClick = { showDeleteAllDialog = true }) },
            )
        },
        onArtistClick = onArtistClick,
    )

    // Clearing every favourite is destructive and unrecoverable - the previous full-width
    // "Delete All Favorites" button fired straight through on a single tap.
    if (showDeleteAllDialog) {
        DeleteAllDialog(
            onConfirm = {
                showDeleteAllDialog = false
                onDeleteAllConfirmed()
            },
            onDismiss = { showDeleteAllDialog = false },
        )
    }
}

@Composable
private fun DeleteAllButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val deleteAllDescription = stringResource(R.string.delete_all_favorites_description)

    IconButton(
        onClick = onClick,
        modifier = modifier.semantics { contentDescription = deleteAllDescription },
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_delete),
            contentDescription = null,
            modifier = Modifier.size(Dimensions.iconSizeMedium),
            tint = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun DeleteAllDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = painterResource(R.drawable.ic_delete),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        },
        title = { Text(stringResource(R.string.delete_all_favorites_dialog_title)) },
        text = { Text(stringResource(R.string.delete_all_favorites_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.delete_all_favorites_confirm),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Preview(showSystemUi = true, name = "Light")
@Preview(showSystemUi = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FavoritesContentPreview() {
    PreviewWrapper {
        FavoritesContent(
            state = BaseUiState.Success(previewArtists),
            onAction = {},
        )
    }
}

@Preview(showSystemUi = true, name = "Light")
@Preview(showSystemUi = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FavoritesEmptyPreview() {
    PreviewWrapper {
        FavoritesContent(
            state = BaseUiState.Empty,
            onAction = {},
        )
    }
}
