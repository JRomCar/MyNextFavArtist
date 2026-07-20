package com.jrom.mynextfavartist.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

interface BottomNavItem {
    @get:DrawableRes val icon: Int
    @get:StringRes val titleRes: Int
}
