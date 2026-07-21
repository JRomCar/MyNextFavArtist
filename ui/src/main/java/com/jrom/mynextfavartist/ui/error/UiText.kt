package com.jrom.mynextfavartist.ui.error

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Defers string resolution from where a message is *decided* to where it's *displayed*.
 * ViewModels build errors via [DataError.asUiText][com.jrom.mynextfavartist.ui.error.asUiText]
 * but have no Context or @Composable scope to call `getString`/`stringResource` themselves -
 * and shouldn't hold a Context anyway, since that's a leak risk and untestable without
 * Robolectric. Wrapping a string resource + its format args instead lets a ViewModel stay pure
 * Kotlin while the UI resolves the actual localized string only when it renders, via
 * [asString]. [DynamicString] covers the rare case of a message that isn't in resources at all
 * (e.g. a raw value echoed back from an API).
 */
sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    data class StringResource(
        @StringRes val id: Int,
        val args: List<Any> = emptyList()
    ) : UiText()

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(id, *args.toTypedArray())
        }
    }

    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(id, *args.toTypedArray())
        }
    }
}
