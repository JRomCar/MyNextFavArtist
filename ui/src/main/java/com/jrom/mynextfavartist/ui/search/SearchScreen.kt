package com.jrom.mynextfavartist.ui.search

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.error.DataError
import com.jrom.mynextfavartist.ui.R
import com.jrom.mynextfavartist.ui.components.ArtistList
import com.jrom.mynextfavartist.ui.components.ArtistListSkeleton
import com.jrom.mynextfavartist.ui.components.EmptyStateView
import com.jrom.mynextfavartist.ui.components.ErrorView
import com.jrom.mynextfavartist.ui.components.SearchView
import com.jrom.mynextfavartist.ui.components.SectionHeader
import com.jrom.mynextfavartist.ui.error.asUiIcon
import com.jrom.mynextfavartist.ui.error.asUiText
import com.jrom.mynextfavartist.ui.states.BaseUiEffect
import com.jrom.mynextfavartist.ui.states.BaseUiState
import com.jrom.mynextfavartist.ui.utils.PreviewWrapper
import com.jrom.mynextfavartist.ui.utils.collectWithEffect
import com.jrom.mynextfavartist.ui.utils.previewArtists

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: SearchViewModel = hiltViewModel(),
    onDetailClick: (Artist) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    viewModel.uiEffect.collectWithEffect { effect ->
        when (effect) {
            is BaseUiEffect.NavigateToDetail -> onDetailClick(effect.artist)
        }
    }

    SearchContent(
        modifier = modifier,
        contentPadding = contentPadding,
        state = uiState,
        onAction = viewModel::handleAction
    )
}

@Composable
fun SearchContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    state: BaseUiState<List<Artist>>,
    onAction: (SearchUiAction) -> Unit,
) {
    val screenDescription = stringResource(R.string.search_screen_description)
    val bottomPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding())

    Box(
        modifier = modifier.semantics { contentDescription = screenDescription }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Only the top inset is consumed here; the bottom one is handed to whichever
                // list or empty state is showing so it can scroll under the navigation bar.
                .padding(top = contentPadding.calculateTopPadding())
                .imePadding()
        ) {
            SearchView(onQueryChange = { query ->
                onAction(SearchUiAction.SearchRequest(query))
            })

            when (state) {
                BaseUiState.Initial, BaseUiState.Empty -> EmptyStateView(
                    modifier = Modifier.fillMaxSize(),
                    icon = R.drawable.ic_search,
                    title = stringResource(R.string.search_prompt_title),
                    description = stringResource(R.string.search_prompt_description),
                )

                BaseUiState.Loading -> ArtistListSkeleton(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = bottomPadding,
                )

                is BaseUiState.Error -> ErrorView(modifier = Modifier.fillMaxSize(), error = state)

                // A query that matched nothing still arrives as Success, so the "no results"
                // message lives here rather than in a dedicated state.
                is BaseUiState.Success -> if (state.data.isEmpty()) {
                    EmptyStateView(
                        modifier = Modifier.fillMaxSize(),
                        icon = R.drawable.ic_search,
                        title = stringResource(R.string.search_no_results_title),
                        description = stringResource(R.string.search_no_results_description),
                    )
                } else {
                    ArtistList(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = bottomPadding,
                        artists = state.data,
                        header = {
                            SectionHeader(
                                title = stringResource(R.string.search_results_title),
                                subtitle = pluralStringResource(
                                    R.plurals.search_results_count,
                                    state.data.size,
                                    state.data.size,
                                ),
                            )
                        },
                        onArtistClick = { artist -> onAction(SearchUiAction.ArtistClicked(artist)) }
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true, name = "Light")
@Preview(showSystemUi = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SearchContentPreview() {
    PreviewWrapper {
        SearchContent(
            state = BaseUiState.Success(previewArtists),
            onAction = {})
    }
}

@Preview(showSystemUi = true, name = "Light")
@Preview(showSystemUi = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ErrorSearchContentPreview() {
    PreviewWrapper {
        val error = DataError.Network.UNKNOWN
        SearchContent(
            state = BaseUiState.Error(error.asUiText(), error.asUiIcon()),
            onAction = {})
    }
}

@Preview(showSystemUi = true, name = "Light")
@Preview(showSystemUi = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptySearchContentPreview() {
    PreviewWrapper {
        SearchContent(
            state = BaseUiState.Initial,
            onAction = {})
    }
}

@Preview(showSystemUi = true, name = "Light")
@Preview(showSystemUi = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoadingSearchContentPreview() {
    PreviewWrapper {
        SearchContent(
            state = BaseUiState.Loading,
            onAction = {})
    }
}
