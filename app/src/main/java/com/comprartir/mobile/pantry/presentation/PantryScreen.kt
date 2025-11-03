package com.comprartir.mobile.pantry.presentation

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.R
import com.comprartir.mobile.core.navigation.NavigationIntent
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PantryRoute(
    onNavigate: (NavigationIntent) -> Unit,
    viewModel: PantryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PantryScreen(
        state = state,
        onRefresh = viewModel::refresh,
        onClearError = viewModel::clearError,
    )
}

@Composable
fun PantryScreen(
    state: PantryUiState,
    onRefresh: () -> Unit,
    onClearError: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.large),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        Text(text = stringResource(id = R.string.pantry_heading))
        if (state.errorMessage != null) {
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
        if (state.isLoading && state.items.isEmpty()) {
            CircularProgressIndicator()
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
            items(state.items, key = { it.id }) { item ->
                Card {
                    Column(modifier = Modifier.padding(spacing.medium), verticalArrangement = Arrangement.spacedBy(spacing.tiny)) {
                        Text(text = item.name, style = MaterialTheme.typography.titleMedium)
                        val quantityLabel = if (item.unit.isNullOrBlank()) {
                            "${item.quantity}"
                        } else {
                            "${item.quantity} ${item.unit}"
                        }
                        Text(text = quantityLabel, style = MaterialTheme.typography.bodyMedium)
                        val expirationLabel = remember(item.expiresAt) {
                            item.expiresAt?.let { expiration ->
                                val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
                                formatter.format(expiration.atZone(ZoneId.systemDefault()).toLocalDate())
                            }
                        }
                        if (expirationLabel != null) {
                            Text(
                                text = stringResource(id = R.string.pantry_item_expires, expirationLabel),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}
