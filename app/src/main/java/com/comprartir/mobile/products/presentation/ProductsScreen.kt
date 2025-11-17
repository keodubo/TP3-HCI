package com.comprartir.mobile.products.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
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
import com.comprartir.mobile.core.ui.rememberIsLandscape
import com.comprartir.mobile.core.ui.rememberIsTablet

@Composable
fun ProductsRoute(
    onNavigate: (NavigationIntent) -> Unit,
    windowSizeClass: WindowSizeClass,
    viewModel: ProductsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isTablet = rememberIsTablet(windowSizeClass)
    ProductsScreen(
        state = state,
        isTablet = isTablet,
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
    isTablet: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onProductSelected: (String) -> Unit,
    onRefresh: () -> Unit,
    onClearError: () -> Unit,
) {
    val isLandscape = rememberIsLandscape()
    val columns = when {
        isTablet -> 4
        isLandscape -> 2
        else -> 1
    }
    if (columns > 1) {
        ProductsScreenGrid(
            state = state,
            onSearchQueryChanged = onSearchQueryChanged,
            onProductSelected = onProductSelected,
            onRefresh = onRefresh,
            onClearError = onClearError,
            columns = columns,
            isTablet = isTablet,
        )
    } else {
        ProductsScreenPortrait(
            state = state,
            onSearchQueryChanged = onSearchQueryChanged,
            onProductSelected = onProductSelected,
            onRefresh = onRefresh,
            onClearError = onClearError,
        )
    }
}

@Composable
private fun ProductsScreenPortrait(
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
        ProductsSearchField(
            state = state,
            onSearchQueryChanged = onSearchQueryChanged,
            onRefresh = onRefresh,
            onClearError = onClearError,
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
            items(state.filteredProducts, key = { it.id }) { product ->
                ProductCard(
                    name = product.name,
                    onClick = { onProductSelected(product.id) },
                )
            }
        }
    }
}

@Composable
private fun ProductsScreenGrid(
    state: ProductsUiState,
    onSearchQueryChanged: (String) -> Unit,
    onProductSelected: (String) -> Unit,
    onRefresh: () -> Unit,
    onClearError: () -> Unit,
    columns: Int,
    isTablet: Boolean,
) {
    val spacing = LocalSpacing.current
    val horizontalPadding = if (isTablet) spacing.xl else spacing.large
    val verticalPadding = if (isTablet) spacing.large else spacing.medium
    val horizontalSpacing = if (isTablet) spacing.large else spacing.medium
    val verticalSpacing = if (isTablet) spacing.large else spacing.medium
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = verticalPadding),
        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }, key = "search") {
            ProductsSearchField(
                state = state,
                onSearchQueryChanged = onSearchQueryChanged,
                onRefresh = onRefresh,
                onClearError = onClearError,
            )
        }
        items(state.filteredProducts, key = { it.id }) { product ->
            ProductCard(
                name = product.name,
                onClick = { onProductSelected(product.id) },
            )
        }
    }
}

@Composable
private fun ProductsSearchField(
    state: ProductsUiState,
    onSearchQueryChanged: (String) -> Unit,
    onRefresh: () -> Unit,
    onClearError: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        ComprartirOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.searchQuery,
            onValueChange = onSearchQueryChanged,
            isError = state.errorMessage != null,
            placeholder = { Text(text = stringResource(id = R.string.hint_search_products)) },
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
    }
}

@Composable
private fun ProductCard(
    name: String,
    onClick: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(spacing.medium),
            text = name.ifBlank { stringResource(id = R.string.empty_product_name) },
        )
    }
}
