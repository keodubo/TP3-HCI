package com.comprartir.mobile.core.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.comprartir.mobile.core.util.FeatureFlags

data class ComprartirAppState(
    val navController: NavHostController,
    val windowSizeClass: WindowSizeClass,
    val isLandscape: Boolean,
    val featureFlags: FeatureFlags,
) {
    val currentDestinationRoute: String?
        get() = navController.currentBackStackEntry?.destination?.route

    fun navigate(intent: NavigationIntent) {
        val route = buildString {
            append(intent.destination.route)
            if (intent.arguments.isNotEmpty()) {
                append("?")
                append(
                    intent.arguments.entries.joinToString("&") { (key, value) ->
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
): ComprartirAppState = remember(windowSizeClass, isLandscape, featureFlags, navController) {
    ComprartirAppState(
        navController = navController,
        windowSizeClass = windowSizeClass,
        isLandscape = isLandscape,
        featureFlags = featureFlags,
    )
}
