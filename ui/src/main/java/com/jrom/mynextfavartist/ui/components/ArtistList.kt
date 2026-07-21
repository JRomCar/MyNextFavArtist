package com.jrom.mynextfavartist.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.ui.utils.Dimensions
import com.jrom.mynextfavartist.ui.utils.PreviewWrapper
import com.jrom.mynextfavartist.ui.utils.previewArtists

/**
 * [header] is rendered as the list's first item rather than above the list, so it scrolls away
 * with the content the way a screen title does in Dice or Fever, instead of permanently eating
 * vertical space on a short phone.
 */
@Composable
fun ArtistList(
    modifier: Modifier = Modifier,
    artists: List<Artist>,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    header: (@Composable () -> Unit)? = null,
    onArtistClick: (artist: Artist) -> Unit,
) {
    val topPadding = Dimensions.paddingLarge + contentPadding.calculateTopPadding()
    val bottomPadding = Dimensions.paddingLarge + contentPadding.calculateBottomPadding()
    LazyColumn(
        modifier = modifier.consumeWindowInsets(contentPadding),
        state = rememberLazyListState(),
        contentPadding = PaddingValues(
            start = Dimensions.paddingLarge,
            end = Dimensions.paddingLarge,
            top = topPadding,
            bottom = bottomPadding,
        ),
        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)
    ) {
        header?.let {
            item(key = "header") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Dimensions.paddingMedium)
                ) {
                    it()
                }
            }
        }

        items(
            items = artists,
            key = { artist -> artist.mbid }
        ) { artist ->
            ArtistItem(
                modifier = Modifier.clickable { onArtistClick(artist) },
                artist = artist,
            )
        }
    }
}

@Preview(showBackground = true, name = "Light")
@Preview(showBackground = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ArtistListPreview() {
    PreviewWrapper {
        ArtistList(
            artists = previewArtists,
            header = { SectionHeader(title = "Discover", subtitle = "Artists picked for you") },
            onArtistClick = { },
        )
    }
}
