package com.comprartir.mobile.core.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import com.comprartir.mobile.core.navigation.AppDestination
import com.comprartir.mobile.core.navigation.ComprartirAppState

@Suppress("UnusedParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponsiveAppScaffold(
    appState: ComprartirAppState,
    isLandscape: Boolean,
    topBar: @Composable () -> Unit,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val navBackStackEntry by appState.navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val authRoutes = remember {
        setOf(
            AppDestination.SignIn.route,
            AppDestination.Register.route,
            AppDestination.Verify.route,
            AppDestination.UpdatePassword.route,
        )
    }
    val showNavigation = currentRoute != null && currentRoute !in authRoutes
    val onNavigate: (AppDestination) -> Unit = { destination ->
        if (currentRoute != destination.route) {
            appState.navigate(com.comprartir.mobile.core.navigation.NavigationIntent(destination))
        }
    }

    val isTablet = rememberIsTablet(appState.windowSizeClass)

    if (isTablet && showNavigation) {
        Row(modifier = Modifier.fillMaxSize()) {
            ComprartirNavigationRail(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(min = 88.dp, max = 96.dp),
                items = appState.navigationItems,
                currentRoute = currentRoute,
                onNavigate = onNavigate,
            )
            Scaffold(
                modifier = Modifier.weight(1f),
                topBar = topBar,
                floatingActionButton = floatingActionButton,
                content = content,
            )
        }
    } else {
        Scaffold(
            topBar = topBar,
            bottomBar = {
                if (showNavigation) {
                    ComprartirBottomNavBar(
                        items = appState.navigationItems,
                        currentRoute = currentRoute,
                        onNavigate = onNavigate,
                    )
                }
            },
            floatingActionButton = floatingActionButton,
            content = content,
        )
    }
}
