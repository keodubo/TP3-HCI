package com.comprartir.mobile.products.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.navigation.NavigationIntent

@Composable
fun CategorizeProductsRoute(
    onNavigate: (NavigationIntent) -> Unit,
    viewModel: CategorizeProductsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CategorizeProductsScreen(state = state)
}

@Composable
fun CategorizeProductsScreen(state: CategorizeProductsUiState) {
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.large),
    ) {
        Text(text = stringResource(id = R.string.header_assign_categories))
        // TODO: Provide drag & drop categorization experience aligned with web UI.
    }
}
