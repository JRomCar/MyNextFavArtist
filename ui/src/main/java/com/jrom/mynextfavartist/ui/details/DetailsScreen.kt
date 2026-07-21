package com.jrom.mynextfavartist.ui.details

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.entities.ReleaseGroup
import com.jrom.mynextfavartist.ui.R
import com.jrom.mynextfavartist.ui.components.AlbumArtCard
import com.jrom.mynextfavartist.ui.components.ErrorView
import com.jrom.mynextfavartist.ui.components.LoadingView
import com.jrom.mynextfavartist.ui.components.SectionHeader
import com.jrom.mynextfavartist.ui.components.rememberArtistBrush
import com.jrom.mynextfavartist.ui.states.BaseUiState
import com.jrom.mynextfavartist.ui.states.DetailsUiState
import com.jrom.mynextfavartist.ui.utils.AccessibilityUtils
import com.jrom.mynextfavartist.ui.utils.Dimensions
import com.jrom.mynextfavartist.ui.utils.PreviewWrapper
import com.jrom.mynextfavartist.ui.utils.collectWithEffect

// Backs the floating back button so it stays legible over any of the artist gradients.
private const val SCRIM_ALPHA = 0.35f

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
    val listState = rememberLazyListState()

    val screenDescription = stringResource(R.string.artist_details_screen_description, artist.name)
    val backButtonDescription = stringResource(R.string.back_button_description)

    // Once the hero (item 0) has scrolled away there is nothing dark behind the bar, so it takes
    // on a solid surface and picks up the artist's name - otherwise the back button and the
    // scrolling content would overlap each other under the status bar. derivedStateOf keeps this
    // from recomposing on every scroll pixel.
    val isCollapsed by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val barColor by animateColorAsState(
        targetValue = if (isCollapsed) {
            MaterialTheme.colorScheme.surface
        } else {
            Color.Transparent
        },
        label = "detailsBarColor",
    )

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
        DetailsContent(
            modifier = Modifier.fillMaxSize(),
            listState = listState,
            artist = artist,
            state = uiState,
            onAction = viewModel::handleAction
        )

        // The bar floats over the hero gradient rather than sitting above it, so the colour runs
        // all the way to the top of the screen. It keeps its default insets so the back button
        // still lands below the status bar.
        TopAppBar(
            title = {
                if (isCollapsed) {
                    Text(
                        text = artist.name,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = barColor),
            navigationIcon = {
                IconButton(
                    onClick = { viewModel.handleAction(DetailsUiAction.OnBackRequest) },
                    colors = IconButtonDefaults.iconButtonColors(
                        // Over the gradient the icon needs its own scrim to stay legible; over
                        // the collapsed bar it would just be a smudge.
                        containerColor = if (isCollapsed) {
                            Color.Transparent
                        } else {
                            Color.Black.copy(alpha = SCRIM_ALPHA)
                        },
                        contentColor = if (isCollapsed) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            Color.White
                        },
                    ),
                    modifier = Modifier.semantics { contentDescription = backButtonDescription }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun DetailsContent(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    artist: Artist,
    state: DetailsUiState,
    onAction: (DetailsUiAction) -> Unit,
) {
    val contentDescription = AccessibilityUtils.getArtistDetailsContentDescription(artist)
    val sidePadding = Modifier.padding(horizontal = Dimensions.paddingLarge)

    LazyColumn(
        modifier = modifier.semantics { this.contentDescription = contentDescription },
        state = listState,
        contentPadding = PaddingValues(bottom = Dimensions.paddingXXL),
        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingLarge),
    ) {
        item(key = "hero") { ArtistHero(artist = artist) }

        item(key = "favorite") {
            LikeButton(
                modifier = sidePadding.padding(top = Dimensions.paddingMedium),
                isFavorite = state.isFavorite,
                isLoading = state.isFavoriteActionInProgress,
                onClick = { onAction(DetailsUiAction.ToggleFavorite(artist)) }
            )
        }

        artist.disambiguation?.takeIf { it.isNotBlank() }?.let { disambiguation ->
            item(key = "about") {
                Text(
                    text = disambiguation,
                    modifier = sidePadding,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        albumsSection(
            modifier = sidePadding,
            releaseGroups = state.releaseGroups,
            onRetryClick = { onAction(DetailsUiAction.LoadArtistDetails(artist)) },
        )
    }
}

/**
 * The artist's own gradient (the same one their list avatar uses - see
 * [com.jrom.mynextfavartist.ui.components.ArtistAvatar]) blown up to a full-bleed header, faded
 * into the page background at the bottom so the scrolling content doesn't butt against a hard
 * colour edge.
 */
@Composable
private fun ArtistHero(
    modifier: Modifier = Modifier,
    artist: Artist,
) {
    val brush = rememberArtistBrush(artist.name)
    val surface = MaterialTheme.colorScheme.surface
    val nameHeadingDescription =
        stringResource(R.string.artist_name_heading_description, artist.name)

    // Both backgrounds sit on the column rather than on wrapper boxes: they paint in declaration
    // order, so the scrim lands on top of the gradient, and the header's height stays a function
    // of the text it contains instead of needing a parent to measure against.
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(brush)
            .background(Brush.verticalGradient(listOf(Color.Transparent, surface)))
            .statusBarsPadding()
            .padding(top = Dimensions.heroTopClearance)
            .padding(Dimensions.paddingLarge),
        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium),
    ) {
        Text(
            text = artist.name,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics {
                heading()
                contentDescription = nameHeadingDescription
            }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)) {
            HeroPill(text = artist.type ?: stringResource(R.string.unknown_artist_type))
            artist.country?.let { HeroPill(text = it) }
        }
    }
}

@Composable
private fun HeroPill(
    modifier: Modifier = Modifier,
    text: String,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(Dimensions.buttonCornerRadius),
            )
            .padding(
                horizontal = Dimensions.pillPaddingHorizontal,
                vertical = Dimensions.pillPaddingVertical,
            ),
    )
}

/**
 * Saved and not-saved are different enough to warrant different weights: adding is the screen's
 * primary call to action, whereas "already in your list" is a confirmation the user should be
 * able to undo without the button shouting at them.
 */
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
            .heightIn(min = Dimensions.buttonHeight)
            .semantics { contentDescription = buttonDescription },
        shape = RoundedCornerShape(Dimensions.buttonCornerRadius),
        colors = if (isFavorite) {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        } else {
            ButtonDefaults.buttonColors()
        },
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(Dimensions.iconSizeMedium),
                // Not onPrimary: the button is disabled while loading, and in the favourited
                // state its container is tertiary - either way a hardcoded colour disappears.
                // LocalContentColor is whatever the button actually resolved for its content.
                color = LocalContentColor.current,
            )
        } else {
            Icon(
                painter = painterResource(if (isFavorite) R.drawable.ic_check else R.drawable.ic_favorite),
                contentDescription = null,
                modifier = Modifier.size(Dimensions.iconSizeMedium),
            )
            Spacer(modifier = Modifier.width(Dimensions.paddingMedium))
            Text(
                text = stringResource(if (isFavorite) R.string.liked_artist_button else R.string.like_artist_button),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

/**
 * Contributes the albums heading and one item per release group to the enclosing [LazyColumn],
 * so a long discography is only composed as far as the user scrolls.
 */
private fun LazyListScope.albumsSection(
    modifier: Modifier = Modifier,
    releaseGroups: BaseUiState<List<ReleaseGroup>>,
    onRetryClick: () -> Unit,
) {
    item(key = "albums_header") {
        SectionHeader(
            modifier = modifier.padding(top = Dimensions.paddingMedium),
            title = stringResource(R.string.albums_section_title),
            subtitle = (releaseGroups as? BaseUiState.Success)
                ?.data
                ?.takeIf { it.isNotEmpty() }
                ?.let { pluralStringResource(R.plurals.albums_count, it.size, it.size) },
        )
    }

    when (releaseGroups) {
        is BaseUiState.Success -> if (releaseGroups.data.isEmpty()) {
            item(key = "albums_empty") {
                Text(
                    text = stringResource(R.string.no_albums_message),
                    modifier = modifier,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(
                items = releaseGroups.data,
                key = { releaseGroup -> releaseGroup.mbid },
            ) { releaseGroup ->
                ReleaseGroupRow(modifier = modifier, releaseGroup = releaseGroup)
            }
        }

        is BaseUiState.Error -> item(key = "albums_error") {
            ErrorView(
                modifier = modifier.fillMaxWidth(),
                error = releaseGroups,
                onRetryClick = onRetryClick,
            )
        }

        BaseUiState.Loading, BaseUiState.Initial, BaseUiState.Empty -> item(key = "albums_loading") {
            // Sized to roughly one release row so the section doesn't collapse and then jump
            // open when the list arrives.
            LoadingView(
                modifier = modifier
                    .fillMaxWidth()
                    .height(Dimensions.imageSizeSmall)
            )
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

        Spacer(modifier = Modifier.width(Dimensions.paddingLarge))

        Column {
            Text(
                text = releaseGroup.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            val subtitle = listOfNotNull(releaseGroup.primaryType, releaseGroup.firstReleaseDate)
                .joinToString(separator = " · ")
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
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

@Preview(showSystemUi = true, name = "Light")
@Preview(showSystemUi = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetailsContentFavoritePreview() {
    PreviewWrapper {
        DetailsContent(
            artist = Artist(
                mbid = "5b11f4ce-a62d-471e-81fc-a69a8278c7da",
                name = "Nirvana",
                type = "Group",
                country = "US",
                disambiguation = "1980s-1990s US grunge band",
            ),
            state = DetailsUiState(
                isFavorite = true,
                releaseGroups = BaseUiState.Loading,
            ),
            onAction = { }
        )
    }
}
