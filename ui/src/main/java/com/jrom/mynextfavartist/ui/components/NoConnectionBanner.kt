package com.jrom.mynextfavartist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.error)
            .padding(Dimensions.paddingMedium)
            .semantics {
                contentDescription = bannerDescription
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.no_connection_label),
            color = MaterialTheme.colorScheme.onError,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
@Preview("Light")
private fun NoConnectionBannerPreview() = PreviewWrapper {
    NoConnectionBanner()
}
