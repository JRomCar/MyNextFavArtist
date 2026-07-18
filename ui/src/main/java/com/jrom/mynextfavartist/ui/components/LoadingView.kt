package com.jrom.mynextfavartist.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
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
fun LoadingView(
    modifier: Modifier = Modifier
) {
    val loadingDescription = stringResource(R.string.loading_label)

    Box(
        modifier = modifier.semantics { contentDescription = loadingDescription },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(Dimensions.progressIndicatorSize)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingViewPreview() {
    PreviewWrapper {
        LoadingView()
    }
}
