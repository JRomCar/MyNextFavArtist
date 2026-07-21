package com.jrom.mynextfavartist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.jrom.mynextfavartist.ui.theme.AvatarGradients
import com.jrom.mynextfavartist.ui.utils.Dimensions
import com.jrom.mynextfavartist.ui.utils.PreviewWrapper

/**
 * The gradient is picked from [AvatarGradients] by hashing the artist's name, which gives every
 * artist a stable, distinct colour for free - the same list scrolled twice always looks the same.
 * Real Coil-loaded imagery is reserved for release-group (album) cover art, which MusicBrainz does
 * provide via Cover Art Archive - see [AlbumArtCard].
 */
@Composable
fun ArtistAvatar(
    modifier: Modifier = Modifier,
    artistName: String,
    size: Dp = Dimensions.imageSizeMedium,
    shape: Shape = CircleShape,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge,
) {
    val brush = rememberArtistBrush(artistName)
    val initials = remember(artistName) { artistName.toInitials() }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(brush),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = textStyle,
            // The gradients are saturated in both themes, so the label is always light-on-colour
            // rather than following the colour scheme's onSurface.
            color = Color.White,
        )
    }
}

/** The gradient [artistName] maps to, exposed so larger surfaces (the details hero) can match. */
@Composable
fun rememberArtistBrush(artistName: String): Brush = remember(artistName) {
    val (start, end) = AvatarGradients[artistName.hashCode().mod(AvatarGradients.size)]
    Brush.linearGradient(listOf(start, end))
}

/**
 * First letter of the first two words, so "Nine Inch Nails" reads as "NI" rather than "N".
 * Falls back to "?" for names that are blank or made entirely of separators.
 */
private fun String.toInitials(): String = trim()
    .split(' ', '-')
    .filter { it.isNotBlank() }
    .take(2)
    .mapNotNull { word -> word.firstOrNull { it.isLetterOrDigit() }?.uppercase() }
    .joinToString(separator = "")
    .ifEmpty { "?" }

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
        ArtistAvatar(
            artistName = "Nine Inch Nails",
            size = Dimensions.imageSizeLarge,
            textStyle = MaterialTheme.typography.displayLarge,
        )
    }
}
