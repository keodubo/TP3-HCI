package com.comprartir.mobile.core.ui

import android.content.res.Configuration
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun rememberIsLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return remember(configuration.orientation) {
        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }
}

@Composable
fun rememberIsTablet(windowSizeClass: WindowSizeClass): Boolean {
    return remember(windowSizeClass.widthSizeClass) {
        windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded ||
            windowSizeClass.widthSizeClass == WindowWidthSizeClass.Medium
    }
}

@Composable
fun AdaptiveLayout(
    isLandscape: Boolean = rememberIsLandscape(),
    portrait: @Composable () -> Unit,
    landscape: @Composable () -> Unit,
) {
    if (isLandscape) {
        landscape()
    } else {
        portrait()
    }
}
