package com.jrom.mynextfavartist.ui.components

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.jrom.mynextfavartist.ui.R
import com.jrom.mynextfavartist.ui.utils.Dimensions
import com.jrom.mynextfavartist.ui.utils.PreviewWrapper

/**
 * The button an empty state can offer. Label and click live together because neither is useful
 * alone - passing them as separate nullable parameters made "label but no handler" expressible,
 * and the layout had to guard against it at runtime.
 */
data class EmptyStateAction(
    val label: String,
    val onClick: () -> Unit,
    @DrawableRes val icon: Int? = null,
)

/**
 * The one layout behind every "there is nothing to show" screen: the search prompt, the empty
 * favourites list, and - via [ErrorView] - failures.
 */
@Composable
fun EmptyStateView(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    title: String,
    description: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    iconBackground: Color = MaterialTheme.colorScheme.primaryContainer,
    action: EmptyStateAction? = null,
) {
    Box(
        modifier = modifier.padding(Dimensions.paddingXXL),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = Dimensions.emptyStateMaxWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(Dimensions.emptyStateIconBoxSize)
                    .background(color = iconBackground, shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null, // Announced by the title below.
                    modifier = Modifier.size(Dimensions.iconSizeLarge),
                    tint = iconTint,
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.paddingXXL))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )

            description?.let {
                Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            action?.let {
                Spacer(modifier = Modifier.height(Dimensions.paddingXXL))
                Button(
                    onClick = it.onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = Dimensions.buttonHeight),
                    shape = RoundedCornerShape(Dimensions.buttonCornerRadius),
                ) {
                    it.icon?.let { resId ->
                        Icon(
                            painter = painterResource(resId),
                            contentDescription = null,
                            modifier = Modifier.size(Dimensions.iconSizeSmall),
                        )
                        Spacer(modifier = Modifier.width(Dimensions.paddingMedium))
                    }
                    Text(text = it.label, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Light")
@Preview(showBackground = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyStateViewPreview() {
    PreviewWrapper {
        EmptyStateView(
            icon = R.drawable.ic_favorite,
            title = stringResource(R.string.no_favorites_title),
            description = stringResource(R.string.no_favorites_description),
        )
    }
}

@Preview(showBackground = true, name = "Light")
@Preview(showBackground = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyStateViewWithActionPreview() {
    PreviewWrapper {
        EmptyStateView(
            icon = R.drawable.ic_warning,
            title = stringResource(R.string.server_error),
            description = stringResource(R.string.error_occurred_label),
            action = EmptyStateAction(
                label = stringResource(R.string.try_again_label),
                onClick = {},
                icon = R.drawable.ic_refresh,
            ),
        )
    }
}
