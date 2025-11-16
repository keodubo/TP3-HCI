package com.comprartir.mobile.pantry.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.designsystem.brand
import com.comprartir.mobile.core.designsystem.darkNavy
import com.comprartir.mobile.core.designsystem.surfaceCard
import com.comprartir.mobile.core.designsystem.textMuted
import com.comprartir.mobile.feature.lists.model.SortDirection
import com.comprartir.mobile.core.navigation.NavigationIntent
import com.comprartir.mobile.shared.components.AddFab
import com.comprartir.mobile.pantry.data.PantryItem
import com.comprartir.mobile.pantry.data.PantrySummary
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
        onNavigate = onNavigate,
        onRefresh = viewModel::refresh,
        onClearError = viewModel::clearError,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onToggleFilters = viewModel::toggleFilters,
        onSortOptionChange = viewModel::onSortOptionChange,
        onSortDirectionChange = viewModel::onSortDirectionChange,
        onPantryTypeFilterChange = viewModel::onPantryTypeFilterChange,
        onClearFilters = viewModel::clearFilters,
        onShowPantryDialog = viewModel::showPantryDialog,
        onPantryNameChange = viewModel::onPantryNameChanged,
        onPantryDescriptionChange = viewModel::onPantryDescriptionChanged,
        onSavePantry = viewModel::savePantry,
        onDeletePantry = viewModel::deleteCurrentPantry,
        onDismissPantryDialog = viewModel::dismissPantryDialog,
    )
}

@Composable
fun PantryScreen(
    state: PantryUiState,
    onNavigate: (NavigationIntent) -> Unit,
    onRefresh: () -> Unit,
    onClearError: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleFilters: () -> Unit,
    onSortOptionChange: (PantrySortOption) -> Unit,
    onSortDirectionChange: (SortDirection) -> Unit,
    onPantryTypeFilterChange: (PantryTypeFilter) -> Unit,
    onClearFilters: () -> Unit,
    onShowPantryDialog: (String?) -> Unit,
    onPantryNameChange: (String) -> Unit,
    onPantryDescriptionChange: (String) -> Unit,
    onSavePantry: () -> Unit,
    onDeletePantry: () -> Unit,
    onDismissPantryDialog: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val filteredPantries = remember(
        state.pantries,
        state.searchQuery,
        state.sortOption,
        state.sortDirection,
        state.pantryTypeFilter,
        state.allItems,
    ) {
        state.pantries
            .filter { pantry ->
                if (state.searchQuery.isNotBlank()) {
                    pantry.name.contains(state.searchQuery, ignoreCase = true) ||
                        (pantry.description?.contains(state.searchQuery, ignoreCase = true) == true)
                } else {
                    true
                }
            }
            .filter { pantry ->
                when (state.pantryTypeFilter) {
                    PantryTypeFilter.ALL -> true
                    PantryTypeFilter.SHARED -> pantry.sharedUsers.isNotEmpty()
                    PantryTypeFilter.PERSONAL -> pantry.sharedUsers.isEmpty()
                }
            }
            .sortedWith(
                compareBy<PantrySummary> { pantry ->
                    when (state.sortOption) {
                        PantrySortOption.NAME -> pantry.name
                        PantrySortOption.RECENT -> pantry.id
                        PantrySortOption.ITEM_COUNT -> {
                            state.allItems.count { it.pantryId == pantry.id }
                                .toString()
                                .padStart(10, '0')
                        }
                    }
                }.let { comparator ->
                    if (state.sortDirection == SortDirection.DESCENDING) {
                        comparator.reversed()
                    } else {
                        comparator
                    }
                }
            )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    start = spacing.large,
                    end = spacing.large,
                    top = spacing.medium,
                    bottom = 100.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(spacing.small),
            ) {
            // Header with refresh button
            item(key = "header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = spacing.small),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = R.string.pantry_heading),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = stringResource(id = R.string.common_retry),
                        )
                    }
                }
            }

            // Error banner
            if (state.errorMessage != null) {
                item(key = "error") {
                    ErrorBanner(
                        message = state.errorMessage,
                        onDismiss = onClearError,
                    )
                }
            }

            // Search and filters pill
            item(key = "search-filter") {
                SearchAndFilterBar(
                    searchQuery = state.searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    onFilterClick = onToggleFilters,
                )
            }

            // Filter panel
            item(key = "filters") {
                FilterPanel(
                    isExpanded = state.isFiltersExpanded,
                    sortOption = state.sortOption,
                    sortDirection = state.sortDirection,
                    pantryType = state.pantryTypeFilter,
                    onSortOptionChange = onSortOptionChange,
                    onSortDirectionChange = onSortDirectionChange,
                    onPantryTypeChange = onPantryTypeFilterChange,
                    onClearFilters = onClearFilters,
                )
            }

            // Empty state
            if (filteredPantries.isEmpty()) {
                item(key = "empty") {
                    EmptyPantryState(onCreatePantry = { onShowPantryDialog(null) })
                }
            } else {
                // Pantry cards
                items(filteredPantries, key = { it.id }) { pantry ->
                    val pantryItemCount = state.allItems.count { it.pantryId == pantry.id }
                    PantryCard(
                        pantry = pantry,
                        itemCount = pantryItemCount,
                        onPantryClick = {
                            onNavigate(
                                NavigationIntent(
                                    destination = com.comprartir.mobile.core.navigation.AppDestination.PantryDetail,
                                    arguments = mapOf("pantryId" to pantry.id),
                                )
                            )
                        },
                        onEditClick = { onShowPantryDialog(pantry.id) },
                        onDeleteClick = {
                            // TODO: Show confirmation dialog
                            onDeletePantry()
                        },
                        onShareClick = { /* TODO: Navigate to share screen */ },
                        showManagementFeatures = state.showManagementFeatures,
                    )
                }
            }
        }
        }
        
        // FAB positioned absolutely to avoid being hidden by bottom nav
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 3.dp, bottom = 153.dp)
        ) {
            AddFab(
                onClick = { onShowPantryDialog(null) },
                contentDescription = stringResource(id = R.string.pantry_add_pantry),
                modifier = Modifier.size(64.dp),
            )
        }
    }

    if (state.pantryDialog.isVisible) {
        PantryDialog(
            state = state.pantryDialog,
            onNameChange = onPantryNameChange,
            onDescriptionChange = onPantryDescriptionChange,
            onDismiss = onDismissPantryDialog,
            onSave = onSavePantry,
            onDelete = if (state.pantryDialog.pantryId != null) onDeletePantry else null,
        )
    }
}

