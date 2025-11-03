package com.comprartir.mobile.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.comprartir.mobile.auth.presentation.RegisterRoute
import com.comprartir.mobile.auth.presentation.SignInRoute
import com.comprartir.mobile.auth.presentation.UpdatePasswordRoute
import com.comprartir.mobile.auth.presentation.VerifyRoute
import com.comprartir.mobile.shared.components.DashboardRoute
import com.comprartir.mobile.lists.presentation.ListDetailsRoute
import com.comprartir.mobile.lists.presentation.ListsRoute
import com.comprartir.mobile.lists.presentation.ShareListRoute
import com.comprartir.mobile.pantry.presentation.PantryRoute
import com.comprartir.mobile.products.presentation.CategorizeProductsRoute
import com.comprartir.mobile.products.presentation.ProductsRoute
import com.comprartir.mobile.profile.presentation.ProfileRoute
import com.comprartir.mobile.shared.components.OptionalFeaturePlaceholder
import com.comprartir.mobile.shared.settings.SettingsRoute
import com.comprartir.mobile.lists.presentation.AcquireProductRoute

@Composable
fun ComprartirNavHost(
    appState: ComprartirAppState,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
) {
    NavHost(
        navController = appState.navController,
        startDestination = AppDestination.SignIn.route,
        modifier = modifier,
    ) {
        authGraph(appState)
        profileGraph()
        productsGraph(appState)
        listsGraph(appState)
        settingsGraph()
        pantryGraph(appState)
        optionalFeaturesGraph(appState)
    }
}

private fun NavGraphBuilder.authGraph(appState: ComprartirAppState) {
    composable(AppDestination.SignIn.route) {
        SignInRoute(onNavigate = appState::navigate)
    }
    composable(AppDestination.Dashboard.route) {
        DashboardRoute(onNavigate = appState::navigate)
    }
    composable(AppDestination.Register.route) {
        RegisterRoute(onNavigate = appState::navigate)
    }
    composable(AppDestination.Verify.route) {
        VerifyRoute(onNavigate = appState::navigate)
    }
    composable(AppDestination.UpdatePassword.route) {
        UpdatePasswordRoute(onNavigate = appState::navigate)
    }
    composable(AppDestination.AcquireProduct.route) {
        AcquireProductRoute(onNavigate = appState::navigate)
    }
}

private fun NavGraphBuilder.profileGraph() {
    composable(AppDestination.Profile.route) {
        ProfileRoute()
    }
}

private fun NavGraphBuilder.productsGraph(appState: ComprartirAppState) {
    composable(AppDestination.Products.route) {
        ProductsRoute(onNavigate = appState::navigate)
    }
    composable(AppDestination.Categorize.route) {
        CategorizeProductsRoute(onNavigate = appState::navigate)
    }
}

private fun NavGraphBuilder.listsGraph(appState: ComprartirAppState) {
    composable(AppDestination.Lists.route) {
        ListsRoute(onNavigate = appState::navigate)
    }
    composable(
        route = AppDestination.ListDetails.route + "?listId={listId}",
        arguments = listOf(
            navArgument("listId") {
                type = NavType.StringType
                defaultValue = ""
            },
        ),
    ) {
        ListDetailsRoute(onNavigate = appState::navigate)
    }
    composable(
        route = AppDestination.ShareList.route + "?listId={listId}",
        arguments = listOf(
            navArgument("listId") {
                type = NavType.StringType
                defaultValue = ""
            },
        ),
    ) {
        ShareListRoute(onNavigate = appState::navigate)
    }
}

private fun NavGraphBuilder.settingsGraph() {
    composable(AppDestination.Settings.route) {
        SettingsRoute()
    }
}

private fun NavGraphBuilder.pantryGraph(appState: ComprartirAppState) {
    composable(AppDestination.Pantry.route) {
        PantryRoute(onNavigate = appState::navigate)
    }
}

private fun NavGraphBuilder.optionalFeaturesGraph(appState: ComprartirAppState) {
    if (appState.featureFlags.rf12PasswordRecovery) {
        composable(AppDestination.OptionalPasswordRecovery.route) {
            OptionalFeaturePlaceholder(featureLabel = com.comprartir.mobile.R.string.placeholder_password_recovery)
        }
    }
    if (appState.featureFlags.rf13History) {
        composable(AppDestination.OptionalHistory.route) {
            OptionalFeaturePlaceholder(featureLabel = com.comprartir.mobile.R.string.placeholder_history)
        }
    }
    if (appState.featureFlags.rf14RecurringLists) {
        composable(AppDestination.OptionalRecurringLists.route) {
            OptionalFeaturePlaceholder(featureLabel = com.comprartir.mobile.R.string.placeholder_recurring_lists)
        }
    }
    if (appState.featureFlags.rf15PantryProducts) {
        composable(AppDestination.OptionalPantryManagement.route) {
            OptionalFeaturePlaceholder(featureLabel = com.comprartir.mobile.R.string.placeholder_pantry_management)
        }
    }
}
