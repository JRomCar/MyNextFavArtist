package com.jrom.mynextfavartist.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.jrom.mynextfavartist.ui.R
import com.jrom.mynextfavartist.ui.utils.Dimensions
import com.jrom.mynextfavartist.ui.utils.PreviewWrapper

@Composable
fun NoConnectionBanner(
    modifier: Modifier = Modifier
) {
    val bannerDescription = stringResource(R.string.no_connection_label)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(
                horizontal = Dimensions.paddingLarge,
                vertical = Dimensions.paddingMedium,
            )
            .semantics { contentDescription = bannerDescription },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_warning),
            contentDescription = null,
            modifier = Modifier.size(Dimensions.iconSizeSmall),
            tint = MaterialTheme.colorScheme.onErrorContainer,
        )
        Text(
            text = stringResource(id = R.string.no_connection_label),
            modifier = Modifier.padding(start = Dimensions.paddingMedium),
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
@Preview(showBackground = true, name = "Light")
@Preview(showBackground = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun NoConnectionBannerPreview() = PreviewWrapper {
    NoConnectionBanner()
}
