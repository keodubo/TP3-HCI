package com.comprartir.mobile.products.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.comprartir.mobile.core.designsystem.ComprartirOutlinedTextField

@Composable
fun ProductsRoute(
    onNavigate: (NavigationIntent) -> Unit,
    viewModel: ProductsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProductsScreen(
        state = state,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onProductSelected = { productId ->
            // TODO: Navigate to product detail when implemented.
        },
        onRefresh = viewModel::refresh,
        onClearError = viewModel::clearError,
    )
}

@Composable
fun ProductsScreen(
    state: ProductsUiState,
    onSearchQueryChanged: (String) -> Unit,
    onProductSelected: (String) -> Unit,
    onRefresh: () -> Unit,
    onClearError: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.large, vertical = spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        ComprartirOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.searchQuery,
            onValueChange = onSearchQueryChanged,
            label = { Text(text = stringResource(id = R.string.hint_search_products)) },
            isError = state.errorMessage != null,
            supportingText = state.errorMessage?.let { message ->
                {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
        )
        if (state.isLoading && state.filteredProducts.isEmpty()) {
            CircularProgressIndicator()
        }
        if (state.errorMessage != null && state.filteredProducts.isEmpty()) {
            Text(
                text = state.errorMessage,
                color = MaterialTheme.colorScheme.error,
            )
            Button(onClick = {
                onClearError()
                onRefresh()
            }) {
                Text(text = stringResource(id = R.string.common_retry))
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
            items(state.filteredProducts, key = { it.id }) { product ->
                Card(onClick = { onProductSelected(product.id) }) {
                    Text(
                        modifier = Modifier.padding(spacing.medium),
                        text = product.name.ifBlank { stringResource(id = R.string.empty_product_name) },
                    )
                }
            }
        }
    }
}
