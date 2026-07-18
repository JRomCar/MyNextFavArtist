package com.jrom.mynextfavartist.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
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

@Composable
fun ArtistList(
    modifier: Modifier = Modifier,
    artists: List<Artist>,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onArtistClick: (artist: Artist) -> Unit,
) {
    val topPadding = Dimensions.listContentPadding + contentPadding.calculateTopPadding()
    val bottomPadding = Dimensions.listContentPadding + contentPadding.calculateBottomPadding()
    LazyColumn(
        modifier = modifier.consumeWindowInsets(contentPadding),
        state = rememberLazyListState(),
        contentPadding = PaddingValues(
            start = Dimensions.listContentPadding,
            end = Dimensions.listContentPadding,
            top = topPadding,
            bottom = bottomPadding,
        ),
        verticalArrangement = Arrangement.spacedBy(Dimensions.listItemSpacing)
    ) {
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

@Preview(showBackground = true)
@Composable
private fun ArtistListPreview() {
    PreviewWrapper {
        ArtistList(
            artists = previewArtists,
            onArtistClick = { },
        )
    }
}
