package com.comprartir.mobile.lists.presentation

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.designsystem.brand
import com.comprartir.mobile.core.designsystem.darkNavy
import com.comprartir.mobile.core.designsystem.searchFilterPill
import com.comprartir.mobile.core.designsystem.surfaceCard
import com.comprartir.mobile.core.designsystem.textMuted
import com.comprartir.mobile.core.navigation.AppDestination
import com.comprartir.mobile.core.navigation.NavigationIntent
import com.comprartir.mobile.feature.lists.ui.components.CreateListDialog
import com.comprartir.mobile.feature.lists.ui.components.EditListDialog
import com.comprartir.mobile.feature.lists.ui.components.DeleteListDialog
import com.comprartir.mobile.feature.lists.ui.components.CompleteListDialog
import com.comprartir.mobile.lists.data.ShoppingList
import com.comprartir.mobile.shared.components.AddFab
import com.comprartir.mobile.shared.components.EmptyStateMessage
import com.comprartir.mobile.feature.lists.model.ListTypeFilter
import com.comprartir.mobile.feature.lists.model.SortDirection
import com.comprartir.mobile.feature.lists.model.SortOption

@Composable
fun ListsRoute(
    onNavigate: (NavigationIntent) -> Unit,
    viewModel: ListsViewModel = hiltViewModel(),
    openCreateDialog: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    
    // Usar rememberSaveable para recordar si ya manejamos el openCreate
    // Esto evita que se reabra el diálogo al volver del detalle de una lista
    val hasHandledOpenCreate = androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    
    // Auto-open create dialog SOLO UNA VEZ si navigated con openCreate=true
    androidx.compose.runtime.LaunchedEffect(openCreateDialog) {
        if (openCreateDialog && !hasHandledOpenCreate.value && !state.createListState.isVisible) {
            android.util.Log.d("ListsRoute", "🔥 AUTO-OPENING create dialog from navigation parameter (FIRST TIME)")
            viewModel.showCreateDialog()
            hasHandledOpenCreate.value = true
        }
    }
    
    // Log state changes for debugging
    androidx.compose.runtime.LaunchedEffect(state.createListState.isVisible) {
        android.util.Log.d("ListsRoute", "createListState.isVisible changed to: ${state.createListState.isVisible}")
    }

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage ?: return@LaunchedEffect
        val args = message.messageArgs.toTypedArray()
        val text = context.getString(message.messageRes, *args)
        snackbarHostState.showSnackbar(text)
        viewModel.onSnackbarConsumed()
    }
    
    ListsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        contentPadding = contentPadding,
        onShowCreateDialog = viewModel::showCreateDialog,
        onDismissCreateDialog = viewModel::dismissCreateDialog,
        onCreateListNameChanged = viewModel::onCreateListNameChanged,
        onCreateListDescriptionChanged = viewModel::onCreateListDescriptionChanged,
        onCreateListRecurringChanged = viewModel::onCreateListRecurringChanged,
        onConfirmCreateList = viewModel::confirmCreateList,
        onShowEditDialog = viewModel::showEditDialog,
        onDismissEditDialog = viewModel::dismissEditDialog,
        onEditListNameChanged = viewModel::onEditListNameChanged,
        onEditListDescriptionChanged = viewModel::onEditListDescriptionChanged,
        onEditListRecurringChanged = viewModel::onEditListRecurringChanged,
        onConfirmEditList = viewModel::confirmEditList,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onToggleFilters = viewModel::toggleFilters,
        onSortOptionChange = viewModel::onSortOptionChange,
        onSortDirectionChange = viewModel::onSortDirectionChange,
        onListTypeChange = viewModel::onListTypeChange,
        onClearFilters = viewModel::clearFilters,
        onShowDeleteDialog = viewModel::showDeleteDialog,
        onDismissDeleteDialog = viewModel::dismissDeleteDialog,
        onConfirmDeleteList = viewModel::confirmDeleteList,
        onListSelected = { listId ->
            onNavigate(NavigationIntent(AppDestination.ListDetails, mapOf("listId" to listId)))
        },
        onShareList = { listId ->
            onNavigate(NavigationIntent(AppDestination.ShareList, mapOf("listId" to listId)))
        },
        onShowCompleteDialog = viewModel::showCompleteDialog,
        onDismissCompleteDialog = viewModel::dismissCompleteDialog,
        onCompletePantrySelected = viewModel::onCompletePantrySelected,
        onConfirmCompleteList = viewModel::confirmCompleteList,
        onRefresh = viewModel::refresh,
        onClearError = viewModel::clearError,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    state: ListsUiState,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues = PaddingValues(),
    onShowCreateDialog: () -> Unit,
    onDismissCreateDialog: () -> Unit,
    onCreateListNameChanged: (String) -> Unit,
    onCreateListDescriptionChanged: (String) -> Unit,
    onCreateListRecurringChanged: (Boolean) -> Unit,
    onConfirmCreateList: () -> Unit,
    onShowEditDialog: (ShoppingList) -> Unit,
    onDismissEditDialog: () -> Unit,
    onEditListNameChanged: (String) -> Unit,
    onEditListDescriptionChanged: (String) -> Unit,
    onEditListRecurringChanged: (Boolean) -> Unit,
    onConfirmEditList: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleFilters: () -> Unit,
    onSortOptionChange: (SortOption) -> Unit,
    onSortDirectionChange: (SortDirection) -> Unit,
    onListTypeChange: (ListTypeFilter) -> Unit,
    onClearFilters: () -> Unit,
    onShowDeleteDialog: (ShoppingList) -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDeleteList: () -> Unit,
    onListSelected: (String) -> Unit,
    onShareList: (String) -> Unit,
    onShowCompleteDialog: (ShoppingList) -> Unit,
    onDismissCompleteDialog: () -> Unit,
    onCompletePantrySelected: (String) -> Unit,
    onConfirmCompleteList: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    onClearError: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val canComplete = state.pantryOptions.isNotEmpty()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = spacing.large,
                end = spacing.large,
                top = spacing.medium,
                bottom = 80.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            // Recurring Lists Section
            if (state.showRecurringSection) {
                item {
                    RecurringListsSection(
                        lists = state.recurringLists,
                        onListSelected = onListSelected,
                        onCreateList = onShowCreateDialog,
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(spacing.medium))
                }
            }

            // Search and Filter Bar
            item {
                SearchAndFilterBar(
                    searchQuery = state.searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    onFilterClick = onToggleFilters,
                )
            }

            // Filter Panel
            item {
                FilterPanel(
                    isExpanded = state.isFiltersExpanded,
                    sortOption = state.sortOption,
                    sortDirection = state.sortDirection,
                    listType = state.listType,
                    onSortOptionChange = onSortOptionChange,
                    onSortDirectionChange = onSortDirectionChange,
                    onListTypeChange = onListTypeChange,
                    onClearFilters = onClearFilters,
                )
            }

            // Loading State
            if (state.isLoading && state.lists.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.large),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Error State
            if (state.errorMessage != null && state.lists.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.large),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(spacing.small)
                    ) {
                        Text(
                            text = state.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
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

            // Empty State
            if (!state.isLoading && state.errorMessage == null && state.lists.isEmpty()) {
                item {
                    EmptyStateMessage(
                        title = stringResource(id = R.string.lists_empty_title),
                        subtitle = stringResource(id = R.string.lists_empty_subtitle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = spacing.large),
                    )
                }
            }

            // Shopping Lists
            items(state.lists, key = { it.id }) { list ->
                ShoppingListCard(
                    list = list,
                    onListClick = { onListSelected(list.id) },
                    onEditClick = { onShowEditDialog(list) },
                    onDeleteClick = { onShowDeleteDialog(list) },
                    onShareClick = { onShareList(list.id) },
                    onCompleteClick = { onShowCompleteDialog(list) },
                    canComplete = canComplete,
                )
            }
        }

        AddFab(
            onClick = onShowCreateDialog,
            contentDescription = stringResource(id = R.string.lists_empty_action),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 4.dp,  // Minimal padding to extend beyond the cards column
                    bottom = spacing.xxl,
                )
                .size(64.dp),
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = spacing.large)
                .padding(bottom = spacing.large),
        )
    }

    CreateListDialog(
        state = state.createListState,
        onNameChange = { newName ->
            android.util.Log.d("ListsScreen", "CreateListDialog onNameChange: '$newName'")
            onCreateListNameChanged(newName)
        },
        onDescriptionChange = { newDesc ->
            android.util.Log.d("ListsScreen", "CreateListDialog onDescriptionChange: '$newDesc'")
            onCreateListDescriptionChanged(newDesc)
        },
        onRecurringChange = { recurring ->
            android.util.Log.d("ListsScreen", "CreateListDialog onRecurringChange: $recurring")
            onCreateListRecurringChanged(recurring)
        },
        onDismiss = {
            android.util.Log.d("ListsScreen", "CreateListDialog onDismiss called")
            onDismissCreateDialog()
        },
        onConfirm = {
            android.util.Log.d("ListsScreen", "CreateListDialog onConfirm called")
            onConfirmCreateList()
        },
    )

    EditListDialog(
        state = state.editListState,
        onNameChange = onEditListNameChanged,
        onDescriptionChange = onEditListDescriptionChanged,
        onRecurringChange = onEditListRecurringChanged,
        onDismiss = onDismissEditDialog,
        onConfirm = onConfirmEditList,
    )

    DeleteListDialog(
        state = state.deleteListState,
        onDismiss = onDismissDeleteDialog,
        onConfirm = onConfirmDeleteList,
    )

    CompleteListDialog(
        state = state.completeListState,
        onPantrySelected = onCompletePantrySelected,
        onDismiss = onDismissCompleteDialog,
        onConfirm = { onConfirmCompleteList(true) },
        onCompleteWithoutPantry = { onConfirmCompleteList(false) },
    )
}

