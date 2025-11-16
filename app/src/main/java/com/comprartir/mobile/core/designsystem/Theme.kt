package com.comprartir.mobile.core.designsystem

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.comprartir.mobile.core.designsystem.theme.DarkColorTokens
import com.comprartir.mobile.core.designsystem.theme.LightColorTokens
import com.comprartir.mobile.core.designsystem.theme.LocalColorTokens
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun ComprartirTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> comprartirDarkColorScheme()
        else -> comprartirLightColorScheme()
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
        }
    }

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setSystemBarsColor(color = colorScheme.background, darkIcons = !darkTheme)
    }

    val tokens = if (darkTheme) DarkColorTokens else LightColorTokens

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalElevations provides ElevationTokens(),
        LocalColorTokens provides tokens,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = comprartirTypography(),
            shapes = comprartirShapes(),
            content = content,
        )
    }
}
