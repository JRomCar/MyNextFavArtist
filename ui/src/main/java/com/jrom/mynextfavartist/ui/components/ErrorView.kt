package com.jrom.mynextfavartist.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.jrom.mynextfavartist.domain.error.DataError
import com.jrom.mynextfavartist.ui.R
import com.jrom.mynextfavartist.ui.error.asUiIcon
import com.jrom.mynextfavartist.ui.error.asUiText
import com.jrom.mynextfavartist.ui.states.BaseUiState
import com.jrom.mynextfavartist.ui.utils.Dimensions
import com.jrom.mynextfavartist.ui.utils.PreviewWrapper

@Composable
fun ErrorView(
    modifier: Modifier = Modifier,
    error: BaseUiState.Error,
    onRetryClick: (() -> Unit)? = null
) {
    val errorDescription = stringResource(R.string.error_occurred_label)

    Box(
        modifier = modifier.semantics { contentDescription = errorDescription },
        contentAlignment = Alignment.Center
    ) {
        ErrorContent(
            error = error,
            onRetryClick = onRetryClick
        )
    }
}

@Composable
private fun ErrorContent(
    error: BaseUiState.Error,
    onRetryClick: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimensions.paddingXL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(error.errorIcon),
            contentDescription = null,
            modifier = Modifier.size(Dimensions.iconSizeLarge),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

        Text(
            text = error.errorText.asString(),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(Dimensions.paddingMedium))

        onRetryClick?.let { retry ->
            Button(
                onClick = retry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_refresh),
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.iconSizeSmall)
                )
                Spacer(modifier = Modifier.padding(Dimensions.spacingSmall))
                Text(stringResource(R.string.try_again_label))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorViewPreview() {
    PreviewWrapper {
        val error = DataError.Network.SERVER_ERROR
        ErrorView(
            error = BaseUiState.Error(error.asUiText(), error.asUiIcon()),
            onRetryClick = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorViewNoRetryPreview() {
    PreviewWrapper {
        val error = DataError.Local.DB_WRITE_ERROR
        ErrorView(
            error = BaseUiState.Error(error.asUiText(), error.asUiIcon()),
        )
    }
}
