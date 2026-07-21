package com.jrom.mynextfavartist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
    Card(
        modifier = modifier.size(size),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = shape
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_album),
                contentDescription = null,
                modifier = Modifier.size(Dimensions.iconSizeMedium),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            AsyncImage(
                model = releaseGroupCoverArtUrl(releaseGroupMbid),
                contentDescription = stringResource(R.string.album_art_content_description, albumTitle),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
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
