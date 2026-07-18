package com.jrom.mynextfavartist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import com.jrom.mynextfavartist.ui.R
import com.jrom.mynextfavartist.ui.utils.Dimensions
import com.jrom.mynextfavartist.ui.utils.PreviewWrapper
import com.jrom.mynextfavartist.ui.utils.releaseGroupCoverArtUrl

@Composable
fun AlbumArtCard(
    modifier: Modifier = Modifier,
    releaseGroupMbid: String,
    albumTitle: String,
    size: Dp = Dimensions.imageSizeSmall,
    shape: Shape = RoundedCornerShape(Dimensions.cardCornerRadiusSmall),
    elevation: Dp = Dimensions.cardElevationDefault,
) {
    var loadFailed by remember(releaseGroupMbid) { mutableStateOf(false) }

    Card(
        modifier = modifier.size(size),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = shape
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (loadFailed) {
                Box(
                    modifier = Modifier
                        .clip(shape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_album),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                AsyncImage(
                    model = releaseGroupCoverArtUrl(releaseGroupMbid),
                    contentDescription = stringResource(R.string.album_art_content_description, albumTitle),
                    modifier = Modifier
                        .clip(shape)
                        .background(color = MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop,
                    onError = { loadFailed = true },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AlbumArtCardPreview() {
    PreviewWrapper {
        AlbumArtCard(
            releaseGroupMbid = "b1392450-e666-3926-a536-22c65f834433",
            albumTitle = "OK Computer",
        )
    }
}
