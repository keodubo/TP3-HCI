package com.comprartir.mobile.feature.listdetail.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.ComprartirPillShape
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.designsystem.borderDefault
import com.comprartir.mobile.core.designsystem.brand
import com.comprartir.mobile.core.designsystem.brandTint
import com.comprartir.mobile.core.designsystem.surfaceCard
import com.comprartir.mobile.core.designsystem.textMuted
import com.comprartir.mobile.core.designsystem.theme.ColorTokens
import com.comprartir.mobile.feature.listdetail.model.AddProductUiState
import com.comprartir.mobile.feature.listdetail.model.CategorySelectionTarget
import com.comprartir.mobile.feature.listdetail.model.CategoryUi
import com.comprartir.mobile.feature.listdetail.model.CreateCategoryDialogState
import com.comprartir.mobile.feature.listdetail.model.EditProductDialogState
import com.comprartir.mobile.feature.listdetail.model.ListDetailEvent
import com.comprartir.mobile.feature.listdetail.model.ListDetailUiState
import com.comprartir.mobile.feature.listdetail.model.ListItemUi
import com.comprartir.mobile.core.ui.rememberIsLandscape
import androidx.compose.animation.core.animateFloatAsState

@Composable
fun ListDetailScreen(
    state: ListDetailUiState,
    onEvent: (ListDetailEvent) -> Unit,
    onBack: () -> Unit,
    onOpenShareManagement: (String, String) -> Unit,
    snackbarHostState: SnackbarHostState,
    isTabletLayout: Boolean,
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val layoutDirection = LocalLayoutDirection.current
    val clipboardManager = LocalClipboardManager.current
    val isLandscape = rememberIsLandscape()
    val isTablet = isTabletLayout

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ColorTokens.NeutralSurface,
        topBar = {
            val title = state.name.ifBlank { stringResource(id = R.string.lists_default_title) }
            android.util.Log.d("ListDetailScreen", "TopBar title: $title, state.name: ${state.name}")
            ListDetailTopBar(
                title = title,
                onBack = onBack,
                onEdit = { onEvent(ListDetailEvent.ShowEditDialog) },
                onDelete = { onEvent(ListDetailEvent.ShowDeleteDialog) },
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            )
        },
    ) { innerPadding ->
        val combinedPadding = PaddingValues(
            start = innerPadding.calculateStartPadding(layoutDirection) +
                contentPadding.calculateStartPadding(layoutDirection),
            end = innerPadding.calculateEndPadding(layoutDirection) +
                contentPadding.calculateEndPadding(layoutDirection),
            top = innerPadding.calculateTopPadding() + spacing.extraLarge,
            bottom = innerPadding.calculateBottomPadding() +
                contentPadding.calculateBottomPadding() + spacing.large,
        )
        val useExpandedLayout = isTablet || isLandscape
        val onCopyShareLink = {
            if (state.shareState.link.isNotBlank()) {
                clipboardManager.setText(AnnotatedString(state.shareState.link))
                onEvent(ListDetailEvent.LinkCopied)
            }
        }
        val onManageShare = {
            if (state.listId.isNotBlank()) {
                onOpenShareManagement(state.listId, state.name)
            }
        }
        if (useExpandedLayout) {
            val sidebarWeight = if (isTablet) 0.3f else 0.4f
            val itemsWeight = 1f - sidebarWeight
            val horizontalPadding = if (isTablet) spacing.xl else spacing.large
            val panelSpacing = if (isTablet) spacing.xl else spacing.large
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(combinedPadding)
                    .padding(horizontal = horizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(panelSpacing),
                verticalAlignment = Alignment.Top,
            ) {
                ListDetailSidebar(
                    state = state,
                    onEvent = onEvent,
                    onCopyShareLink = onCopyShareLink,
                    onManageShare = onManageShare,
                    modifier = Modifier
                        .weight(sidebarWeight)
                        .fillMaxHeight(),
                )
                ListDetailItemsPanel(
                    items = state.visibleItems,
                    categories = state.categories,
                    onToggle = { id, completed -> onEvent(ListDetailEvent.ToggleItem(id, completed)) },
                    onDelete = { id -> onEvent(ListDetailEvent.DeleteItem(id)) },
                    onEdit = { id -> onEvent(ListDetailEvent.ShowEditProductDialog(id)) },
                    itemColumns = if (isTablet) 2 else 1,
                    modifier = Modifier
                        .weight(itemsWeight)
                        .fillMaxHeight(),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = combinedPadding,
                verticalArrangement = Arrangement.spacedBy(spacing.large),
            ) {
                item {
                    ListDetailMainCard(
                        state = state,
                        onEvent = onEvent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.large),
                    )
                }
                item {
                    AddProductPanel(
                        state = state.addProductState,
                        categories = state.categories,
                        onNameChange = { onEvent(ListDetailEvent.AddProductNameChanged(it)) },
                        onQuantityChange = { onEvent(ListDetailEvent.AddProductQuantityChanged(it)) },
                        onUnitChange = { onEvent(ListDetailEvent.AddProductUnitChanged(it)) },
                        onCategorySelected = { onEvent(ListDetailEvent.AddProductCategoryChanged(it)) },
                        onCreateCategory = { onEvent(ListDetailEvent.ShowCreateCategoryDialog(CategorySelectionTarget.AddProduct)) },
                        onSubmit = { onEvent(ListDetailEvent.SubmitNewProduct) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.large),
                    )
                }
                item {
                    SharePanel(
                        email = state.shareState.email,
                        link = state.shareState.link,
                        isInviting = state.shareState.isInviting,
                        onEmailChange = { onEvent(ListDetailEvent.ShareEmailChanged(it)) },
                        onInvite = { onEvent(ListDetailEvent.SubmitShareInvite) },
                        onCopyLink = onCopyShareLink,
                        onManage = onManageShare,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.large),
                    )
                }
            }
        }
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(combinedPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        if (state.editListState.isVisible) {
            com.comprartir.mobile.feature.lists.ui.components.EditListDialog(
                state = com.comprartir.mobile.lists.presentation.EditListUiState(
                    isVisible = state.editListState.isVisible,
                    listId = state.listId,
                    name = state.editListState.name,
                    description = state.editListState.description,
                    isSubmitting = state.editListState.isSubmitting,
                    errorMessageRes = state.editListState.errorMessageRes,
                ),
                onDismiss = { onEvent(ListDetailEvent.DismissEditDialog) },
                onNameChange = { onEvent(ListDetailEvent.EditListNameChanged(it)) },
                onDescriptionChange = { onEvent(ListDetailEvent.EditListDescriptionChanged(it)) },
                onRecurringChange = { },
                onConfirm = { onEvent(ListDetailEvent.ConfirmEditList) },
            )
        }

        if (state.deleteListState.isVisible) {
            com.comprartir.mobile.feature.lists.ui.components.DeleteListDialog(
                state = com.comprartir.mobile.lists.presentation.DeleteListUiState(
                    isVisible = state.deleteListState.isVisible,
                    listId = state.listId,
                    listName = state.name,
                    isDeleting = state.deleteListState.isDeleting,
                ),
                onDismiss = { onEvent(ListDetailEvent.DismissDeleteDialog) },
                onConfirm = { onEvent(ListDetailEvent.ConfirmDeleteList) },
            )
        }
        if (state.editProductState.isVisible) {
            EditProductDialog(
                state = state.editProductState,
                categories = state.categories,
                onNameChange = { onEvent(ListDetailEvent.EditProductNameChanged(it)) },
                onQuantityChange = { onEvent(ListDetailEvent.EditProductQuantityChanged(it)) },
                onUnitChange = { onEvent(ListDetailEvent.EditProductUnitChanged(it)) },
                onCategorySelected = { onEvent(ListDetailEvent.EditProductCategoryChanged(it)) },
                onCreateCategory = { onEvent(ListDetailEvent.ShowCreateCategoryDialog(CategorySelectionTarget.EditProduct)) },
                onDismiss = { onEvent(ListDetailEvent.DismissEditProductDialog) },
                onConfirm = { onEvent(ListDetailEvent.ConfirmEditProduct) },
            )
        }
        if (state.createCategoryState.isVisible) {
            CreateCategoryDialog(
                state = state.createCategoryState,
                onNameChange = { onEvent(ListDetailEvent.CreateCategoryNameChanged(it)) },
                onDismiss = { onEvent(ListDetailEvent.DismissCreateCategoryDialog) },
                onConfirm = { onEvent(ListDetailEvent.ConfirmCreateCategory) },
            )
        }
    }
}

