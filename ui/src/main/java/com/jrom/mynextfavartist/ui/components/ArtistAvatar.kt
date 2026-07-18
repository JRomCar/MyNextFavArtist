package com.jrom.mynextfavartist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.jrom.mynextfavartist.ui.utils.Dimensions
import com.jrom.mynextfavartist.ui.utils.PreviewWrapper

/**
 * MusicBrainz has no artist-photo API (Cover Art Archive only covers releases/release-groups),
 * so artist rows use a deterministic initials avatar instead of a loaded image. Real
 * Coil-loaded imagery is reserved for release-group (album) cover art, which MusicBrainz
 * does provide via Cover Art Archive - see [AlbumArtCard].
 */
@Composable
fun ArtistAvatar(
    modifier: Modifier = Modifier,
    artistName: String,
    size: Dp = Dimensions.imageSizeSmall,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = artistName.trim().firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArtistAvatarPreview() {
    PreviewWrapper {
        ArtistAvatar(artistName = "Radiohead")
    }
}

@Preview(showBackground = true)
@Composable
private fun ArtistAvatarLargePreview() {
    PreviewWrapper {
        ArtistAvatar(artistName = "Nirvana", size = Dimensions.imageSizeLarge)
    }
}
