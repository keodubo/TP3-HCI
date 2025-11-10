package com.comprartir.mobile.feature.lists.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.comprartir.mobile.core.navigation.AppDestination
import com.comprartir.mobile.core.navigation.NavigationIntent
import com.comprartir.mobile.feature.lists.ui.ListsRoute

const val listsRoute = "feature/lists"

fun NavGraphBuilder.listsScreen(
    onNavigate: (NavigationIntent) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    composable(AppDestination.Lists.route) {
        ListsRoute(
            onNavigate = onNavigate,
            contentPadding = contentPadding,
        )
    }
}
