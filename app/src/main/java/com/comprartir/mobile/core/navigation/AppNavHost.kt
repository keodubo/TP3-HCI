package com.comprartir.mobile.core.navigation

import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.comprartir.mobile.auth.presentation.ForgotPasswordRoute
import com.comprartir.mobile.auth.presentation.ResetPasswordRoute
import com.comprartir.mobile.auth.presentation.UpdatePasswordRoute
import com.comprartir.mobile.feature.auth.login.LoginRoute
import com.comprartir.mobile.feature.auth.register.RegisterRoute
import com.comprartir.mobile.feature.auth.verify.VerifyRoute
import com.comprartir.mobile.feature.home.ui.HomeRoute
import com.comprartir.mobile.feature.listdetail.ui.ListDetailRoute
import com.comprartir.mobile.feature.listdetail.navigation.ListDetailDestination
import com.comprartir.mobile.feature.lists.navigation.listsScreen
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
    contentPadding: PaddingValues = PaddingValues(),
) {
    NavHost(
        navController = appState.navController,
        startDestination = AppDestination.SignIn.route,
        modifier = modifier,
    ) {
        authGraph(appState, contentPadding)
        profileGraph(contentPadding, appState)
        productsGraph(appState)
        listsGraph(appState, contentPadding)
        settingsGraph()
        pantryGraph(appState)
        optionalFeaturesGraph(appState)
    }
}

private fun NavGraphBuilder.authGraph(
    appState: ComprartirAppState,
    contentPadding: PaddingValues,
) {
    composable(AppDestination.SignIn.route) {
        LoginRoute(
            onRecoverPassword = { appState.navigate(NavigationIntent(AppDestination.ForgotPassword)) },
            onRegister = { appState.navigate(NavigationIntent(AppDestination.Register)) },
            onSubmit = {
                println("AppNavHost: onSubmit called, navigating to Dashboard")
                appState.navController.navigate(AppDestination.Dashboard.route) {
                    popUpTo(AppDestination.SignIn.route) { inclusive = true }
                    launchSingleTop = true
                }
                println("AppNavHost: Navigation to Dashboard completed")
            },
        )
    }
    composable(AppDestination.ForgotPassword.route) {
        ForgotPasswordRoute(
            onNavigateToLogin = { appState.navigate(NavigationIntent(AppDestination.SignIn)) },
            onNavigateToResetPassword = { email ->
                val encodedEmail = Uri.encode(email)
                val route = "${AppDestination.ResetPassword.route}?email=$encodedEmail"
                appState.navController.navigate(route) {
                    popUpTo(AppDestination.ForgotPassword.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }
    composable(
        route = AppDestination.ResetPassword.route + "?email={email}",
        arguments = listOf(
            navArgument("email") {
                type = NavType.StringType
                defaultValue = ""
            },
        ),
    ) {
        ResetPasswordRoute(
            email = it.arguments?.getString("email"),
            onNavigateToLogin = {
                appState.navController.navigate(AppDestination.SignIn.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }
    composable(AppDestination.Dashboard.route) {
        HomeRoute(
            onNavigate = appState::navigate,
            windowSizeClass = appState.windowSizeClass,
            contentPadding = contentPadding,
        )
    }
    composable(AppDestination.Register.route) {
        RegisterRoute(
            onNavigateToLogin = { appState.navigate(NavigationIntent(AppDestination.SignIn)) },
            onNavigateToVerify = { email ->
                println("AppNavHost: Navigating to verify with email: $email")
                val encodedEmail = Uri.encode(email)
                val route = "${AppDestination.Verify.route}?email=$encodedEmail"
                println("AppNavHost: Full route: $route")
                appState.navController.navigate(route) {
                    popUpTo(AppDestination.Register.route) { inclusive = true }
                    launchSingleTop = true
                }
            },
        )
    }
    composable(
        route = AppDestination.Verify.route + "?email={email}",
        arguments = listOf(
            navArgument("email") { type = NavType.StringType },
        ),
    ) {
        VerifyRoute(
            onNavigateToLogin = { appState.navigate(NavigationIntent(AppDestination.SignIn)) },
            onVerifySuccess = {
                appState.navController.navigate(AppDestination.Dashboard.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            },
        )
    }
    composable(AppDestination.UpdatePassword.route) {
        UpdatePasswordRoute(onNavigate = appState::navigate)
    }
    composable(AppDestination.AcquireProduct.route) {
        AcquireProductRoute(onNavigate = appState::navigate)
    }
}

private fun NavGraphBuilder.profileGraph(contentPadding: PaddingValues, appState: ComprartirAppState) {
    composable(AppDestination.Profile.route) { backStackEntry ->
        ProfileRoute(
            contentPadding = contentPadding,
            navController = appState.navController,
            onChangePasswordClick = {
                appState.navController.navigate(AppDestination.ChangePassword.route) {
                    launchSingleTop = true
                }
            },
            onLogout = {
                appState.navController.navigate(AppDestination.SignIn.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }
    
    composable(AppDestination.ChangePassword.route) {
        com.comprartir.mobile.profile.presentation.ChangePasswordRoute(
            onNavigateBackWithSuccess = {
                // Set result in previous back stack entry's savedStateHandle
                appState.navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("password_changed", true)
                appState.navController.popBackStack()
            },
            onNavigateBack = {
                appState.navController.popBackStack()
            }
        )
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

private fun NavGraphBuilder.listsGraph(
    appState: ComprartirAppState,
    contentPadding: PaddingValues,
) {
    listsScreen(
        onNavigate = appState::navigate,
        contentPadding = contentPadding,
    )
    composable(
        route = AppDestination.ListDetails.route,
        arguments = listOf(
            navArgument(ListDetailDestination.listIdArg) {
                type = NavType.StringType
                defaultValue = ""
            },
        ),
    ) {
        ListDetailRoute(
            onBack = appState::onBack,
            windowSizeClass = appState.windowSizeClass,
            contentPadding = contentPadding,
        )
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
