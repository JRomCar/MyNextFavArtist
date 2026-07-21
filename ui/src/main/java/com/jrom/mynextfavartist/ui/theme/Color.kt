package com.jrom.mynextfavartist.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Brand palette. Music-discovery apps lean on a saturated violet/magenta identity
 * against near-black surfaces rather than on the device's wallpaper colours, so these are fixed
 * values and [MyNextFavArtistTheme] opts out of dynamic colour by default.
 */

// Primary - violet
val Violet10 = Color(0xFF1B0068)
val Violet40 = Color(0xFF5B3DF5)
val Violet80 = Color(0xFFC9BCFF)
val Violet90 = Color(0xFFE5DEFF)
val VioletContainerDark = Color(0xFF4526C7)
val OnVioletDark = Color(0xFF2F0F91)

// Secondary - a muted violet. Material uses the secondary container for low-emphasis chrome such
// as the navigation bar's selected-item pill, so it has to stay in the primary's family; an
// unrelated accent hue here reads as a mismatched highlight rather than as part of the brand.
val Secondary10 = Color(0xFF1D192B)
val Secondary40 = Color(0xFF605A78)
val Secondary80 = Color(0xFFCBC2DB)
val Secondary90 = Color(0xFFE6DFF9)
val SecondaryContainerDark = Color(0xFF4A4458)
val OnSecondaryDark = Color(0xFF332D41)

// Tertiary - magenta, the accent that marks an artist as saved
val Magenta10 = Color(0xFF40001B)
val Magenta40 = Color(0xFFE0316E)
val Magenta80 = Color(0xFFFFB1C4)
val Magenta90 = Color(0xFFFFD9E1)
val MagentaContainerDark = Color(0xFF8F2148)
val OnMagentaDark = Color(0xFF66002E)

// Neutrals
val NeutralLightBackground = Color(0xFFFCFAFF)
val NeutralLightSurfaceContainer = Color(0xFFF2ECF6)
val NeutralLightSurfaceContainerHigh = Color(0xFFECE5F1)
val NeutralLightOnSurface = Color(0xFF1B1B1F)
val NeutralLightSurfaceVariant = Color(0xFFE7E0EC)
val NeutralLightOnSurfaceVariant = Color(0xFF49454E)
val NeutralLightOutline = Color(0xFF7A757F)
val NeutralLightOutlineVariant = Color(0xFFCAC4CF)

val NeutralDarkBackground = Color(0xFF121016)
val NeutralDarkSurfaceContainer = Color(0xFF1E1B23)
val NeutralDarkSurfaceContainerHigh = Color(0xFF29252E)
val NeutralDarkOnSurface = Color(0xFFE6E1E9)
val NeutralDarkSurfaceVariant = Color(0xFF49454E)
val NeutralDarkOnSurfaceVariant = Color(0xFFCAC4CF)
val NeutralDarkOutline = Color(0xFF948F99)
val NeutralDarkOutlineVariant = Color(0xFF49454E)

// Errors
val ErrorLight = Color(0xFFBA1A1A)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)
val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

/**
 * MusicBrainz exposes no artist imagery, so [com.jrom.mynextfavartist.ui.components.ArtistAvatar]
 * fills that gap with a gradient picked deterministically from the artist's name. Keeping the
 * ramps here rather than in the component keeps every brand colour in one file.
 *
 * The list is deliberately long: with only a handful of ramps, a screenful of artists visibly
 * repeats itself. Ten spread across the wheel makes a collision in any one viewport unlikely.
 */
val AvatarGradients: List<Pair<Color, Color>> = listOf(
    Color(0xFF7C4DFF) to Color(0xFFE040FB),
    Color(0xFF00B0FF) to Color(0xFF3D5AFE),
    Color(0xFFFF6E40) to Color(0xFFFF1744),
    Color(0xFF1DE9B6) to Color(0xFF00B8D4),
    Color(0xFFFFC400) to Color(0xFFFF6D00),
    Color(0xFFEC407A) to Color(0xFF7B1FA2),
    Color(0xFF00C853) to Color(0xFF64DD17),
    Color(0xFF536DFE) to Color(0xFF00BFA5),
    Color(0xFFF50057) to Color(0xFFFF6D00),
    Color(0xFF6200EA) to Color(0xFF2962FF),
)
