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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.pantry.data.PantrySummary
import com.comprartir.mobile.shared.components.EmptyStateMessage
import com.comprartir.mobile.core.ui.rememberIsLandscape
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
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(context.getString(message))
        viewModel.onSnackbarConsumed()
    }

    HistoryScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        contentPadding = contentPadding,
        onRetry = viewModel::retry,
        onRefresh = viewModel::refresh,
        onDismissError = viewModel::clearError,
        onRestoreCompletedList = viewModel::restoreList,
        onAddListToPantry = viewModel::showAddToPantryDialog,
        onSelectPantry = viewModel::addListToPantry,
        onDismissAddToPantryDialog = viewModel::dismissAddToPantryDialog,
    )
}

@Composable
fun HistoryScreen(
    state: PurchaseHistoryUiState,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onDismissError: () -> Unit,
    onRestoreCompletedList: (String) -> Unit,
    onAddListToPantry: (String) -> Unit,
    onSelectPantry: (String) -> Unit,
    onDismissAddToPantryDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val isLandscape = rememberIsLandscape()
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        when {
            state.isLoading && state.sections.isEmpty() && state.completedLists.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            state.errorMessage != null && state.sections.isEmpty() && state.completedLists.isEmpty() -> {
                HistoryError(
                    message = state.errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            state.sections.isEmpty() && state.completedLists.isEmpty() -> {
                EmptyStateMessage(
                    title = stringResource(id = R.string.history_empty_title),
                    subtitle = stringResource(id = R.string.history_empty_subtitle),
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            else -> {
                HistoryList(
                    state = state,
                    isLandscape = isLandscape,
                    onRetry = onRetry,
                    onRefresh = onRefresh,
                    onDismissError = onDismissError,
                    onRestoreCompletedList = onRestoreCompletedList,
                    onAddListToPantry = onAddListToPantry,
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = spacing.large)
                .padding(bottom = spacing.large),
        )

        if (state.showAddToPantryDialog) {
            AddToPantryDialog(
                pantries = state.pantries,
                isLoading = state.isAddingToPantry,
                onSelectPantry = onSelectPantry,
                onDismiss = onDismissAddToPantryDialog,
            )
        }
    }
}

@Composable
private fun HistoryList(
    state: PurchaseHistoryUiState,
    isLandscape: Boolean,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onDismissError: () -> Unit,
    onRestoreCompletedList: (String) -> Unit,
    onAddListToPantry: (String) -> Unit,
) {
    val spacing = LocalSpacing.current
    val locale = Locale.getDefault()
    val dateFormatter = rememberDateFormatter(locale)
    if (isLandscape) {
        HistoryListLandscape(
            state = state,
            locale = locale,
            dateFormatter = dateFormatter,
            onRetry = onRetry,
            onRefresh = onRefresh,
            onDismissError = onDismissError,
            onRestoreCompletedList = onRestoreCompletedList,
            onAddListToPantry = onAddListToPantry,
        )
    } else {
        HistoryListPortrait(
            state = state,
            locale = locale,
            dateFormatter = dateFormatter,
            onRetry = onRetry,
            onRefresh = onRefresh,
            onDismissError = onDismissError,
            onRestoreCompletedList = onRestoreCompletedList,
            onAddListToPantry = onAddListToPantry,
        )
    }
}

@Composable
private fun HistoryListPortrait(
    state: PurchaseHistoryUiState,
    locale: Locale,
    dateFormatter: DateTimeFormatter,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onDismissError: () -> Unit,
    onRestoreCompletedList: (String) -> Unit,
    onAddListToPantry: (String) -> Unit,
) {
    val spacing = LocalSpacing.current
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

        if (state.completedLists.isNotEmpty()) {
            item(key = "completed-header") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs),
                ) {
                    Text(
                        text = stringResource(id = R.string.history_completed_lists_section),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(id = R.string.history_completed_lists_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(
                items = state.completedLists,
                key = { it.id },
            ) { completed ->
                CompletedListCard(
                    item = completed,
                    onRestore = onRestoreCompletedList,
                    onAddToPantry = onAddListToPantry,
                    dateFormatter = dateFormatter,
                )
            }

            item(key = "completed-divider") {
                Divider(
                    modifier = Modifier.padding(vertical = spacing.small),
                    color = MaterialTheme.colorScheme.outlineVariant,
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
private fun HistoryListLandscape(
    state: PurchaseHistoryUiState,
    locale: Locale,
    dateFormatter: DateTimeFormatter,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onDismissError: () -> Unit,
    onRestoreCompletedList: (String) -> Unit,
    onAddListToPantry: (String) -> Unit,
) {
    val spacing = LocalSpacing.current
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.large, vertical = spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }, key = "header") {
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
            item(span = { GridItemSpan(maxLineSpan) }, key = "error-banner") {
                HistoryError(
                    message = state.errorMessage,
                    onRetry = {
                        onDismissError()
                        onRetry()
                    },
                )
            }
        }
        if (state.completedLists.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "completed-header") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs),
                ) {
                    Text(
                        text = stringResource(id = R.string.history_completed_lists_section),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(id = R.string.history_completed_lists_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(
                items = state.completedLists,
                key = { "completed-${it.id}" },
                span = { GridItemSpan(maxLineSpan) },
            ) { completed ->
                CompletedListCard(
                    item = completed,
                    onRestore = onRestoreCompletedList,
                    onAddToPantry = onAddListToPantry,
                    dateFormatter = dateFormatter,
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }, key = "completed-divider") {
                Divider(
                    modifier = Modifier.padding(vertical = spacing.small),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
        state.sections.forEach { section ->
            item(span = { GridItemSpan(maxLineSpan) }, key = "section-${section.date.toEpochDay()}") {
                Text(
                    text = section.date.format(dateFormatter),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            section.purchases.forEach { purchase ->
                item(
                    key = "purchase-${purchase.id}",
                    span = { GridItemSpan(1) },
                ) {
                    PurchaseCard(
                        item = purchase,
                        locale = locale,
                    )
                }
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }, key = "refresh-cta") {
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
    val timeText = item.purchasedAt
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(timeFormatter)
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
        }
    }
}

@Composable
private fun CompletedListCard(
    item: CompletedListHistoryItem,
    onRestore: (String) -> Unit,
    onAddToPantry: (String) -> Unit,
    dateFormatter: DateTimeFormatter,
) {
    val spacing = LocalSpacing.current
    val completedText = item.completedAt
        ?.atZone(ZoneId.systemDefault())
        ?.toLocalDate()
        ?.format(dateFormatter)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    completedText?.let { text ->
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Text(
                text = stringResource(id = R.string.history_completed_list_items, item.totalItems),
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.small, Alignment.CenterHorizontally),
            ) {
                FilledTonalButton(
                    onClick = { onAddToPantry(item.id) },
                    shape = RoundedCornerShape(50),
                ) {
                    Text(text = stringResource(id = R.string.history_add_to_pantry_button))
                }
                FilledTonalButton(
                    onClick = { onRestore(item.id) },
                    shape = RoundedCornerShape(50),
                ) {
                    Text(text = stringResource(id = R.string.history_restore_button))
                }
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

@Composable
private fun AddToPantryDialog(
    pantries: List<PantrySummary>,
    isLoading: Boolean,
    onSelectPantry: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val spacing = LocalSpacing.current
    androidx.compose.material3.AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        confirmButton = {},
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = { if (!isLoading) onDismiss() }) {
                Text(text = stringResource(id = R.string.dialog_cancel))
            }
        },
        title = {
            Text(
                text = stringResource(id = R.string.history_add_to_pantry_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.small),
                verticalArrangement = Arrangement.spacedBy(spacing.small),
            ) {
                Text(
                    text = stringResource(id = R.string.history_add_to_pantry_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (pantries.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.lists_complete_dialog_no_pantry),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                } else {
                    pantries.forEach { pantry: PantrySummary ->
                        PantryOption(
                            name = pantry.name,
                            isLoading = isLoading,
                            onClick = { onSelectPantry(pantry.id) },
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun PantryOption(
    name: String,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(end = 8.dp),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Text(text = name)
    }
}
