package com.comprartir.mobile.core.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.comprartir.mobile.core.designsystem.theme.LocalColorTokens

/**
 * Color palette aligned with the Comprartir web design tokens.
 */
object ComprartirColors {
    val Surface = Color(0xFFF4F6F8)
    val SurfaceDark = Color(0xFF101522)
    val SurfaceCard = Color(0xFFFFFFFF)
    val SurfaceCardDark = Color(0xFF1E2536)
    val Border = Color(0xFFE5E7EB)
    val BorderHover = Color(0xFFD1D5DB)
    val TextPrimary = Color(0xFF0F172A)
    val TextPrimaryDark = Color(0xFFF4F6FB)
    val TextMuted = Color(0xFF6B7280)
    val TextMutedDark = Color(0xFFB5BDCC)
    val Placeholder = Color(0xFF9CA3AF)
    val White = Color(0xFFFFFFFF)
    val DarkNavy = Color(0xFF2A2A44)
    val SearchFilterPill = Color(0xFFA2A244)

    val BrandPrimary = Color(0xFF4DA851)
    val BrandPressed = Color(0xFF3E8E47)
    val BrandTint = Color(0xFFE9F7F0)
    val FocusHalo = Color(0x1F4DA851) // rgba(77,168,81,0.12)

    val Error = Color(0xFFB3261E)
    val ErrorContainer = Color(0xFFFCE8E6)
    val Success = BrandPrimary
    val Warning = Color(0xFFF6A609)
}

fun comprartirLightColorScheme(): ColorScheme = lightColorScheme(
    primary = ComprartirColors.BrandPrimary,
    onPrimary = ComprartirColors.White,
    primaryContainer = ComprartirColors.BrandTint,
    onPrimaryContainer = ComprartirColors.TextPrimary,
    secondary = ComprartirColors.BrandPressed,
    onSecondary = ComprartirColors.White,
    secondaryContainer = ComprartirColors.BrandTint,
    onSecondaryContainer = ComprartirColors.BrandPressed,
    tertiary = ComprartirColors.TextMuted,
    onTertiary = ComprartirColors.White,
    background = ComprartirColors.Surface,
    onBackground = ComprartirColors.TextPrimary,
    surface = ComprartirColors.Surface,
    onSurface = ComprartirColors.TextPrimary,
    surfaceVariant = ComprartirColors.Border,
    onSurfaceVariant = ComprartirColors.TextMuted,
    outline = ComprartirColors.Border,
    outlineVariant = ComprartirColors.Placeholder,
    scrim = Color(0x66000000),
    surfaceTint = ComprartirColors.BrandPrimary,
    inverseSurface = ComprartirColors.TextPrimary,
    inverseOnSurface = ComprartirColors.Surface,
    error = ComprartirColors.Error,
    onError = ComprartirColors.White,
    errorContainer = ComprartirColors.ErrorContainer,
    onErrorContainer = ComprartirColors.Error,
)

fun comprartirDarkColorScheme(): ColorScheme = darkColorScheme(
    primary = ComprartirColors.BrandPrimary,
    onPrimary = ComprartirColors.White,
    primaryContainer = ComprartirColors.BrandPressed,
    onPrimaryContainer = ComprartirColors.White,
    secondary = ComprartirColors.BrandTint,
    onSecondary = ComprartirColors.TextPrimary,
    secondaryContainer = ComprartirColors.BrandPrimary,
    onSecondaryContainer = ComprartirColors.White,
    tertiary = ComprartirColors.Placeholder,
    onTertiary = ComprartirColors.TextPrimary,
    background = ComprartirColors.SurfaceDark,
    onBackground = ComprartirColors.White,
    surface = Color(0xFF121826),
    onSurface = ComprartirColors.White,
    surfaceVariant = Color(0xFF1E2536),
    onSurfaceVariant = ComprartirColors.Placeholder,
    outline = Color(0xFF293248),
    outlineVariant = Color(0xFF1E2536),
    scrim = Color(0x99000000),
    surfaceTint = ComprartirColors.BrandPrimary,
    inverseSurface = ComprartirColors.White,
    inverseOnSurface = ComprartirColors.TextPrimary,
    error = ComprartirColors.Error,
    onError = ComprartirColors.White,
    errorContainer = ComprartirColors.Error,
    onErrorContainer = ComprartirColors.White,
)

val ColorScheme.brand: Color
    get() = ComprartirColors.BrandPrimary

val ColorScheme.brandDark: Color
    get() = ComprartirColors.BrandPressed

val ColorScheme.brandTint: Color
    get() = ComprartirColors.BrandTint

val ColorScheme.textPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalColorTokens.current.textPrimary

val ColorScheme.textMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalColorTokens.current.textMuted

val ColorScheme.surfaceCard: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalColorTokens.current.surfaceCard

val ColorScheme.borderDefault: Color
    get() = ComprartirColors.Border

val ColorScheme.darkNavy: Color
    get() = ComprartirColors.DarkNavy

val ColorScheme.searchFilterPill: Color
    get() = ComprartirColors.SearchFilterPill
