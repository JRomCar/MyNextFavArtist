package com.jrom.mynextfavartist.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.jrom.mynextfavartist.ui.R
import kotlinx.serialization.Serializable

@Serializable
data object Home : NavKey, BottomNavItem {
    override val icon: Int = R.drawable.ic_home
    override val title: String = "Home"
}

@Serializable
data object Favorites : NavKey, BottomNavItem {
    override val icon: Int = R.drawable.ic_favorite
    override val title: String = "Favorites"
}

@Serializable
data object Search : NavKey, BottomNavItem {
    override val icon: Int = R.drawable.ic_search
    override val title: String = "Search"
}

@Serializable
data class ArtistDetails(val artist: ArtistNavArg) : NavKey