@Composable
private fun ListDetailTopBar(
    title: String,
    onBack: () -> Unit,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    val spacing = LocalSpacing.current
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.large, vertical = spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = stringResource(id = R.string.cd_back))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(id = R.string.lists_edit),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(id = R.string.lists_delete),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
    Divider(
        color = MaterialTheme.colorScheme.borderDefault,
        thickness = 1.dp,
    )
}

@Composable
private fun ListDetailMainCard(
    state: ListDetailUiState,
    onEvent: (ListDetailEvent) -> Unit,
    modifier: Modifier = Modifier,
    showItemsList: Boolean = true,
) {
    val spacing = LocalSpacing.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceCard),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            val title = state.name.ifBlank { stringResource(id = R.string.lists_default_title) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row {
                    IconButton(onClick = { onEvent(com.comprartir.mobile.feature.listdetail.model.ListDetailEvent.ShowEditDialog) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.lists_edit)
                        )
                    }
                    IconButton(onClick = { onEvent(com.comprartir.mobile.feature.listdetail.model.ListDetailEvent.ShowDeleteDialog) }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.lists_delete),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            state.subtitle.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.textMuted,
                )
            }
            ListDetailProgress(progress = state.progressFraction, completed = state.completedItems, total = state.totalItems)
            ListDetailFilterCard(
                isExpanded = state.filtersExpanded,
                hideCompleted = state.hideCompleted,
                categories = state.categories,
                selectedCategoryId = state.selectedCategoryFilterId,
                onToggleFilters = { onEvent(ListDetailEvent.ToggleFilters) },
                onToggleHideCompleted = { onEvent(ListDetailEvent.ToggleHideCompleted) },
                onCategorySelected = { onEvent(ListDetailEvent.FilterCategoryChanged(it)) },
            )
            state.errorMessageRes?.let { res ->
                Text(
                    text = stringResource(id = res),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            if (showItemsList) {
                ListDetailItemsList(
                    items = state.visibleItems,
                    categories = state.categories,
                    onToggle = { id, completed -> onEvent(ListDetailEvent.ToggleItem(id, completed)) },
                    onDelete = { id -> onEvent(ListDetailEvent.DeleteItem(id)) },
                    onEdit = { id -> onEvent(ListDetailEvent.ShowEditProductDialog(id)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                )
            }
        }
    }
}

@Composable
private fun ListDetailSidebar(
    state: ListDetailUiState,
    onEvent: (ListDetailEvent) -> Unit,
    onCopyShareLink: () -> Unit,
    onManageShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier.verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(spacing.large),
    ) {
        ListDetailMainCard(
            state = state,
            onEvent = onEvent,
            modifier = Modifier.fillMaxWidth(),
            showItemsList = false,
        )
        AddProductPanel(
            state = state.addProductState,
            categories = state.categories,
            onNameChange = { onEvent(ListDetailEvent.AddProductNameChanged(it)) },
            onQuantityChange = { onEvent(ListDetailEvent.AddProductQuantityChanged(it)) },
            onUnitChange = { onEvent(ListDetailEvent.AddProductUnitChanged(it)) },
            onCategorySelected = { onEvent(ListDetailEvent.AddProductCategoryChanged(it)) },
            onCreateCategory = { onEvent(ListDetailEvent.ShowCreateCategoryDialog(CategorySelectionTarget.AddProduct)) },
            onSubmit = { onEvent(ListDetailEvent.SubmitNewProduct) },
            modifier = Modifier.fillMaxWidth(),
        )
        SharePanel(
            email = state.shareState.email,
            link = state.shareState.link,
            isInviting = state.shareState.isInviting,
            onEmailChange = { onEvent(ListDetailEvent.ShareEmailChanged(it)) },
            onInvite = { onEvent(ListDetailEvent.SubmitShareInvite) },
            onCopyLink = onCopyShareLink,
            onManage = onManageShare,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ListDetailItemsPanel(
    items: List<ListItemUi>,
    categories: List<CategoryUi>,
    onToggle: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
    onEdit: (String) -> Unit,
    itemColumns: Int,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceCard),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing.large),
            verticalArrangement = Arrangement.spacedBy(if (itemColumns > 1) spacing.large else spacing.medium),
        ) {
            Text(
                text = stringResource(id = R.string.list_detail_screen_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            ListDetailItemsList(
                items = items,
                categories = categories,
                onToggle = onToggle,
                onDelete = onDelete,
                onEdit = onEdit,
                columns = itemColumns,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun ListDetailProgress(
    progress: Float,
    completed: Int,
    total: Int,
) {
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "list-progress")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(ComprartirPillShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.brandTint,
        )
        Text(
            text = stringResource(id = R.string.lists_progress_label, completed, total),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.textMuted,
        )
    }
}

@Composable
private fun ListDetailFilterCard(
    isExpanded: Boolean,
    hideCompleted: Boolean,
    categories: List<CategoryUi>,
    selectedCategoryId: String?,
    onToggleFilters: () -> Unit,
    onToggleHideCompleted: () -> Unit,
    onCategorySelected: (String?) -> Unit,
) {
    val spacing = LocalSpacing.current
    Card(
        colors = CardDefaults.cardColors(containerColor = ColorTokens.PurpleDeep),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(id = R.string.list_detail_filters_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
                TextButton(onClick = onToggleFilters) {
                    Text(
                        text = if (isExpanded) stringResource(id = R.string.lists_filters_hide) else stringResource(id = R.string.lists_filters_show),
                        color = Color.White,
                    )
                }
            }
            AnimatedVisibility(visible = isExpanded, enter = fadeIn(), exit = fadeOut()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.medium),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.small),
                        ) {
                            Checkbox(
                                checked = hideCompleted,
                                onCheckedChange = { onToggleHideCompleted() },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = Color.White,
                                ),
                            )
                            Text(
                                text = stringResource(id = R.string.list_detail_hide_completed),
                                color = Color.White,
                            )
                        }
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = null,
                            tint = Color.White,
                        )
                    }
                    CategoryDropdownField(
                        label = stringResource(id = R.string.list_detail_filter_category_label),
                        categories = listOf(CategoryUi(id = null, nameRes = R.string.list_detail_filter_category_all)) + categories.filter { it.id != null },
                        selectedCategoryId = selectedCategoryId,
                        onCategorySelected = onCategorySelected,
                        containerColor = ColorTokens.PurpleDeep,
                        textColor = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun ListDetailItemsList(
    items: List<ListItemUi>,
    categories: List<CategoryUi>,
    onToggle: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
    onEdit: (String) -> Unit,
    columns: Int = 1,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    if (items.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(id = R.string.list_detail_empty_items),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.textMuted,
            )
        }
        return
    }
    if (columns > 1) {
        val gridSpacing = spacing.medium
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(gridSpacing),
            horizontalArrangement = Arrangement.spacedBy(gridSpacing),
        ) {
            items(items, key = { it.id }) { item ->
                val categoryLabel = categoryLabelFor(item.categoryId, categories)
                ListItemRow(
                    item = item,
                    categoryLabel = categoryLabel,
                    onToggle = { checked -> onToggle(item.id, checked) },
                    onEdit = { onEdit(item.id) },
                    onDelete = { onDelete(item.id) },
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            items(items, key = { it.id }) { item ->
                val categoryLabel = categoryLabelFor(item.categoryId, categories)
                ListItemRow(
                    item = item,
                    categoryLabel = categoryLabel,
                    onToggle = { checked -> onToggle(item.id, checked) },
                    onEdit = { onEdit(item.id) },
                    onDelete = { onDelete(item.id) },
                )
            }
        }
    }
}

