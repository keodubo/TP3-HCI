package com.comprartir.mobile.feature.lists.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.ComprartirPillShape
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.designsystem.brand
import com.comprartir.mobile.core.designsystem.brandTint
import com.comprartir.mobile.core.designsystem.surfaceCard
import com.comprartir.mobile.core.designsystem.textMuted
import com.comprartir.mobile.core.designsystem.borderDefault
import com.comprartir.mobile.core.designsystem.theme.ColorTokens
import com.comprartir.mobile.feature.lists.model.ListTypeFilter
import com.comprartir.mobile.feature.lists.model.ListsEvent
import com.comprartir.mobile.feature.lists.model.ListsSummaryUi
import com.comprartir.mobile.feature.lists.model.ListsUiState
import com.comprartir.mobile.feature.lists.model.ShoppingListUi
import com.comprartir.mobile.feature.lists.model.SortDirection
import com.comprartir.mobile.feature.lists.model.SortOption
import com.comprartir.mobile.feature.lists.ui.components.CreateListDialog

@Composable
fun ListsScreen(
    state: ListsUiState,
    onEvent: (ListsEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val layoutDirection = LocalLayoutDirection.current
    val combinedPadding = PaddingValues(
        start = contentPadding.calculateStartPadding(layoutDirection) + spacing.large,
        end = contentPadding.calculateEndPadding(layoutDirection) + spacing.large,
        top = contentPadding.calculateTopPadding() + spacing.large,
        bottom = contentPadding.calculateBottomPadding() + spacing.large,
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ColorTokens.NeutralSurface),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = combinedPadding,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { ListsTopBar() }
            item {
                ListsSearchCard(
                    query = state.searchQuery,
                    onQueryChange = { onEvent(ListsEvent.SearchQueryChanged(it)) },
                    onCreateList = { onEvent(ListsEvent.CreateList) },
                )
            }
            item {
                ListsFilterPanel(
                    isExpanded = state.isFiltersExpanded,
                    sortOption = state.sortOption,
                    sortDirection = state.sortDirection,
                    listType = state.listType,
                    onEvent = onEvent,
                )
            }
            item {
                ListsCarousel(
                    lists = state.lists,
                    onOpenList = { onEvent(ListsEvent.OpenList(it)) },
                    onCreateList = { onEvent(ListsEvent.CreateList) },
                )
            }
            item {
                ListsSummaryPanel(summary = state.summary)
            }
        }

        if (state.isLoading) {
            val loadingDescription = stringResource(id = R.string.cd_loading_home)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = loadingDescription },
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.brand)
            }
        }

        SnackbarHost(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = spacing.large, vertical = spacing.large),
            hostState = snackbarHostState,
        )
    }
    CreateListDialog(
        state = state.createListState,
        onNameChange = { onEvent(ListsEvent.CreateListNameChanged(it)) },
        onDescriptionChange = { onEvent(ListsEvent.CreateListDescriptionChanged(it)) },
        onRecurringChange = { onEvent(ListsEvent.CreateListRecurringChanged(it)) },
        onDismiss = { onEvent(ListsEvent.DismissCreateList) },
        onConfirm = { onEvent(ListsEvent.ConfirmCreateList) },
    )
}

@Composable
private fun ListsTopBar() {
    val spacing = LocalSpacing.current
    Column {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.large, vertical = spacing.medium),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.lists_breadcrumb_home),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.textMuted,
                    )
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.textMuted,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = stringResource(id = R.string.lists_screen_title),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.textMuted,
                    )
                }
                Text(
                    text = stringResource(id = R.string.lists_screen_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Divider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.borderDefault,
        )
    }
}

@Composable
private fun ListsSearchCard(
    query: String,
    onQueryChange: (String) -> Unit,
    onCreateList: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        val searchContentDescription = stringResource(id = R.string.lists_search_cd)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.large),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = searchContentDescription
                    },
                value = query,
                onValueChange = onQueryChange,
                shape = ComprartirPillShape,
                placeholder = { Text(text = stringResource(id = R.string.lists_search_placeholder)) },
                singleLine = true,
                maxLines = 1,
                label = { Text(text = searchContentDescription) },
            )
            Button(
                onClick = onCreateList,
                shape = ComprartirPillShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(text = stringResource(id = R.string.lists_new_list_button))
            }
        }
    }
}

