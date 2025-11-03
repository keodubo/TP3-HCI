package com.comprartir.mobile.shared.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.navigation.AppDestination
import com.comprartir.mobile.core.navigation.NavigationIntent
import com.comprartir.mobile.shared.state.DashboardViewModel
import com.comprartir.mobile.R

@Composable
fun DashboardRoute(
    onNavigate: (NavigationIntent) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    DashboardScreen(
        onListsClick = { onNavigate(NavigationIntent(AppDestination.Lists)) },
        onProductsClick = { onNavigate(NavigationIntent(AppDestination.Products)) },
        onPantryClick = { onNavigate(NavigationIntent(AppDestination.Pantry)) },
    )
}

@Composable
fun DashboardScreen(
    onListsClick: () -> Unit,
    onProductsClick: () -> Unit,
    onPantryClick: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.large),
        verticalArrangement = Arrangement.spacedBy(spacing.large),
    ) {
        Text(text = stringResource(id = R.string.dashboard_heading))
        Button(onClick = onListsClick) {
            Text(text = stringResource(id = R.string.dashboard_lists))
        }
        Button(onClick = onProductsClick) {
            Text(text = stringResource(id = R.string.dashboard_products))
        }
        Button(onClick = onPantryClick) {
            Text(text = stringResource(id = R.string.dashboard_pantry))
        }
    }
}