@Composable
private fun PantryCard(
    pantry: PantrySummary,
    itemCount: Int,
    onPantryClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit,
    showManagementFeatures: Boolean,
) {
    val spacing = LocalSpacing.current
    var showMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onPantryClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.brand.copy(alpha = 0.2f)
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Header: Name and menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = pantry.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (itemCount > 0) {
                        val countDescription = stringResource(id = R.string.pantry_item_count, itemCount)
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.darkNavy,
                            modifier = Modifier.semantics {
                                contentDescription = countDescription
                            },
                        ) {
                            Text(
                                text = itemCount.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }

                    if (showManagementFeatures) {
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(24.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.MoreVert,
                                    contentDescription = stringResource(id = R.string.lists_menu_cd),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(Color.White),
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(spacing.small),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Edit,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                            )
                                            Text(text = stringResource(id = R.string.pantry_edit_pantry))
                                        }
                                    },
                                    onClick = {
                                        showMenu = false
                                        onEditClick()
                                    },
                                    colors = MenuDefaults.itemColors(),
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(spacing.small),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Share,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                            )
                                            Text(text = stringResource(id = R.string.lists_share))
                                        }
                                    },
                                    onClick = {
                                        showMenu = false
                                        onShareClick()
                                    },
                                    colors = MenuDefaults.itemColors(),
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(spacing.small),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Delete,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colorScheme.error,
                                            )
                                            Text(
                                                text = stringResource(id = R.string.pantry_delete_pantry),
                                                color = MaterialTheme.colorScheme.error,
                                            )
                                        }
                                    },
                                    onClick = {
                                        showMenu = false
                                        onDeleteClick()
                                    },
                                    colors = MenuDefaults.itemColors(),
                                )
                            }
                        }
                    }
                }
            }

            // Description
            if (!pantry.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(spacing.small))
                Text(
                    text = pantry.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.textMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Item count badge (Empty state)
            Spacer(modifier = Modifier.height(spacing.small))
            if (itemCount == 0) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = stringResource(id = R.string.lists_empty_badge),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            } else {
                Text(
                    text = stringResource(id = R.string.pantry_item_count, itemCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.textMuted,
                )
            }

            // Shared users count
            if (showManagementFeatures && pantry.sharedUsers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(spacing.small))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.tiny),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.brand,
                    )
                    Text(
                        text = stringResource(id = R.string.pantry_shared_count, pantry.sharedUsers.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.textMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchAndFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    var isSearchExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            isSearchExpanded = true
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!isSearchExpanded) {
            Text(
                text = stringResource(id = R.string.pantry_all_pantries),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }

        AnimatedVisibility(
            visible = isSearchExpanded,
            modifier = Modifier.weight(1f),
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(id = R.string.pantry_search_placeholder)) },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = {
                        onSearchQueryChange("")
                        isSearchExpanded = false
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(id = R.string.common_dismiss),
                        )
                    }
                },
                shape = RoundedCornerShape(50.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.brand,
                    unfocusedBorderColor = MaterialTheme.colorScheme.brand.copy(alpha = 0.5f),
                ),
            )
        }

        Surface(
            modifier = Modifier
                .height(40.dp)
                .padding(start = spacing.small),
            shape = RoundedCornerShape(50.dp),
            color = MaterialTheme.colorScheme.darkNavy,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onFilterClick,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FilterList,
                        contentDescription = stringResource(id = R.string.pantry_filters_toggle_cd),
                        tint = Color.White,
                    )
                }
                IconButton(
                    onClick = { isSearchExpanded = !isSearchExpanded },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = stringResource(id = R.string.pantry_search_placeholder),
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterPanel(
    isExpanded: Boolean,
    sortOption: PantrySortOption,
    sortDirection: SortDirection,
    pantryType: PantryTypeFilter,
    onSortOptionChange: (PantrySortOption) -> Unit,
    onSortDirectionChange: (SortDirection) -> Unit,
    onPantryTypeChange: (PantryTypeFilter) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.darkNavy,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.medium),
                verticalArrangement = Arrangement.spacedBy(spacing.medium),
            ) {
                FilterDropdown(
                    label = stringResource(id = R.string.pantry_filter_sort_label),
                    selectedValue = sortOption,
                    textColor = Color.White,
                    options = listOf(
                        PantrySortOption.RECENT to R.string.pantry_sort_recent,
                        PantrySortOption.NAME to R.string.pantry_sort_name,
                        PantrySortOption.ITEM_COUNT to R.string.pantry_sort_item_count,
                    ),
                    onValueChange = onSortOptionChange,
                )
                FilterDropdown(
                    label = stringResource(id = R.string.pantry_filter_direction_label),
                    selectedValue = sortDirection,
                    textColor = Color.White,
                    options = listOf(
                        SortDirection.ASCENDING to R.string.lists_direction_asc,
                        SortDirection.DESCENDING to R.string.lists_direction_desc,
                    ),
                    onValueChange = onSortDirectionChange,
                )
                FilterDropdown(
                    label = stringResource(id = R.string.pantry_filter_type_label),
                    selectedValue = pantryType,
                    textColor = Color.White,
                    options = listOf(
                        PantryTypeFilter.ALL to R.string.pantry_type_all,
                        PantryTypeFilter.SHARED to R.string.pantry_type_shared,
                        PantryTypeFilter.PERSONAL to R.string.pantry_type_personal,
                    ),
                    onValueChange = onPantryTypeChange,
                )
                TextButton(
                    onClick = onClearFilters,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = stringResource(id = R.string.pantry_clear_filters))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> FilterDropdown(
    label: String,
    selectedValue: T,
    options: List<Pair<T, Int>>,
    onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = options.firstOrNull { it.first == selectedValue }
        ?.let { stringResource(id = it.second) }.orEmpty()

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor.copy(alpha = 0.7f),
        )
        Spacer(modifier = Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedBorderColor = textColor,
                    unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                    focusedTrailingIconColor = textColor,
                    unfocusedTrailingIconColor = textColor,
                ),
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White),
            ) {
                options.forEach { (value, textRes) ->
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = textRes)) },
                        onClick = {
                            onValueChange(value)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PantryItemCard(
    item: PantryItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val expirationLabel = remember(item.expiresAt) {
        item.expiresAt?.let { expiration ->
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
            formatter.format(expiration.atZone(ZoneId.systemDefault()).toLocalDate())
        }
    }
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(text = item.name, style = MaterialTheme.typography.titleMedium)
                    val quantityLabel = if (item.unit.isNullOrBlank()) {
                        "${item.quantity}"
                    } else {
                        "${item.quantity} ${item.unit}"
                    }
                    Text(text = quantityLabel, style = MaterialTheme.typography.bodyMedium)
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(imageVector = Icons.Outlined.Edit, contentDescription = stringResource(id = R.string.pantry_edit_item))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Outlined.Delete, contentDescription = stringResource(id = R.string.pantry_delete_item))
                    }
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

