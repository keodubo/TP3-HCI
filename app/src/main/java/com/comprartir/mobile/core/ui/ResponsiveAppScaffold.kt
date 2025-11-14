package com.comprartir.mobile.core.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import com.comprartir.mobile.core.navigation.AppDestination
import com.comprartir.mobile.core.navigation.ComprartirAppState
import com.comprartir.mobile.R
import com.comprartir.mobile.core.ui.BottomNavItem
import com.comprartir.mobile.core.ui.ComprartirBottomNavBar
import com.comprartir.mobile.core.ui.primaryNavigationItems
import com.comprartir.mobile.core.navigation.isDestinationSelected

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponsiveAppScaffold(
    appState: ComprartirAppState,
    isLandscape: Boolean,
    topBar: @Composable () -> Unit,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val navigationItems = remember { primaryNavigationItems() }

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

    val useNavigationRail = appState.windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded || isLandscape

    when {
        useNavigationRail && showNavigation -> {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(modifier = Modifier.fillMaxHeight()) {
                    navigationItems.forEach { item ->
                        NavigationRailItem(
                            selected = isDestinationSelected(currentRoute, item.destination),
                            onClick = { onNavigate(item.destination) },
                            icon = { Icon(item.icon, contentDescription = stringResource(id = item.labelRes)) },
                            label = { Text(stringResource(id = item.labelRes)) },
                        )
                    }
                }
                Scaffold(
                    topBar = topBar,
                    floatingActionButton = floatingActionButton,
                    content = { padding ->
                        content(padding)
                    },
                )
            }
        }

        else -> {
            Scaffold(
                topBar = topBar,
                bottomBar = {
                    if (showNavigation) {
                        ComprartirBottomNavBar(
                            items = navigationItems,
                            currentRoute = currentRoute,
                            onNavigate = onNavigate,
                        )
                    }
                },
                floatingActionButton = floatingActionButton,
                content = { padding ->
                    content(padding)
                },
            )
        }
    }
}
