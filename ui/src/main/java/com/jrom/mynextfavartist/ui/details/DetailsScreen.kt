package com.jrom.mynextfavartist.ui.details

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.entities.ReleaseGroup
import com.jrom.mynextfavartist.ui.R
import com.jrom.mynextfavartist.ui.components.AlbumArtCard
import com.jrom.mynextfavartist.ui.components.ArtistAvatar
import com.jrom.mynextfavartist.ui.components.ErrorView
import com.jrom.mynextfavartist.ui.components.LoadingView
import com.jrom.mynextfavartist.ui.states.BaseUiState
import com.jrom.mynextfavartist.ui.states.DetailsUiState
import com.jrom.mynextfavartist.ui.utils.AccessibilityUtils
import com.jrom.mynextfavartist.ui.utils.Dimensions
import com.jrom.mynextfavartist.ui.utils.PreviewWrapper
import com.jrom.mynextfavartist.ui.utils.collectWithEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: DetailsViewModel = hiltViewModel(),
    artist: Artist,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val screenDescription = AccessibilityUtils.getArtistDetailsScreenDescription(artist.name)
    val backButtonDescription = AccessibilityUtils.getBackButtonDescription()

    LaunchedEffect(artist) {
        viewModel.handleAction(DetailsUiAction.LoadArtistDetails(artist))
    }

    viewModel.uiEffect.collectWithEffect { effect ->
        when (effect) {
            is DetailsUiEffect.NavigateBack -> onBackClick()
            is DetailsUiEffect.ShowMessage -> {
                snackbarHostState.showSnackbar(effect.message.asString(context))
            }
        }
    }

    Box(
        modifier = modifier.semantics { contentDescription = screenDescription }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = artist.name,
                        modifier = Modifier.semantics { heading() }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.handleAction(DetailsUiAction.OnBackRequest) },
                        modifier = Modifier.semantics { contentDescription = backButtonDescription }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back_button)
                        )
                    }
                }
            )
            DetailsContent(
                modifier = Modifier.weight(1f),
                artist = artist,
                state = uiState,
                onAction = viewModel::handleAction
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun DetailsContent(
    modifier: Modifier = Modifier,
    artist: Artist,
    state: DetailsUiState,
    onAction: (DetailsUiAction) -> Unit,
) {
    val contentDescription = AccessibilityUtils.getArtistDetailsContentDescription(artist)
    val nameHeadingDescription = AccessibilityUtils.getArtistNameHeadingDescription(artist.name)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimensions.paddingLarge)
            .semantics { this.contentDescription = contentDescription },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ArtistAvatar(
            modifier = Modifier.padding(bottom = Dimensions.paddingXXL),
            artistName = artist.name,
            size = Dimensions.imageSizeLarge,
        )

        Text(
            text = artist.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(bottom = Dimensions.paddingXXL)
                .semantics {
                    heading()
                    this.contentDescription = nameHeadingDescription
                }
        )

        ArtistDetailsCard(artist = artist)

        LikeButton(
            isFavorite = state.isFavorite,
            isLoading = state.isFavoriteActionInProgress,
            onClick = { onAction(DetailsUiAction.ToggleFavorite(artist)) }
        )

        AlbumsSection(
            modifier = Modifier.padding(top = Dimensions.paddingXL),
            releaseGroups = state.releaseGroups,
            onRetryClick = { onAction(DetailsUiAction.LoadArtistDetails(artist)) },
        )
    }
}

@Composable
private fun ArtistDetailsCard(
    modifier: Modifier = Modifier,
    artist: Artist,
) {
    val cardDescription = AccessibilityUtils.getArtistDetailsCardDescription()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = Dimensions.paddingXL)
            .semantics { contentDescription = cardDescription },
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevationDefault),
        shape = RoundedCornerShape(Dimensions.cardCornerRadius)
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.paddingXL),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
        ) {
            Text(
                text = stringResource(R.string.artist_details_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics { heading() }
            )

            DetailRow(
                label = stringResource(R.string.type_detail_label),
                value = artist.type ?: stringResource(R.string.unknown_artist_type)
            )

            artist.country?.let { country ->
                DetailRow(
                    label = stringResource(R.string.country_detail_label),
                    value = country
                )
            }

            artist.disambiguation?.takeIf { it.isNotBlank() }?.let { disambiguation ->
                DetailRow(
                    label = stringResource(R.string.disambiguation_detail_label),
                    value = disambiguation
                )
            }
        }
    }
}

@Composable
private fun LikeButton(
    modifier: Modifier = Modifier,
    isFavorite: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val buttonDescription = AccessibilityUtils.getFavoriteButtonDescription(isFavorite, isLoading)

    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = buttonDescription },
        shape = RoundedCornerShape(Dimensions.buttonCornerRadius)
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
        } else {
            Icon(
                painter = painterResource(if (isFavorite) R.drawable.ic_check else R.drawable.ic_add),
                contentDescription = null, // Icon description handled by button contentDescription
            )
            Spacer(modifier = Modifier.width(Dimensions.spacingMedium))
            Text(stringResource(if (isFavorite) R.string.liked_artist_button else R.string.like_artist_button))
        }
    }
}

@Composable
private fun AlbumsSection(
    modifier: Modifier = Modifier,
    releaseGroups: BaseUiState<List<ReleaseGroup>>,
    onRetryClick: () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.albums_section_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(bottom = Dimensions.paddingLarge)
                .semantics { heading() }
        )

        when (releaseGroups) {
            is BaseUiState.Success -> {
                if (releaseGroups.data.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_albums_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.listItemSpacing)) {
                        releaseGroups.data.forEach { releaseGroup ->
                            ReleaseGroupRow(releaseGroup = releaseGroup)
                        }
                    }
                }
            }

            is BaseUiState.Error -> ErrorView(error = releaseGroups, onRetryClick = onRetryClick)
            BaseUiState.Loading, BaseUiState.Initial -> LoadingView()
        }
    }
}

@Composable
private fun ReleaseGroupRow(
    modifier: Modifier = Modifier,
    releaseGroup: ReleaseGroup,
) {
    val rowDescription = AccessibilityUtils.getReleaseGroupContentDescription(releaseGroup)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = rowDescription },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlbumArtCard(
            releaseGroupMbid = releaseGroup.mbid,
            albumTitle = releaseGroup.title,
        )

        Spacer(modifier = Modifier.width(Dimensions.spacingLarge))

        Column {
            Text(
                text = releaseGroup.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val subtitle = listOfNotNull(releaseGroup.primaryType, releaseGroup.firstReleaseDate)
                .joinToString(separator = " · ")
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = "$label $value" },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Preview(showSystemUi = true, name = "Light")
@Preview(showSystemUi = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetailsContentPreview() {
    PreviewWrapper {
        DetailsContent(
            artist = Artist(
                mbid = "a74b1b7f-71a5-4011-9441-d0b5e4122711",
                name = "Radiohead",
                type = "Group",
                country = "GB",
                disambiguation = null,
            ),
            state = DetailsUiState(
                isFavorite = false,
                releaseGroups = BaseUiState.Success(
                    listOf(
                        ReleaseGroup(
                            mbid = "b1392450-e666-3926-a536-22c65f834433",
                            title = "OK Computer",
                            primaryType = "Album",
                            firstReleaseDate = "1997-05-21",
                        )
                    )
                )
            ),
            onAction = { }
        )
    }
}