@Composable
private fun PantryDialog(
    state: PantryDialogState,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDelete: (() -> Unit)?,
) {
    AlertDialog(
        onDismissRequest = { if (!state.isSubmitting) onDismiss() },
        properties = DialogProperties(),
        containerColor = Color.White,
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = !state.isSubmitting,
            ) {
                Text(text = stringResource(id = R.string.dialog_save))
            }
        },
        dismissButton = {
            TextButton(onClick = { if (!state.isSubmitting) onDismiss() }) {
                Text(text = stringResource(id = R.string.dialog_cancel))
            }
        },
        title = {
            Text(
                text = if (state.pantryId == null) stringResource(id = R.string.pantry_add_pantry) else stringResource(id = R.string.pantry_edit_pantry),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.small)) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    singleLine = true,
                    placeholder = { Text(text = stringResource(id = R.string.pantry_name_label)) },
                    enabled = !state.isSubmitting,
                    isError = state.errorMessageRes != null,
                    supportingText = state.errorMessageRes?.let { resId ->
                        { Text(text = stringResource(id = resId), color = MaterialTheme.colorScheme.error) }
                    },
                    shape = RoundedCornerShape(50.dp),
                )
                OutlinedTextField(
                    value = state.description,
                    onValueChange = onDescriptionChange,
                    placeholder = { Text(text = stringResource(id = R.string.pantry_description_label)) },
                    enabled = !state.isSubmitting,
                    shape = RoundedCornerShape(50.dp),
                )
                if (onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        enabled = !state.isSubmitting,
                    ) {
                        Text(text = stringResource(id = R.string.pantry_delete_pantry))
                    }
                }
            }
        },
    )
}

