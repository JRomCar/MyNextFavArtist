package com.jrom.mynextfavartist.ui.components

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.jrom.mynextfavartist.domain.error.DataError
import com.jrom.mynextfavartist.ui.R
import com.jrom.mynextfavartist.ui.error.asUiIcon
import com.jrom.mynextfavartist.ui.error.asUiText
import com.jrom.mynextfavartist.ui.states.BaseUiState
import com.jrom.mynextfavartist.ui.utils.PreviewWrapper

/**
 * A failure is just an empty state with an error palette and a retry action, so this delegates to
 * [EmptyStateView] rather than repeating the icon/title/button column.
 */
@Composable
fun ErrorView(
    modifier: Modifier = Modifier,
    error: BaseUiState.Error,
    onRetryClick: (() -> Unit)? = null
) {
    val errorDescription = stringResource(R.string.error_occurred_label)
    val retryLabel = stringResource(R.string.try_again_label)

    EmptyStateView(
        modifier = modifier.semantics { contentDescription = errorDescription },
        icon = error.errorIcon,
        title = error.errorText.asString(),
        iconTint = MaterialTheme.colorScheme.error,
        iconBackground = MaterialTheme.colorScheme.errorContainer,
        action = onRetryClick?.let { retry ->
            EmptyStateAction(
                label = retryLabel,
                onClick = retry,
                icon = R.drawable.ic_refresh,
            )
        },
    )
}

@Preview(showBackground = true, name = "Light")
@Preview(showBackground = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
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

@Preview(showBackground = true, name = "Light")
@Preview(showBackground = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ErrorViewNoRetryPreview() {
    PreviewWrapper {
        val error = DataError.Local.DB_WRITE_ERROR
        ErrorView(
            error = BaseUiState.Error(error.asUiText(), error.asUiIcon()),
        )
    }
}
