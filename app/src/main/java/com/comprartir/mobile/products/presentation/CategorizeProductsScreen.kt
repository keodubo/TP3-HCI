package com.comprartir.mobile.products.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.navigation.NavigationIntent
import com.comprartir.mobile.core.ui.rememberIsLandscape

@Composable
fun CategorizeProductsRoute(
    onNavigate: (NavigationIntent) -> Unit,
    viewModel: CategorizeProductsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CategorizeProductsScreen(state = state)
}

@Composable
fun CategorizeProductsScreen(
    state: CategorizeProductsUiState,
) {
    val spacing = LocalSpacing.current
    val isLandscape = rememberIsLandscape()
    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing.large),
            horizontalArrangement = Arrangement.spacedBy(spacing.large),
        ) {
            ProductListColumn(
                title = stringResource(id = R.string.header_assign_categories),
                products = state.products,
                modifier = Modifier.weight(1f),
            )
            CategoryListColumn(
                categories = state.categories,
                modifier = Modifier.weight(1f),
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.large),
        ) {
            ProductListColumn(
                title = stringResource(id = R.string.header_assign_categories),
                products = state.products,
                modifier = Modifier.fillMaxWidth(),
            )
            CategoryListColumn(
                categories = state.categories,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ProductListColumn(
    title: String,
    products: List<com.comprartir.mobile.products.data.Product>,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Card(
        modifier = modifier,
        shape = CardDefaults.shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Divider()
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(spacing.small),
            ) {
                items(products, key = { it.id }) { product ->
                    Text(
                        text = product.name.ifBlank { stringResource(id = R.string.empty_product_name) },
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            if (products.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.categorize_empty_products),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

@Composable
private fun CategoryListColumn(
    categories: List<com.comprartir.mobile.products.data.Category>,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Card(
        modifier = modifier,
        shape = CardDefaults.shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            Text(
                text = stringResource(id = R.string.title_categories),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Divider()
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(spacing.small),
            ) {
                items(categories, key = { it.id }) { category ->
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            if (categories.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.categorize_empty_categories),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}
