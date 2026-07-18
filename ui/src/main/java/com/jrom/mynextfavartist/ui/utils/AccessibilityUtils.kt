package com.jrom.mynextfavartist.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.jrom.mynextfavartist.domain.entities.Artist
import com.jrom.mynextfavartist.domain.entities.ReleaseGroup
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

    @Composable
    fun getFavoritesScreenDescription(): String {
        return stringResource(R.string.favorites_screen_description)
    }

    @Composable
    fun getFavoritesListDescription(): String {
        return stringResource(R.string.favorites_list_description)
    }

    @Composable
    fun getNoFavoritesContentDescription(): String {
        return stringResource(R.string.no_favorites_content_description)
    }

    @Composable
    fun getDeleteAllFavoritesDescription(): String {
        return stringResource(R.string.delete_all_favorites_description)
    }

    @Composable
    fun getEmptyFavoritesMessageDescription(): String {
        return stringResource(R.string.empty_favorites_message_description)
    }

    @Composable
    fun getBackButtonDescription(): String {
        return stringResource(R.string.back_button_description)
    }

    @Composable
    fun getArtistDetailsScreenDescription(artistName: String): String {
        return stringResource(R.string.artist_details_screen_description, artistName)
    }

    @Composable
    fun getArtistDetailsContentDescription(artist: Artist): String {
        return stringResource(
            R.string.artist_details_content_description,
            artist.name,
            artist.type ?: stringResource(R.string.unknown_artist_type)
        )
    }

    @Composable
    fun getArtistNameHeadingDescription(artistName: String): String {
        return stringResource(R.string.artist_name_heading_description, artistName)
    }

    @Composable
    fun getArtistDetailsCardDescription(): String {
        return stringResource(R.string.artist_details_card_description)
    }

    @Composable
    fun getFavoriteButtonDescription(isFavorite: Boolean, isLoading: Boolean): String {
        return when {
            isLoading -> stringResource(R.string.favorite_button_loading_description)
            isFavorite -> stringResource(R.string.remove_from_favorites_description)
            else -> stringResource(R.string.add_to_favorites_description)
        }
    }

    @Composable
    fun getReleaseGroupContentDescription(releaseGroup: ReleaseGroup): String {
        return stringResource(
            R.string.release_group_content_description,
            releaseGroup.title,
            releaseGroup.primaryType ?: stringResource(R.string.unknown_release_group_type)
        )
    }
}
