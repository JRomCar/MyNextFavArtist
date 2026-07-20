package com.jrom.mynextfavartist.ui.search

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.error.DataError
import com.jrom.mynextfavartist.ui.R
import com.jrom.mynextfavartist.ui.components.ArtistList
import com.jrom.mynextfavartist.ui.components.ErrorView
import com.jrom.mynextfavartist.ui.components.LoadingView
import com.jrom.mynextfavartist.ui.components.SearchView
import com.jrom.mynextfavartist.ui.error.asUiIcon
import com.jrom.mynextfavartist.ui.error.asUiText
import com.jrom.mynextfavartist.ui.states.BaseUiEffect
import com.jrom.mynextfavartist.ui.states.BaseUiState
import com.jrom.mynextfavartist.ui.utils.Dimensions
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

    Box(
        modifier = modifier.semantics { contentDescription = screenDescription }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            SearchView(onQueryChange = { query ->
                onAction(SearchUiAction.SearchRequest(query))
            })

            when (state) {
                BaseUiState.Initial, BaseUiState.Empty -> SearchPromptContent(modifier = Modifier.fillMaxSize())
                BaseUiState.Loading -> LoadingView(modifier = Modifier.fillMaxSize())
                is BaseUiState.Error -> ErrorView(modifier = Modifier.fillMaxSize(), error = state)
                is BaseUiState.Success -> ArtistList(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding()),
                    artists = state.data,
                    onArtistClick = { artist -> onAction(SearchUiAction.ArtistClicked(artist)) }
                )
            }
        }
    }
}

@Composable
private fun SearchPromptContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(Dimensions.paddingXL),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
                modifier = Modifier.padding(bottom = Dimensions.paddingLarge),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.search_prompt_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showSystemUi = true, name = "Light")
@Preview(showSystemUi = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SearchContentPreview() {
    PreviewWrapper {
        SearchContent(
            state = BaseUiState.Success(previewArtists),
            onAction = {})
    }
}

@Preview(showSystemUi = true, name = "Light")
@Preview(showSystemUi = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ErrorSearchContentPreview() {
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
fun EmptySearchContentPreview() {
    PreviewWrapper {
        SearchContent(
            state = BaseUiState.Initial,
            onAction = {})
    }
}

@Preview(showSystemUi = true, name = "Light")
@Preview(showSystemUi = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LoadingSearchContentPreview() {
    PreviewWrapper {
        SearchContent(
            state = BaseUiState.Loading,
            onAction = {})
    }
}
