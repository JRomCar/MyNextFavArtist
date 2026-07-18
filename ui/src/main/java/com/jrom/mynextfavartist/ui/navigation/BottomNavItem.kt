package com.jrom.mynextfavartist.ui.navigation

import androidx.annotation.DrawableRes

interface BottomNavItem {
    @get:DrawableRes val icon: Int
    val title: String
}
