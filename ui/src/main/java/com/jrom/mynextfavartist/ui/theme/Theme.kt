package com.jrom.mynextfavartist.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Violet80,
    onPrimary = OnVioletDark,
    primaryContainer = VioletContainerDark,
    onPrimaryContainer = Violet90,
    secondary = Secondary80,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = Secondary90,
    tertiary = Magenta80,
    onTertiary = OnMagentaDark,
    tertiaryContainer = MagentaContainerDark,
    onTertiaryContainer = Magenta90,
    background = NeutralDarkBackground,
    onBackground = NeutralDarkOnSurface,
    surface = NeutralDarkBackground,
    onSurface = NeutralDarkOnSurface,
    surfaceContainer = NeutralDarkSurfaceContainer,
    surfaceContainerHigh = NeutralDarkSurfaceContainerHigh,
    surfaceVariant = NeutralDarkSurfaceVariant,
    onSurfaceVariant = NeutralDarkOnSurfaceVariant,
    outline = NeutralDarkOutline,
    outlineVariant = NeutralDarkOutlineVariant,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    scrim = Color.Black,
)

private val LightColorScheme = lightColorScheme(
    primary = Violet40,
    onPrimary = Color.White,
    primaryContainer = Violet90,
    onPrimaryContainer = Violet10,
    secondary = Secondary40,
    onSecondary = Color.White,
    secondaryContainer = Secondary90,
    onSecondaryContainer = Secondary10,
    tertiary = Magenta40,
    onTertiary = Color.White,
    tertiaryContainer = Magenta90,
    onTertiaryContainer = Magenta10,
    background = NeutralLightBackground,
    onBackground = NeutralLightOnSurface,
    surface = NeutralLightBackground,
    onSurface = NeutralLightOnSurface,
    surfaceContainer = NeutralLightSurfaceContainer,
    surfaceContainerHigh = NeutralLightSurfaceContainerHigh,
    surfaceVariant = NeutralLightSurfaceVariant,
    onSurfaceVariant = NeutralLightOnSurfaceVariant,
    outline = NeutralLightOutline,
    outlineVariant = NeutralLightOutlineVariant,
    error = ErrorLight,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    scrim = Color.Black,
)

/**
 * [dynamicColor] defaults to false: the app has a brand identity (see Color.kt) and letting the
 * device wallpaper recolour it would make the same screen look different on every phone. The
 * parameter is kept so a caller that wants Material You can still opt in.
 */
@Composable
fun MyNextFavArtistTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
