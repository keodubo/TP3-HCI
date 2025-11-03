package com.comprartir.mobile.core.designsystem

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Elevation levels inspired by the shadow definitions from the web project.
 * shadow1 ≈ 2.dp (cards), shadow2 ≈ 8.dp (hovered cards/dialogs).
 */
data class ElevationTokens(
    val level0: Dp = 0.dp,
    val shadow1: Dp = 2.dp,
    val shadow2: Dp = 8.dp,
)

val LocalElevations = staticCompositionLocalOf { ElevationTokens() }
