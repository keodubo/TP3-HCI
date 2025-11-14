package com.comprartir.mobile.feature.listdetail.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.AnnotatedString
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
import com.comprartir.mobile.feature.listdetail.model.ListDetailEvent
import com.comprartir.mobile.feature.listdetail.model.ListDetailUiState
import com.comprartir.mobile.feature.listdetail.model.ListItemUi
import androidx.compose.animation.core.animateFloatAsState

@Composable
fun ListDetailScreen(
    state: ListDetailUiState,
    onEvent: (ListDetailEvent) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    isTabletLayout: Boolean,
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val layoutDirection = LocalLayoutDirection.current
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ColorTokens.NeutralSurface,
        topBar = {
            ListDetailTopBar(
                title = state.title.ifBlank { stringResource(id = R.string.lists_default_title) },
                onBack = onBack,
                onEdit = { onEvent(ListDetailEvent.ShowEditDialog) },
                onDelete = { onEvent(ListDetailEvent.ShowDeleteDialog) },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        val combinedPadding = PaddingValues(
            start = innerPadding.calculateStartPadding(layoutDirection) +
                contentPadding.calculateStartPadding(layoutDirection),
            end = innerPadding.calculateEndPadding(layoutDirection) +
                contentPadding.calculateEndPadding(layoutDirection),
            top = innerPadding.calculateTopPadding() + spacing.large,
            bottom = innerPadding.calculateBottomPadding() +
                contentPadding.calculateBottomPadding() + spacing.large,
        )
        if (isTabletLayout) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = combinedPadding,
                verticalArrangement = Arrangement.Top,
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 0.dp)
                            .padding(horizontal = spacing.large),
                        horizontalArrangement = Arrangement.spacedBy(spacing.large),
                        verticalAlignment = Alignment.Top,
                    ) {
                        ListDetailMainCard(
                            state = state,
                            onEvent = onEvent,
                            modifier = Modifier
                                .weight(1.5f)
                                .fillMaxHeight(),
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(spacing.large),
                        ) {
                            AddProductPanel(
                                state = state.addProductState,
                                onNameChange = { onEvent(ListDetailEvent.AddProductNameChanged(it)) },
                                onQuantityChange = { onEvent(ListDetailEvent.AddProductQuantityChanged(it)) },
                                onUnitChange = { onEvent(ListDetailEvent.AddProductUnitChanged(it)) },
                                onSubmit = { onEvent(ListDetailEvent.SubmitNewProduct) },
                            )
                            SharePanel(
                                email = state.shareState.email,
                                link = state.shareState.link,
                                onEmailChange = { onEvent(ListDetailEvent.ShareEmailChanged(it)) },
                                onCopyLink = {
                                    if (state.shareState.link.isNotBlank()) {
                                        clipboardManager.setText(AnnotatedString(state.shareState.link))
                                        onEvent(ListDetailEvent.LinkCopied)
                                    }
                                },
                            )
                        }
                    }
                }
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
                        onNameChange = { onEvent(ListDetailEvent.AddProductNameChanged(it)) },
                        onQuantityChange = { onEvent(ListDetailEvent.AddProductQuantityChanged(it)) },
                        onUnitChange = { onEvent(ListDetailEvent.AddProductUnitChanged(it)) },
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
                        onEmailChange = { onEvent(ListDetailEvent.ShareEmailChanged(it)) },
                        onCopyLink = {
                            if (state.shareState.link.isNotBlank()) {
                                clipboardManager.setText(AnnotatedString(state.shareState.link))
                                onEvent(ListDetailEvent.LinkCopied)
                            }
                        },
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
                    listName = state.title,
                    isDeleting = state.deleteListState.isDeleting,
                ),
                onDismiss = { onEvent(ListDetailEvent.DismissDeleteDialog) },
                onConfirm = { onEvent(ListDetailEvent.ConfirmDeleteList) },
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
            val title = state.title.ifBlank { stringResource(id = R.string.lists_default_title) }
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
                onToggleFilters = { onEvent(ListDetailEvent.ToggleFilters) },
                onToggleHideCompleted = { onEvent(ListDetailEvent.ToggleHideCompleted) },
            )
            state.errorMessageRes?.let { res ->
                Text(
                    text = stringResource(id = res),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            ListDetailItemsList(
                items = state.visibleItems,
                onToggle = { id, completed -> onEvent(ListDetailEvent.ToggleItem(id, completed)) },
                onDelete = { id -> onEvent(ListDetailEvent.DeleteItem(id)) },
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
    onToggleFilters: () -> Unit,
    onToggleHideCompleted: () -> Unit,
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
                    text = stringResource(id = R.string.list_detail_filters_title),
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
            }
        }
    }
}

@Composable
private fun ListDetailItemsList(
    items: List<ListItemUi>,
    onToggle: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
) {
    val spacing = LocalSpacing.current
    if (items.isEmpty()) {
        Box(
            modifier = Modifier
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
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 420.dp),
        verticalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        items(items, key = { it.id }) { item ->
            ListItemRow(
                item = item,
                onToggle = { checked -> onToggle(item.id, checked) },
                onDelete = { onDelete(item.id) },
            )
        }
    }
}

@Composable
private fun ListItemRow(
    item: ListItemUi,
    onToggle: (Boolean) -> Unit,
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
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Outlined.Delete, contentDescription = stringResource(id = R.string.list_detail_delete_item))
            }
        }
    }
}

@Composable
private fun AddProductPanel(
    state: AddProductUiState,
    onNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
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
                label = { Text(text = stringResource(id = R.string.list_detail_add_name)) },
                shape = ComprartirPillShape,
                singleLine = true,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = state.quantity,
                    onValueChange = onQuantityChange,
                    label = { Text(text = stringResource(id = R.string.list_detail_add_quantity)) },
                    shape = ComprartirPillShape,
                    singleLine = true,
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = state.unit,
                    onValueChange = onUnitChange,
                    label = { Text(text = stringResource(id = R.string.list_detail_add_unit)) },
                    shape = ComprartirPillShape,
                    singleLine = true,
                )
            }
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
private fun SharePanel(
    email: String,
    link: String,
    onEmailChange: (String) -> Unit,
    onCopyLink: () -> Unit,
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
                label = { Text(text = stringResource(id = R.string.list_detail_share_email)) },
                shape = ComprartirPillShape,
                singleLine = true,
            )
            Text(
                text = link,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.textMuted,
            )
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
