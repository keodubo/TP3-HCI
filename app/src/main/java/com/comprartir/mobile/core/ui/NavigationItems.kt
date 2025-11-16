package com.comprartir.mobile.core.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.List
import com.comprartir.mobile.R
import com.comprartir.mobile.core.navigation.AppDestination
import com.comprartir.mobile.core.util.FeatureFlags

fun primaryNavigationItems(featureFlags: FeatureFlags): List<BottomNavItem> {
    val items = mutableListOf(
        BottomNavItem(AppDestination.Dashboard, R.string.title_dashboard, Icons.Rounded.Home),
        BottomNavItem(AppDestination.Lists, R.string.title_lists, Icons.Rounded.List),
        BottomNavItem(AppDestination.Categories, R.string.title_categories, Icons.Rounded.Label),
        BottomNavItem(AppDestination.Pantry, R.string.title_pantry, Icons.Rounded.Inventory2),
    )
    if (featureFlags.rf13History) {
        items += BottomNavItem(AppDestination.OptionalHistory, R.string.title_history, Icons.Rounded.History)
    }
    return items
}
