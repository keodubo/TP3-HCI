package com.comprartir.mobile.core.navigation

fun isDestinationSelected(currentRoute: String?, destination: AppDestination): Boolean {
    val base = currentRoute?.substringBefore("?") ?: return false
    return when (destination) {
        AppDestination.Lists -> base == AppDestination.Lists.route ||
            (base.startsWith("lists/") && base != AppDestination.OptionalHistory.route)
        else -> base == destination.route
    }
}
