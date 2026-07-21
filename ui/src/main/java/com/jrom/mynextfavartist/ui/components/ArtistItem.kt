package com.jrom.mynextfavartist.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.ui.R
import com.jrom.mynextfavartist.ui.utils.AccessibilityUtils
import com.jrom.mynextfavartist.ui.utils.Dimensions
import com.jrom.mynextfavartist.ui.utils.PreviewWrapper
import com.jrom.mynextfavartist.ui.utils.previewArtists

/**
 * A flat tonal row rather than an elevated card: with a dozen of these stacked in a list, drop
 * shadows read as visual noise, so the row separates itself from the background with a tonal
 * surface and a generous corner radius instead.
 */
@Composable
fun ArtistItem(
    modifier: Modifier = Modifier,
    artist: Artist,
) {
    val itemDescription = AccessibilityUtils.getArtistItemContentDescription(artist)

    Card(
        modifier = modifier.semantics { contentDescription = itemDescription },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(Dimensions.cardCornerRadiusLarge)
    ) {
        Row(
            modifier = Modifier.padding(Dimensions.paddingLarge),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ArtistAvatar(artistName = artist.name)

            Spacer(modifier = Modifier.width(Dimensions.paddingLarge))

            ArtistInformation(
                modifier = Modifier.weight(1f),
                artist = artist,
            )

            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null, // The whole row is already labelled.
                modifier = Modifier.size(Dimensions.iconSizeMedium),
                tint = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun ArtistInformation(
    modifier: Modifier = Modifier,
    artist: Artist,
) {
    // spacedBy rather than Spacers between the lines: it applies only between children, so the
    // optional subtitle and disambiguation can be absent without leaving a phantom gap behind.
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimensions.spacingSmall),
    ) {
        Text(
            text = artist.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        val subtitle = listOfNotNull(artist.type, artist.country).joinToString(separator = " · ")
        if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        artist.disambiguation?.takeIf { it.isNotBlank() }?.let { disambiguation ->
            Text(
                text = disambiguation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true, name = "Light")
@Preview(showBackground = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ArtistItemPreview() {
    PreviewWrapper {
        ArtistItem(
            modifier = Modifier.padding(Dimensions.paddingLarge),
            artist = previewArtists.first(),
        )
    }
}

@Preview(showBackground = true, name = "Light")
@Preview(showBackground = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ArtistItemWithDisambiguationPreview() {
    PreviewWrapper {
        ArtistItem(
            modifier = Modifier.padding(Dimensions.paddingLarge),
            artist = previewArtists[1],
        )
    }
}