@Composable
private fun ListsFilterPanel(
    isExpanded: Boolean,
    sortOption: SortOption,
    sortDirection: SortDirection,
    listType: ListTypeFilter,
    onEvent: (ListsEvent) -> Unit,
) {
    val spacing = LocalSpacing.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(id = R.string.lists_filters_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                val toggleLabel = if (isExpanded) {
                    stringResource(id = R.string.lists_filters_hide)
                } else {
                    stringResource(id = R.string.lists_filters_show)
                }
                val toggleDescription = stringResource(id = R.string.lists_filters_toggle_cd)
                TextButton(
                    onClick = { onEvent(ListsEvent.ToggleFilters) },
                    modifier = Modifier.semantics {
                        contentDescription = toggleDescription
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(text = toggleLabel)
                }
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
                    FilterSection(
                        label = stringResource(id = R.string.lists_filter_sort_label),
                        options = listOf(
                            SortOption.NAME to R.string.lists_sort_name,
                            SortOption.RECENT to R.string.lists_sort_recent,
                            SortOption.PROGRESS to R.string.lists_sort_progress,
                        ),
                        isSelected = { it == sortOption },
                        onSelected = { onEvent(ListsEvent.SortOptionSelected(it)) },
                    )
                    FilterSection(
                        label = stringResource(id = R.string.lists_filter_direction_label),
                        options = listOf(
                            SortDirection.ASCENDING to R.string.lists_direction_asc,
                            SortDirection.DESCENDING to R.string.lists_direction_desc,
                        ),
                        isSelected = { it == sortDirection },
                        onSelected = { onEvent(ListsEvent.SortDirectionSelected(it)) },
                    )
                    FilterSection(
                        label = stringResource(id = R.string.lists_filter_type_label),
                        options = listOf(
                            ListTypeFilter.ALL to R.string.lists_type_all,
                            ListTypeFilter.SHARED to R.string.lists_type_shared,
                            ListTypeFilter.PERSONAL to R.string.lists_type_personal,
                        ),
                        isSelected = { it == listType },
                        onSelected = { onEvent(ListsEvent.ListTypeSelected(it)) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> FilterSection(
    label: String,
    options: List<Pair<T, Int>>,
    isSelected: (T) -> Boolean,
    onSelected: (T) -> Unit,
) {
    val spacing = LocalSpacing.current
    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            options.forEach { (value, textRes) ->
                FilterChip(
                    selected = isSelected(value),
                    onClick = { onSelected(value) },
                    label = { Text(text = stringResource(id = textRes)) },
                    leadingIcon = if (isSelected(value)) {
                        {
                            Icon(
                                imageVector = Icons.Outlined.FilterList,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f),
                        selectedContainerColor = MaterialTheme.colorScheme.surface,
                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                        labelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        }
    }
}

@Composable
private fun ListsCarousel(
    lists: List<ShoppingListUi>,
    onOpenList: (String) -> Unit,
    onCreateList: () -> Unit,
) {
    if (lists.isEmpty()) {
        EmptyStateCard(onCreateList = onCreateList)
        return
    }
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.semantics { contentDescription = "lists-carousel" },
    ) {
        items(lists, key = { it.id }) { list ->
            ListCard(list = list, onOpen = { onOpenList(list.id) })
        }
    }
}

@Composable
private fun ListCard(
    list: ShoppingListUi,
    onOpen: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val openDescription = stringResource(id = R.string.lists_open_list_cd, list.name)
    Surface(
        modifier = Modifier
            .width(260.dp)
            .height(180.dp),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.borderDefault),
        color = MaterialTheme.colorScheme.surfaceCard,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing.medium),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Text(
                    text = list.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = list.updatedAgo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.textMuted,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = if (list.totalItems == 0) 0f else list.acquiredItems / list.totalItems.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(ComprartirPillShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.brandTint,
                )
                Text(
                    text = stringResource(
                        id = R.string.lists_progress_label,
                        list.acquiredItems,
                        list.totalItems,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.textMuted,
                )
            }
            TextButton(
                onClick = onOpen,
                shape = ComprartirPillShape,
                modifier = Modifier.semantics {
                    contentDescription = openDescription
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(text = stringResource(id = R.string.lists_open_list))
            }
        }
    }
}

@Composable
private fun EmptyStateCard(onCreateList: () -> Unit) {
    val spacing = LocalSpacing.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceCard),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.borderDefault),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            Text(
                text = stringResource(id = R.string.lists_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = stringResource(id = R.string.lists_empty_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.textMuted,
            )
            Button(
                onClick = onCreateList,
                shape = ComprartirPillShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(text = stringResource(id = R.string.lists_empty_action))
            }
        }
    }
}

@Composable
private fun ListsSummaryPanel(summary: ListsSummaryUi) {
    val spacing = LocalSpacing.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceCard),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            SummaryRow(
                title = stringResource(id = R.string.lists_summary_shared),
                value = summary.sharedCount,
            )
            SummaryRow(
                title = stringResource(id = R.string.lists_summary_pending),
                value = summary.pendingItems,
            )
            SummaryRow(
                title = stringResource(id = R.string.lists_summary_recurring),
                value = summary.recurringReminders,
            )
        }
    }
}

@Composable
private fun SummaryRow(
    title: String,
    value: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
        )
        Surface(
            color = MaterialTheme.colorScheme.brandTint,
            shape = ComprartirPillShape,
        ) {
            Text(
                text = value.toString(),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.brand,
            )
        }
    }
}