@Composable
private fun categoryLabelFor(categoryId: String?, categories: List<CategoryUi>): String? {
    if (categoryId == null) {
        return stringResource(id = R.string.list_detail_category_none)
    }
    val category = categories.firstOrNull { it.id == categoryId } ?: return null
    return categoryDisplayName(category)
}

@Composable
private fun ListItemRow(
    item: ListItemUi,
    categoryLabel: String?,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Surface(
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.borderDefault),
        color = MaterialTheme.colorScheme.surfaceCard,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = onToggle,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                ),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (item.isCompleted) MaterialTheme.colorScheme.textMuted else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.quantityLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.textMuted,
                )
                categoryLabel?.let { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                item.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.textMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Outlined.Edit, contentDescription = stringResource(id = R.string.list_detail_edit_item))
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = stringResource(id = R.string.list_detail_delete_item))
                }
            }
        }
    }
}

@Composable
private fun AddProductPanel(
    state: AddProductUiState,
    categories: List<CategoryUi>,
    onNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onCreateCategory: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceCard),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            Text(
                text = stringResource(id = R.string.list_detail_add_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                shape = ComprartirPillShape,
                singleLine = true,
                placeholder = { Text(text = stringResource(id = R.string.list_detail_add_name)) },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = state.quantity,
                    onValueChange = onQuantityChange,
                    shape = ComprartirPillShape,
                    singleLine = true,
                    placeholder = { Text(text = stringResource(id = R.string.list_detail_add_quantity)) },
                )
                UnitDropdownField(
                    modifier = Modifier.weight(1f),
                    selectedUnit = state.unit,
                    onUnitSelected = onUnitChange,
                )
            }
            CategoryDropdownField(
                label = stringResource(id = R.string.list_detail_category_label),
                categories = categories,
                selectedCategoryId = state.categoryId,
                onCategorySelected = onCategorySelected,
                showCreateOption = true,
                onCreateCategory = onCreateCategory,
            )
            state.errorMessageRes?.let { res ->
                Text(
                    text = stringResource(id = res),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Button(
                onClick = onSubmit,
                enabled = state.canSubmit,
                shape = ComprartirPillShape,
            ) {
                Text(text = stringResource(id = R.string.list_detail_add_button))
            }
        }
    }
}