@Composable
private fun RecurringListsSection(
    lists: List<ShoppingList>,
    onListSelected: (String) -> Unit,
    onCreateList: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        Text(
            text = stringResource(id = R.string.recurring_lists_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(id = R.string.recurring_lists_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.textMuted,
        )
        
        Spacer(modifier = Modifier.height(spacing.small))
        
        if (lists.isEmpty()) {
            FilledTonalButton(
                onClick = onCreateList,
                modifier = Modifier.align(Alignment.Start),
            ) {
                Text(text = stringResource(id = R.string.recurring_lists_action_create))
            }
            Text(
                text = stringResource(id = R.string.recurring_lists_empty),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            val fadeColor = MaterialTheme.colorScheme.background
            val fadeWidth = 48.dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                ) {
                    items(lists, key = { it.id }) { list ->
                        RecurringListCard(
                            list = list,
                            onOpenList = { onListSelected(list.id) },
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .width(fadeWidth)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(fadeColor, Color.Transparent),
                            ),
                        ),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(fadeWidth)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, fadeColor),
                            ),
                        ),
                )
            }
        }
    }
}

@Composable
private fun RecurringListCard(
    list: ShoppingList,
    onOpenList: () -> Unit,
) {
    val completedItems = list.items.count { it.isAcquired }
    val totalItems = list.items.size
    
    Card(
        modifier = Modifier
            .width(260.dp)
            .height(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.brand.copy(alpha = 0.2f)),
        onClick = onOpenList,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Header with name and counter
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = list.name.ifBlank { stringResource(id = R.string.lists_default_title) },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.TopStart),
                )
                // Item counter pill
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd),
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.darkNavy,
                ) {
                    Text(
                        text = "$completedItems/$totalItems",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
            
            // Footer with status
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomStart,
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = when {
                        totalItems == 0 -> MaterialTheme.colorScheme.surfaceVariant
                        completedItems == totalItems -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    },
                ) {
                    Text(
                        text = when {
                            totalItems == 0 -> stringResource(id = R.string.list_status_empty)
                            completedItems == totalItems -> stringResource(id = R.string.list_status_complete)
                            else -> stringResource(id = R.string.list_status_pending)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            totalItems == 0 -> MaterialTheme.colorScheme.onSurfaceVariant
                            completedItems == totalItems -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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
                text = stringResource(id = R.string.all_lists_title),
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
                placeholder = { Text(stringResource(id = R.string.lists_search_placeholder)) },
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
                        contentDescription = stringResource(id = R.string.lists_filters_toggle_cd),
                        tint = Color.White,
                    )
                }
                IconButton(
                    onClick = { isSearchExpanded = !isSearchExpanded },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = stringResource(id = R.string.lists_search_placeholder),
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
    sortOption: SortOption,
    sortDirection: SortDirection,
    listType: ListTypeFilter,
    onSortOptionChange: (SortOption) -> Unit,
    onSortDirectionChange: (SortDirection) -> Unit,
    onListTypeChange: (ListTypeFilter) -> Unit,
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
                    label = stringResource(id = R.string.lists_filter_sort_label),
                    selectedValue = sortOption,
                    textColor = Color.White,
                    options = listOf(
                        SortOption.RECENT to R.string.lists_sort_recent,
                        SortOption.NAME to R.string.lists_sort_name,
                        SortOption.PROGRESS to R.string.lists_sort_progress,
                    ),
                    onValueChange = onSortOptionChange,
                )
                FilterDropdown(
                    label = stringResource(id = R.string.lists_filter_direction_label),
                    selectedValue = sortDirection,
                    textColor = Color.White,
                    options = listOf(
                        SortDirection.ASCENDING to R.string.lists_direction_asc,
                        SortDirection.DESCENDING to R.string.lists_direction_desc,
                    ),
                    onValueChange = onSortDirectionChange,
                )
                FilterDropdown(
                    label = stringResource(id = R.string.lists_filter_type_label),
                    selectedValue = listType,
                    textColor = Color.White,
                    options = listOf(
                        ListTypeFilter.ALL to R.string.lists_type_all,
                        ListTypeFilter.SHARED to R.string.lists_type_shared,
                        ListTypeFilter.PERSONAL to R.string.lists_type_personal,
                    ),
                    onValueChange = onListTypeChange,
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
                    Text(text = stringResource(id = R.string.lists_clear_filters))
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
    val selectedText = options.firstOrNull { it.first == selectedValue }?.let { stringResource(id = it.second) } ?: ""

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
private fun ShoppingListCard(
    list: ShoppingList,
    onListClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit,
    onCompleteClick: () -> Unit,
    canComplete: Boolean,
) {
    val spacing = LocalSpacing.current
    var showMenu by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    
    val completedItems = list.items.count { it.isAcquired }
    val totalItems = list.items.size
    
    Card(
        onClick = onListClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.brand.copy(alpha = 0.2f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Header: Name and counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = list.name.ifBlank { stringResource(id = R.string.lists_default_title) },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Item counter pill
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.darkNavy,
                    ) {
                        Text(
                            text = "$completedItems/$totalItems",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                    
                    // Three dots menu
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(id = R.string.lists_menu),
                                tint = MaterialTheme.colorScheme.darkNavy,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(Color.White),
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.lists_edit)) },
                                onClick = {
                                    showMenu = false
                                    onEditClick()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.lists_share)) },
                                onClick = {
                                    showMenu = false
                                    onShareClick()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Share, contentDescription = null)
                                }
                            )
                            if (canComplete && list.items.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = R.string.lists_mark_complete)) },
                                    onClick = {
                                        showMenu = false
                                        onCompleteClick()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.lists_delete)) },
                                onClick = {
                                    showMenu = false
                                    onDeleteClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.error,
                                )
                            )
                        }
                    }
                }
            }
            
            // Description - centered vertically between header and footer
            Text(
                text = list.description?.takeIf { it.isNotBlank() } 
                    ?: stringResource(id = R.string.lists_no_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.textMuted,
                modifier = Modifier.padding(vertical = 12.dp),
            )
            
            // Footer with status
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomStart,
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = when {
                        totalItems == 0 -> MaterialTheme.colorScheme.surfaceVariant
                        completedItems == totalItems -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    },
                ) {
                    Text(
                        text = when {
                            totalItems == 0 -> stringResource(id = R.string.list_status_empty)
                            completedItems == totalItems -> stringResource(id = R.string.list_status_complete)
                            else -> stringResource(id = R.string.list_status_pending)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            totalItems == 0 -> MaterialTheme.colorScheme.onSurfaceVariant
                            completedItems == totalItems -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}
