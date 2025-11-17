package com.comprartir.mobile.products.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.navigation.NavigationIntent
import com.comprartir.mobile.core.ui.rememberIsLandscape
import com.comprartir.mobile.core.ui.rememberIsTablet
import androidx.compose.material3.windowsizeclass.WindowSizeClass

@Composable
fun CategorizeProductsRoute(
    onNavigate: (NavigationIntent) -> Unit,
    windowSizeClass: WindowSizeClass? = null,
    viewModel: CategorizeProductsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CategorizeProductsScreen(
        state = state,
        windowSizeClass = windowSizeClass,
    )
}

@Composable
fun CategorizeProductsScreen(
    state: CategorizeProductsUiState,
    windowSizeClass: WindowSizeClass? = null,
) {
    val spacing = LocalSpacing.current
    val isLandscape = rememberIsLandscape()
    val isTablet = windowSizeClass?.let { rememberIsTablet(it) } ?: false
    val useWideLayout = isTablet || isLandscape
    val productColumns = when {
        isTablet -> 3
        isLandscape -> 2
        else -> 1
    }
    val horizontalPadding = if (isTablet) spacing.xl else spacing.large
    val verticalPadding = if (isTablet) spacing.large else spacing.medium
    if (useWideLayout) {
        val productWeight = if (isTablet) 0.55f else 0.5f
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            horizontalArrangement = Arrangement.spacedBy(if (isTablet) spacing.xl else spacing.large),
        ) {
            ProductListColumn(
                title = stringResource(id = R.string.header_assign_categories),
                products = state.products,
                columns = productColumns,
                modifier = Modifier
                    .weight(productWeight)
                    .fillMaxHeight(),
            )
            CategoryListColumn(
                categories = state.categories,
                isTabletLayout = isTablet,
                modifier = Modifier
                    .weight(1f - productWeight)
                    .fillMaxHeight(),
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            verticalArrangement = Arrangement.spacedBy(spacing.large),
        ) {
            ProductListColumn(
                title = stringResource(id = R.string.header_assign_categories),
                products = state.products,
                columns = productColumns,
                modifier = Modifier.fillMaxWidth(),
            )
            CategoryListColumn(
                categories = state.categories,
                isTabletLayout = isTablet,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ProductListColumn(
    title: String,
    products: List<com.comprartir.mobile.products.data.Product>,
    columns: Int = 1,
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
            if (columns > 1) {
                val gridSpacing = if (columns >= 3) spacing.medium else spacing.small
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(gridSpacing),
                    horizontalArrangement = Arrangement.spacedBy(gridSpacing),
                    verticalArrangement = Arrangement.spacedBy(gridSpacing),
                ) {
                    items(products, key = { it.id }) { product ->
                        Text(
                            text = product.name.ifBlank { stringResource(id = R.string.empty_product_name) },
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            } else {
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
    isTabletLayout: Boolean = false,
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
                .padding(horizontal = if (isTabletLayout) spacing.large else spacing.medium, vertical = spacing.medium),
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