@Composable
private fun EditProductDialog(
    state: EditProductDialogState,
    categories: List<CategoryUi>,
    onNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onCreateCategory: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text(text = stringResource(id = R.string.list_detail_edit_product_title)) },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = state.canSubmit) {
                Text(text = stringResource(id = R.string.list_detail_edit_product_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.list_detail_edit_product_cancel))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.medium)) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(text = stringResource(id = R.string.list_detail_add_name)) },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.small),
                ) {
                    OutlinedTextField(
                        value = state.quantity,
                        onValueChange = onQuantityChange,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text(text = stringResource(id = R.string.list_detail_add_quantity)) },
                    )
                    UnitDropdownField(
                        modifier = Modifier.weight(1f),
                        selectedUnit = state.unit,
                        onUnitSelected = onUnitChange,
                    )
                }
                CategoryDropdownField(
                    label = stringResource(id = R.string.list_detail_category_label),
                    categories = categories,
                    selectedCategoryId = state.categoryId,
                    onCategorySelected = onCategorySelected,
                    showCreateOption = true,
                    onCreateCategory = onCreateCategory,
                )
                state.errorMessageRes?.let { res ->
                    Text(
                        text = stringResource(id = res),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
    )
}

@Composable
private fun CreateCategoryDialog(
    state: CreateCategoryDialogState,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text(text = stringResource(id = R.string.list_detail_category_dialog_title)) },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = state.canSubmit) {
                Text(text = stringResource(id = R.string.list_detail_edit_product_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.list_detail_edit_product_cancel))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.medium)) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(text = stringResource(id = R.string.list_detail_category_label)) },
                )
                state.errorMessageRes?.let { res ->
                    Text(
                        text = stringResource(id = res),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdownField(
    label: String,
    categories: List<CategoryUi>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    showCreateOption: Boolean = false,
    onCreateCategory: (() -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = categories.firstOrNull { it.id == selectedCategoryId }
    val defaultLabel = categories.firstOrNull { it.id == null }?.let { categoryDisplayName(it) }
        ?: label
    val displayLabel = selected?.let { categoryDisplayName(it) } ?: defaultLabel

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = displayLabel,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            placeholder = { Text(text = label, color = textColor) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                focusedLabelColor = textColor,
                unfocusedLabelColor = textColor.copy(alpha = 0.8f),
                focusedBorderColor = textColor,
                unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                cursorColor = textColor,
                containerColor = containerColor,
            ),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White),
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(text = categoryDisplayName(category), color = textColor) },
                    onClick = {
                        expanded = false
                        onCategorySelected(category.id)
                    },
                )
            }
            if (showCreateOption && onCreateCategory != null) {
                Divider()
                DropdownMenuItem(
                    text = { Text(text = stringResource(id = R.string.list_detail_category_create_new), color = textColor) },
                    onClick = {
                        expanded = false
                        onCreateCategory()
                    },
                )
            }
        }
    }
}

