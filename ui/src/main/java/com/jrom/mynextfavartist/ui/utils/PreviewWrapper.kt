package com.jrom.mynextfavartist.ui.utils

import androidx.compose.runtime.Composable
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.ui.theme.MyNextFavArtistTheme

@Composable
fun PreviewWrapper(
    content: @Composable () -> Unit
) {
    MyNextFavArtistTheme {
        content()
    }
}

val previewArtists = listOf(
    Artist(mbid = "1", name = "Radiohead", type = "Group", country = "GB", disambiguation = null),
    Artist(mbid = "2", name = "Nirvana", type = "Group", country = "US", disambiguation = "1980s-1990s US grunge band"),
    Artist(mbid = "3", name = "Adele", type = "Person", country = "GB", disambiguation = null),
)
