package com.comprartir.mobile.lists.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
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

@Composable
fun ListDetailsRoute(
    onNavigate: (NavigationIntent) -> Unit,
    viewModel: ListDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ListDetailsScreen(
        state = state,
        onToggleAcquired = viewModel::toggleAcquired,
        onRetry = {
            viewModel.clearError()
            viewModel.refresh()
        },
    )
}

@Composable
fun ListDetailsScreen(
    state: ListDetailsUiState,
    onToggleAcquired: (String, Boolean) -> Unit,
    onRetry: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.large),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        Text(text = state.title.ifBlank { stringResource(id = R.string.lists_default_title) })
        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
            )
            Button(onClick = onRetry) {
                Text(text = stringResource(id = R.string.common_retry))
            }
        }
        if (state.isLoading && state.items.isEmpty()) {
            CircularProgressIndicator()
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
            items(state.items, key = { it.id }) { item ->
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(spacing.tiny),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(spacing.tiny)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                val quantityText = if (item.quantity % 1.0 == 0.0) {
                                    item.quantity.toInt().toString()
                                } else {
                                    item.quantity.toString()
                                }
                                val unitText = item.unit?.takeIf { it.isNotBlank() }?.let { " $it" } ?: ""
                                Text(
                                    text = quantityText + unitText,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                if (!item.notes.isNullOrBlank()) {
                                    Text(
                                        text = item.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            Checkbox(
                                checked = item.isAcquired,
                                onCheckedChange = { onToggleAcquired(item.id, it) },
                            )
                        }
                    }
                }
            }
        }
    }
}