@Composable
private fun PantryItemDialog(
    state: PantryItemDialogState,
    onNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onExpirationChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!state.isSubmitting) onDismiss() },
        containerColor = Color.White,
        confirmButton = {
            Button(onClick = onSave, enabled = !state.isSubmitting) {
                Text(text = stringResource(id = R.string.dialog_save))
            }
        },
        dismissButton = {
            TextButton(onClick = { if (!state.isSubmitting) onDismiss() }) {
                Text(text = stringResource(id = R.string.dialog_cancel))
            }
        },
        title = {
            Text(
                text = if (state.isEditing) stringResource(id = R.string.pantry_edit_item) else stringResource(id = R.string.pantry_add_item),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.small)) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    enabled = !state.isEditing && !state.isSubmitting,
                    singleLine = true,
                    placeholder = { Text(text = stringResource(id = R.string.pantry_item_name_label)) },
                    isError = state.errorMessageRes != null,
                    supportingText = state.errorMessageRes?.let { resId ->
                        { Text(text = stringResource(id = resId), color = MaterialTheme.colorScheme.error) }
                    },
                )
                OutlinedTextField(
                    value = state.quantity,
                    onValueChange = onQuantityChange,
                    enabled = !state.isSubmitting,
                    singleLine = true,
                    placeholder = { Text(text = stringResource(id = R.string.pantry_item_quantity_label)) },
                )
                OutlinedTextField(
                    value = state.unit,
                    onValueChange = onUnitChange,
                    enabled = !state.isSubmitting,
                    singleLine = true,
                    placeholder = { Text(text = stringResource(id = R.string.pantry_item_unit_label)) },
                )
                OutlinedTextField(
                    value = state.expirationDate,
                    onValueChange = onExpirationChange,
                    enabled = !state.isSubmitting,
                    singleLine = true,
                    placeholder = { Text(text = stringResource(id = R.string.pantry_item_expiration_label)) },
                )
            }
        },
    )
}

@Composable
private fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LocalSpacing.current.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = message)
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.common_dismiss))
            }
        }
    }
}

@Composable
private fun EmptyPantryState(onCreatePantry: () -> Unit) {
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.medium, Alignment.Top),
    ) {
        Text(
            text = stringResource(id = R.string.pantry_empty_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(id = R.string.pantry_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onCreatePantry) {
            Text(text = stringResource(id = R.string.pantry_add_pantry))
        }
    }
}
