package com.jrom.mynextfavartist.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jrom.mynextfavartist.ui.R
import com.jrom.mynextfavartist.ui.utils.Dimensions
import com.jrom.mynextfavartist.ui.utils.PreviewWrapper

private const val SKELETON_ROW_COUNT = 6
private const val SHIMMER_DURATION_MILLIS = 1_200
private const val SHIMMER_TRAVEL = 1_000f

/**
 * Placeholder rows shaped like [ArtistItem], shown while a list loads. A skeleton beats a bare
 * spinner here because every artist list has the same row geometry, so the layout doesn't jump
 * when the real data lands.
 */
@Composable
fun ArtistListSkeleton(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val loadingDescription = stringResource(R.string.loading_label)
    val brush = rememberShimmerBrush()

    Column(
        modifier = modifier
            .padding(contentPadding)
            .padding(Dimensions.paddingLarge)
            .semantics { contentDescription = loadingDescription },
        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium),
    ) {
        repeat(SKELETON_ROW_COUNT) {
            SkeletonRow(brush = brush)
        }
    }
}

@Composable
private fun SkeletonRow(
    modifier: Modifier = Modifier,
    brush: Brush,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.cardCornerRadiusLarge))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(Dimensions.paddingLarge),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingLarge),
    ) {
        ShimmerBlock(
            brush = brush,
            shape = CircleShape,
            modifier = Modifier.size(Dimensions.imageSizeMedium),
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium),
        ) {
            ShimmerBlock(
                brush = brush,
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.6f)
                    .height(Dimensions.skeletonLineHeightLarge),
            )
            ShimmerBlock(
                brush = brush,
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.35f)
                    .height(Dimensions.skeletonLineHeightSmall),
            )
        }
    }
}

@Composable
private fun ShimmerBlock(
    brush: Brush,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Dimensions.cardCornerRadiusSmall),
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

/**
 * A highlight band swept across a static base colour. The animation is driven by one
 * [rememberInfiniteTransition] shared by every block in a list, so a six-row skeleton still costs
 * a single running animation rather than one per placeholder.
 */
@Composable
private fun rememberShimmerBrush(): Brush {
    val base = MaterialTheme.colorScheme.surfaceContainerHigh
    val highlight = MaterialTheme.colorScheme.surfaceVariant

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = -SHIMMER_TRAVEL,
        targetValue = SHIMMER_TRAVEL,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SHIMMER_DURATION_MILLIS),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslate",
    )

    return Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(translate, 0f),
        end = Offset(translate + SHIMMER_TRAVEL / 2f, 0f),
    )
}

@Preview(showBackground = true, name = "Light")
@Preview(showBackground = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ArtistListSkeletonPreview() {
    PreviewWrapper {
        ArtistListSkeleton()
    }
}