@Composable
private fun categoryDisplayName(category: CategoryUi): String =
    category.name ?: category.nameRes?.let { stringResource(id = it) } ?: ""

@Composable
private fun UnitDropdownField(
    selectedUnit: String,
    onUnitSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val units = com.comprartir.mobile.core.model.Unit.entries
    val selected = com.comprartir.mobile.core.model.Unit.fromValue(selectedUnit) ?: com.comprartir.mobile.core.model.Unit.getDefault()
    val displayLabel = stringResource(id = selected.labelRes)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = displayLabel,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            placeholder = { Text(text = stringResource(id = R.string.list_detail_add_unit)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = ComprartirPillShape,
            singleLine = true,
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White),
        ) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(text = stringResource(id = unit.labelRes)) },
                    onClick = {
                        expanded = false
                        onUnitSelected(unit.value)
                    },
                )
            }
        }
    }
}

@Composable
private fun SharePanel(
    email: String,
    link: String,
    isInviting: Boolean,
    onEmailChange: (String) -> Unit,
    onInvite: () -> Unit,
    onCopyLink: () -> Unit,
    onManage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceCard),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            Text(
                text = stringResource(id = R.string.list_detail_share_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                shape = ComprartirPillShape,
                singleLine = true,
                placeholder = { Text(text = stringResource(id = R.string.list_detail_share_email)) },
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onInvite,
                    enabled = !isInviting,
                    shape = ComprartirPillShape,
                ) {
                    if (isInviting) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = spacing.xs),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                    Text(text = stringResource(id = R.string.list_detail_share_invite))
                }
                TextButton(onClick = onManage) {
                    Text(text = stringResource(id = R.string.list_detail_share_manage))
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(spacing.tiny)) {
                Text(
                    text = stringResource(id = R.string.list_detail_share_link_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = link,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.textMuted,
                )
            }
            Button(
                onClick = onCopyLink,
                shape = ComprartirPillShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Text(text = stringResource(id = R.string.list_detail_share_copy))
            }
        }
    }
}
