package com.jrom.mynextfavartist.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.ui.R

/**
 * Utility functions for generating consistent accessibility content descriptions
 * across different screens in the app.
 */
object AccessibilityUtils {

    @Composable
    fun getArtistItemContentDescription(artist: Artist): String {
        return stringResource(
            R.string.artist_item_content_description,
            artist.name,
            artist.type ?: stringResource(R.string.unknown_artist_type)
        )
    }

    @Composable
    fun getHomeScreenDescription(): String {
        return stringResource(R.string.home_screen_description)
    }

    @Composable
    fun getSearchScreenDescription(): String {
        return stringResource(R.string.search_screen_description)
    }
}
