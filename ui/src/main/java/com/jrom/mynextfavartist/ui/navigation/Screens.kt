package com.jrom.mynextfavartist.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.jrom.mynextfavartist.ui.R
import kotlinx.serialization.Serializable

@Serializable
data object Home : NavKey, BottomNavItem {
    override val icon: Int = R.drawable.ic_home
    override val titleRes: Int = R.string.home_tab_title
}

@Serializable
data object Favorites : NavKey, BottomNavItem {
    override val icon: Int = R.drawable.ic_favorite
    override val titleRes: Int = R.string.favorites_tab_title
}

@Serializable
data object Search : NavKey, BottomNavItem {
    override val icon: Int = R.drawable.ic_search
    override val titleRes: Int = R.string.search_tab_title
}

@Serializable
data class ArtistDetails(val artist: ArtistNavArg) : NavKey
