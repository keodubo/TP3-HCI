package com.comprartir.mobile

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import com.comprartir.mobile.core.designsystem.ComprartirTheme
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.navigation.ComprartirNavHost
import com.comprartir.mobile.core.navigation.SessionViewModel
import com.comprartir.mobile.core.navigation.AppDestination
import com.comprartir.mobile.core.navigation.rememberComprartirAppState
import com.comprartir.mobile.core.ui.ResponsiveAppScaffold
import com.comprartir.mobile.core.util.FeatureFlags
import com.comprartir.mobile.shared.components.ComprartirTopBar

@Composable
fun ComprartirApp(
    windowSizeClass: WindowSizeClass,
    featureFlags: FeatureFlags = FeatureFlags.Disabled,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    ComprartirTheme {
        val appState = rememberComprartirAppState(
            windowSizeClass = windowSizeClass,
            isLandscape = isLandscape,
            featureFlags = featureFlags,
        )
        val spacing = LocalSpacing.current
        val sessionViewModel: SessionViewModel = hiltViewModel()
        val isAuthenticated by sessionViewModel.isAuthenticated.collectAsStateWithLifecycle()
        val navBackStackEntry by appState.navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val currentRouteBase = currentRoute?.substringBefore("?")
        val isAuthRoute = currentRouteBase == null || currentRouteBase in AuthRoutes

        LaunchedEffect(isAuthenticated) {
            // Only redirect to login if we're not authenticated
            // Check current route inside the effect to get the latest value
            val currentDest = appState.navController.currentBackStackEntry?.destination?.route
            val routeBase = currentDest?.substringBefore("?")
            if (!isAuthenticated && routeBase != null && routeBase !in AuthRoutes) {
                appState.navController.navigate(AppDestination.SignIn.route) {
                    popUpTo(AppDestination.SignIn.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        val horizontalPadding = when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Expanded -> spacing.xxl
            else -> spacing.mobileGutter
        }

        if (isAuthRoute) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                ComprartirNavHost(
                    appState = appState,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        } else {
            ResponsiveAppScaffold(
                appState = appState,
                isLandscape = isLandscape,
                topBar = {
                    ComprartirTopBar(
                        destinationRoute = appState.currentDestinationRoute,
                        showBack = appState.navController.previousBackStackEntry != null,
                        onBack = appState::onBack,
                        featureFlags = featureFlags,
                    )
                },
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxSize()
                            .padding(horizontal = horizontalPadding)
                            .widthIn(max = spacing.maxContentWidth),
                    ) {
                        ComprartirNavHost(
                            appState = appState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = paddingValues,
                        )
                    }
                }
            }
        }
    }
}

private val AuthRoutes = setOf(
    AppDestination.SignIn.route,
    AppDestination.Register.route,
    AppDestination.Verify.route,
    AppDestination.UpdatePassword.route,
)
