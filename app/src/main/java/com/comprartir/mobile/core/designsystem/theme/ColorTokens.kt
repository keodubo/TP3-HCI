package com.comprartir.mobile.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class ColorTokenPalette(
    val navSurface: Color,
    val navDivider: Color,
    val navActiveBackground: Color,
    val navActiveContent: Color,
    val navInactiveBackground: Color,
    val navInactiveBorder: Color,
    val navInactiveContent: Color,
    val neutralSurface: Color,
    val purpleDeep: Color,
    val surfaceCard: Color,
    val textPrimary: Color,
    val textMuted: Color,
)

val LightColorTokens = ColorTokenPalette(
    navSurface = Color(0xFFFFFFFF),
    navDivider = Color(0xFFE5E7EB),
    navActiveBackground = Color(0xFF3C3553),
    navActiveContent = Color(0xFFFFFFFF),
    navInactiveBackground = Color(0xFFFFFFFF),
    navInactiveBorder = Color(0xFFD5D8E0),
    navInactiveContent = Color(0x993C3553),
    neutralSurface = Color(0xFFF4F6F8),
    purpleDeep = Color(0xFF2C2742),
    surfaceCard = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF0F172A),
    textMuted = Color(0xFF6B7280),
)

val DarkColorTokens = ColorTokenPalette(
    navSurface = Color(0xFF0E1320),
    navDivider = Color(0xFF1F2434),
    navActiveBackground = Color(0xFF3C3553),
    navActiveContent = Color(0xFFFFFFFF),
    navInactiveBackground = Color(0xFF0E1320),
    navInactiveBorder = Color(0xFF2E3449),
    navInactiveContent = Color(0xFFB7BED3),
    neutralSurface = Color(0xFF101522),
    purpleDeep = Color(0xFF2C2742),
    surfaceCard = Color(0xFF1E2536),
    textPrimary = Color(0xFFF4F6FB),
    textMuted = Color(0xFFB5BDCC),
)

val LocalColorTokens = staticCompositionLocalOf { LightColorTokens }

object ColorTokens {
    val NavSurface: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalColorTokens.current.navSurface

    val NavDivider: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalColorTokens.current.navDivider

    val NavActiveBackground: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalColorTokens.current.navActiveBackground

    val NavActiveContent: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalColorTokens.current.navActiveContent

    val NavInactiveBackground: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalColorTokens.current.navInactiveBackground

    val NavInactiveBorder: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalColorTokens.current.navInactiveBorder

    val NavInactiveContent: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalColorTokens.current.navInactiveContent

    val NeutralSurface: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalColorTokens.current.neutralSurface

    val PurpleDeep: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalColorTokens.current.purpleDeep

    val SurfaceCard: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalColorTokens.current.surfaceCard

    val TextPrimary: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalColorTokens.current.textPrimary

    val TextMuted: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalColorTokens.current.textMuted
}
