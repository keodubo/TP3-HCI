package com.comprartir.mobile.purchase.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.shared.components.EmptyStateMessage
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun HistoryRoute(
    contentPadding: PaddingValues,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryScreen(
        state = state,
        contentPadding = contentPadding,
        onRetry = viewModel::retry,
        onRefresh = viewModel::refresh,
        onDismissError = viewModel::clearError,
    )
}

@Composable
fun HistoryScreen(
    state: PurchaseHistoryUiState,
    contentPadding: PaddingValues,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        when {
            state.isLoading && state.sections.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            state.errorMessage != null && state.sections.isEmpty() -> {
                HistoryError(
                    message = state.errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            state.sections.isEmpty() -> {
                EmptyStateMessage(
                    title = stringResource(id = R.string.history_empty_title),
                    subtitle = stringResource(id = R.string.history_empty_subtitle),
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            else -> {
                HistoryList(
                    state = state,
                    onRetry = onRetry,
                    onRefresh = onRefresh,
                    onDismissError = onDismissError,
                )
            }
        }
    }
}

@Composable
private fun HistoryList(
    state: PurchaseHistoryUiState,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onDismissError: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val locale = Locale.getDefault()
    val dateFormatter = rememberDateFormatter(locale)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.large, vertical = spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        item(key = "header") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                Text(
                    text = stringResource(id = R.string.title_history),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(id = R.string.history_header_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (state.isRefreshing) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }

        if (state.errorMessage != null) {
            item(key = "error-banner") {
                HistoryError(
                    message = state.errorMessage,
                    onRetry = {
                        onDismissError()
                        onRetry()
                    },
                )
            }
        }

        items(
            items = state.sections,
            key = { section -> section.date.toEpochDay() },
        ) { section ->
            Column(
                verticalArrangement = Arrangement.spacedBy(spacing.small),
            ) {
                Text(
                    text = section.date.format(dateFormatter),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(spacing.small),
                ) {
                    section.purchases.forEach { purchase ->
                        PurchaseCard(
                            item = purchase,
                            locale = locale,
                        )
                    }
                }
            }
        }

        item(key = "refresh-cta") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.small),
                horizontalArrangement = Arrangement.Center,
            ) {
                Button(onClick = onRefresh) {
                    Text(text = stringResource(id = R.string.history_action_refresh))
                }
            }
        }
    }
}

@Composable
private fun PurchaseCard(
    item: PurchaseHistoryItem,
    locale: Locale,
) {
    val spacing = LocalSpacing.current
    val timeFormatter = rememberTimeFormatter(locale)
    val timeText = item.purchasedAt.atZone(ZoneId.systemDefault()).format(timeFormatter)
    val listName = item.listName?.ifBlank { null } ?: stringResource(
        id = R.string.history_fallback_list_name,
        item.listId.takeLast(6),
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = listName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(id = R.string.history_purchase_time, timeText),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (item.isRecurring) {
                    AssistChip(
                        onClick = {},
                        label = { Text(text = stringResource(id = R.string.history_recurring_badge)) },
                    )
                }
            }
            Text(
                text = stringResource(
                    id = R.string.history_items_summary,
                    item.acquiredItems,
                    item.totalItems,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (item.restoredAt != null) {
                val restoredText = item.restoredAt.atZone(ZoneId.systemDefault()).format(timeFormatter)
                Text(
                    text = stringResource(id = R.string.history_restored_label, restoredText),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HistoryError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onRetry) {
                Text(text = stringResource(id = R.string.common_retry))
            }
        }
    }
}

@Composable
private fun rememberDateFormatter(locale: Locale): DateTimeFormatter {
    return remember(locale) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale)
    }
}

@Composable
private fun rememberTimeFormatter(locale: Locale): DateTimeFormatter {
    return remember(locale) {
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)
    }
}
