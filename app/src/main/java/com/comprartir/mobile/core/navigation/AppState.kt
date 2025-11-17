package com.comprartir.mobile.core.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.comprartir.mobile.core.util.FeatureFlags
import com.comprartir.mobile.core.ui.BottomNavItem
import com.comprartir.mobile.core.ui.primaryNavigationItems

data class ComprartirAppState(
    val navController: NavHostController,
    val windowSizeClass: WindowSizeClass,
    val isLandscape: Boolean,
    val featureFlags: FeatureFlags,
    val navigationItems: List<BottomNavItem>,
) {
    val currentDestinationRoute: String?
        get() = navController.currentBackStackEntry?.destination?.route

    fun navigate(intent: NavigationIntent) {
        val consumed = mutableSetOf<String>()
        var resolvedRoute = intent.destination.route
        intent.arguments.forEach { (key, value) ->
            val placeholder = "{$key}"
            if (resolvedRoute.contains(placeholder)) {
                resolvedRoute = resolvedRoute.replace(placeholder, value)
                consumed += key
            }
        }
        val queryArgs = intent.arguments.filterKeys { it !in consumed }
        val route = buildString {
            append(resolvedRoute)
            if (queryArgs.isNotEmpty()) {
                append("?")
                append(
                    queryArgs.entries.joinToString("&") { (key, value) ->
                        "$key=$value"
                    }
                )
            }
        }
        navController.navigate(route)
    }

    fun onBack() {
        navController.popBackStack()
    }
}

@Composable
fun rememberComprartirAppState(
    windowSizeClass: WindowSizeClass,
    isLandscape: Boolean,
    featureFlags: FeatureFlags,
    navController: NavHostController = rememberNavController(),
): ComprartirAppState {
    val navigationItems = remember(featureFlags) { primaryNavigationItems(featureFlags) }
    return remember(windowSizeClass, isLandscape, featureFlags, navController, navigationItems) {
        ComprartirAppState(
            navController = navController,
            windowSizeClass = windowSizeClass,
            isLandscape = isLandscape,
            featureFlags = featureFlags,
            navigationItems = navigationItems,
        )
    }
}
