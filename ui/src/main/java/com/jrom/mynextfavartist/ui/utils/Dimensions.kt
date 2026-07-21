package com.jrom.mynextfavartist.ui.utils

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Dimensions {

    // Padding and Margins
    val paddingMedium: Dp = 8.dp
    val paddingLarge: Dp = 16.dp
    val paddingXXL: Dp = 24.dp

    // Card and Component Dimensions
    val cardCornerRadiusSmall: Dp = 8.dp
    val cardCornerRadiusLarge: Dp = 20.dp
    val cardElevationDefault: Dp = 4.dp

    // Artist / Album Art Image Sizes
    val imageSizeSmall: Dp = 80.dp
    val imageSizeMedium: Dp = 56.dp
    val imageSizeLarge: Dp = 200.dp

    // Details hero header: clearance below the status bar for the floating back button, so the
    // artist name never sits underneath it. The header's height then follows its content.
    val heroTopClearance: Dp = 72.dp

    // Button Dimensions
    val buttonCornerRadius: Dp = 28.dp
    val buttonHeight: Dp = 52.dp

    // Spacing
    val spacingSmall: Dp = 4.dp

    // Pill / chip insets
    val pillPaddingHorizontal: Dp = 12.dp
    val pillPaddingVertical: Dp = 6.dp

    // Icon Sizes
    val iconSizeSmall: Dp = 18.dp
    val iconSizeMedium: Dp = 24.dp
    val iconSizeLarge: Dp = 64.dp

    // Progress Indicator
    val progressIndicatorSize: Dp = 48.dp

    // Placeholder rows shown while a list loads
    val skeletonLineHeightLarge: Dp = 16.dp
    val skeletonLineHeightSmall: Dp = 12.dp

    // Empty / error illustration. The max width keeps the centred message from stretching into
    // an unreadable single line on tablets and landscape.
    val emptyStateIconBoxSize: Dp = 96.dp
    val emptyStateMaxWidth: Dp = 360.dp
}
