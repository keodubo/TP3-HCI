package com.comprartir.mobile.core.designsystem

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing scale derived from the Comprartir layout gutters.
 */
data class Spacing(
    val none: Dp = 0.dp,
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 40.dp,
    val mobileGutter: Dp = 16.dp,
    val horizontalGutterMin: Dp = 16.dp,
    val horizontalGutterMax: Dp = 40.dp,
    val maxContentWidth: Dp = 1360.dp,
    // Friendly named accessors so callers can use `spacing.large` etc without
    // importing extension properties from this package.
    val micro: Dp = xxs,
    val tiny: Dp = xs,
    val small: Dp = sm,
    val medium: Dp = md,
    val large: Dp = lg,
    val extraLarge: Dp = xl,
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }
