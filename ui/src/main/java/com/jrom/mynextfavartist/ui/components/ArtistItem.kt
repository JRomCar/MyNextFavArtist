package com.jrom.mynextfavartist.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.ui.R
import com.jrom.mynextfavartist.ui.utils.Dimensions
import com.jrom.mynextfavartist.ui.utils.PreviewWrapper
import com.jrom.mynextfavartist.ui.utils.previewArtists

@Composable
fun ArtistItem(
    modifier: Modifier = Modifier,
    artist: Artist,
) {
    val itemDescription = stringResource(
        R.string.artist_item_content_description,
        artist.name,
        artist.type ?: stringResource(R.string.unknown_error),
    )

    Card(
        modifier = modifier
            .padding(Dimensions.paddingMedium)
            .semantics { contentDescription = itemDescription },
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevationDefault),
        shape = RoundedCornerShape(Dimensions.cardCornerRadius)
    ) {
        Row(
            modifier = Modifier.padding(Dimensions.paddingLarge),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ArtistAvatar(artistName = artist.name)

            Spacer(modifier = Modifier.width(Dimensions.spacingLarge))

            ArtistInformation(
                modifier = Modifier.weight(1f),
                artist = artist,
            )
        }
    }
}

@Composable
private fun ArtistInformation(
    modifier: Modifier = Modifier,
    artist: Artist,
) {
    Column(modifier = modifier) {
        Text(
            text = artist.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

        val subtitle = listOfNotNull(artist.type, artist.country).joinToString(separator = " · ")
        if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        artist.disambiguation?.takeIf { it.isNotBlank() }?.let { disambiguation ->
            Spacer(modifier = Modifier.height(Dimensions.spacingMicro))
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

@Preview(showBackground = true)
@Composable
private fun ArtistItemPreview() {
    PreviewWrapper {
        ArtistItem(artist = previewArtists.first())
    }
}

@Preview(showBackground = true)
@Composable
private fun ArtistItemWithDisambiguationPreview() {
    PreviewWrapper {
        ArtistItem(artist = previewArtists[1])
    }
}
