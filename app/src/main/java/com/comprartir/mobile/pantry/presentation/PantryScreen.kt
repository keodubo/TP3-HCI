package com.comprartir.mobile.pantry.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.LocalSpacing
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
        onRefresh = viewModel::refresh,
        onClearError = viewModel::clearError,
        onSelectPantry = viewModel::onSelectPantry,
        onShowPantryDialog = viewModel::showPantryDialog,
        onPantryNameChange = viewModel::onPantryNameChanged,
        onPantryDescriptionChange = viewModel::onPantryDescriptionChanged,
        onSavePantry = viewModel::savePantry,
        onDeletePantry = viewModel::deleteCurrentPantry,
        onDismissPantryDialog = viewModel::dismissPantryDialog,
        onShowItemDialog = viewModel::showItemDialog,
        onItemNameChange = viewModel::onItemNameChanged,
        onItemQuantityChange = viewModel::onItemQuantityChanged,
        onItemUnitChange = viewModel::onItemUnitChanged,
        onItemExpirationChange = viewModel::onItemExpirationChanged,
        onSaveItem = viewModel::saveItem,
        onDismissItemDialog = viewModel::dismissItemDialog,
        onDeleteItem = viewModel::deleteItem,
        onShareEmailChange = viewModel::onShareEmailChanged,
        onInviteShare = viewModel::inviteShare,
        onRemoveSharedUser = viewModel::removeSharedUser,
    )
}

@Composable
fun PantryScreen(
    state: PantryUiState,
    onRefresh: () -> Unit,
    onClearError: () -> Unit,
    onSelectPantry: (String) -> Unit,
    onShowPantryDialog: (String?) -> Unit,
    onPantryNameChange: (String) -> Unit,
    onPantryDescriptionChange: (String) -> Unit,
    onSavePantry: () -> Unit,
    onDeletePantry: () -> Unit,
    onDismissPantryDialog: () -> Unit,
    onShowItemDialog: (String?) -> Unit,
    onItemNameChange: (String) -> Unit,
    onItemQuantityChange: (String) -> Unit,
    onItemUnitChange: (String) -> Unit,
    onItemExpirationChange: (String) -> Unit,
    onSaveItem: () -> Unit,
    onDismissItemDialog: () -> Unit,
    onDeleteItem: (String) -> Unit,
    onShareEmailChange: (String) -> Unit,
    onInviteShare: () -> Unit,
    onRemoveSharedUser: (String) -> Unit,
) {
    val spacing = LocalSpacing.current
    val selectedPantry = state.selectedPantry

    Scaffold(
        floatingActionButton = {
            if (selectedPantry != null) {
                AddFab(
                    onClick = { onShowItemDialog(null) },
                    contentDescription = stringResource(id = R.string.pantry_add_item),
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(id = R.string.pantry_heading),
                    style = MaterialTheme.typography.headlineSmall,
                )
                IconButton(onClick = onRefresh) {
                    Icon(imageVector = Icons.Outlined.Refresh, contentDescription = stringResource(id = R.string.common_retry))
                }
            }

            if (state.errorMessage != null) {
                ErrorBanner(
                    message = state.errorMessage,
                    onDismiss = onClearError,
                )
            }

            if (state.pantries.isEmpty()) {
                EmptyPantryState(onCreatePantry = { onShowPantryDialog(null) })
            } else {
                PantryPickerRow(
                    pantries = state.pantries,
                    selectedId = state.selectedPantryId,
                    onSelect = onSelectPantry,
                    onAddPantry = { onShowPantryDialog(null) },
                )

                selectedPantry?.let { pantry ->
                    PantryInfoCard(
                        pantry = pantry,
                        showManagementFeatures = state.showManagementFeatures,
                        onEdit = { onShowPantryDialog(pantry.id) },
                    )
                }

                if (state.showManagementFeatures) {
                    ShareSection(
                        pantry = selectedPantry,
                        shareState = state.shareState,
                        onEmailChanged = onShareEmailChange,
                        onInvite = onInviteShare,
                        onRemoveUser = onRemoveSharedUser,
                    )
                }

                Text(
                    text = stringResource(id = R.string.pantry_items_heading),
                    style = MaterialTheme.typography.titleMedium,
                )

                if (state.isLoading && state.items.isEmpty()) {
                    CircularProgressIndicator()
                } else if (state.items.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.pantry_empty_items),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(spacing.small),
                    ) {
                        items(state.items, key = { it.id }) { item ->
                            PantryItemCard(
                                item = item,
                                onEdit = { onShowItemDialog(item.id) },
                                onDelete = { onDeleteItem(item.id) },
                            )
                        }
                    }
                }
            }
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

    if (state.itemDialog.isVisible) {
        PantryItemDialog(
            state = state.itemDialog,
            onNameChange = onItemNameChange,
            onQuantityChange = onItemQuantityChange,
            onUnitChange = onItemUnitChange,
            onExpirationChange = onItemExpirationChange,
            onDismiss = onDismissItemDialog,
            onSave = onSaveItem,
        )
    }
}

@Composable
private fun PantryPickerRow(
    pantries: List<PantrySummary>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onAddPantry: () -> Unit,
) {
    val spacing = LocalSpacing.current
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(pantries, key = { it.id }) { pantry ->
            FilterChip(
                selected = pantry.id == selectedId,
                onClick = { onSelect(pantry.id) },
                label = {
                    Text(
                        text = pantry.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
        item {
            AssistChip(
                onClick = onAddPantry,
                label = { Text(text = stringResource(id = R.string.pantry_add_pantry)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
                },
            )
        }
    }
}

@Composable
private fun PantryInfoCard(
    pantry: PantrySummary,
    showManagementFeatures: Boolean,
    onEdit: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
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
                Text(
                    text = pantry.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Outlined.Edit, contentDescription = stringResource(id = R.string.pantry_edit_pantry))
                }
            }
            if (!pantry.description.isNullOrBlank()) {
                Text(
                    text = pantry.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (showManagementFeatures) {
                Text(
                    text = stringResource(id = R.string.pantry_shared_count, pantry.sharedUsers.size),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun ShareSection(
    pantry: PantrySummary?,
    shareState: PantryShareUiState,
    onEmailChanged: (String) -> Unit,
    onInvite: () -> Unit,
    onRemoveUser: (String) -> Unit,
) {
    val spacing = LocalSpacing.current
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
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
                Text(
                    text = stringResource(id = R.string.pantry_share_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            if (pantry == null) {
                Text(
                    text = stringResource(id = R.string.pantry_share_no_selection),
                    style = MaterialTheme.typography.bodySmall,
                )
                return@Column
            }

            if (pantry.sharedUsers.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.pantry_share_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.tiny)) {
                    pantry.sharedUsers.forEach { user ->
                        Surface(
                            tonalElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = spacing.small, vertical = spacing.tiny),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(text = user.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = user.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                IconButton(
                                    onClick = { onRemoveUser(user.id) },
                                    enabled = shareState.removingUserId != user.id,
                                ) {
                                    if (shareState.removingUserId == user.id) {
                                        CircularProgressIndicator(modifier = Modifier.height(16.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(imageVector = Icons.Outlined.Delete, contentDescription = stringResource(id = R.string.pantry_share_remove))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Divider()

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = shareState.email,
                onValueChange = onEmailChanged,
                label = { Text(text = stringResource(id = R.string.pantry_share_email_hint)) },
                singleLine = true,
                isError = shareState.errorMessageRes != null,
                supportingText = shareState.errorMessageRes?.let { resId ->
                    { Text(text = stringResource(id = resId), color = MaterialTheme.colorScheme.error) }
                },
            )
            FilledTonalButton(
                onClick = onInvite,
                enabled = !shareState.isInviting,
                modifier = Modifier.wrapContentWidth(Alignment.End),
            ) {
                if (shareState.isInviting) {
                    CircularProgressIndicator(modifier = Modifier.height(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(text = stringResource(id = R.string.pantry_share_invite))
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
                    label = { Text(text = stringResource(id = R.string.pantry_name_label)) },
                    singleLine = true,
                    enabled = !state.isSubmitting,
                    isError = state.errorMessageRes != null,
                    supportingText = state.errorMessageRes?.let { resId ->
                        { Text(text = stringResource(id = resId), color = MaterialTheme.colorScheme.error) }
                    },
                )
                OutlinedTextField(
                    value = state.description,
                    onValueChange = onDescriptionChange,
                    label = { Text(text = stringResource(id = R.string.pantry_description_label)) },
                    enabled = !state.isSubmitting,
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
                    label = { Text(text = stringResource(id = R.string.pantry_item_name_label)) },
                    enabled = !state.isEditing && !state.isSubmitting,
                    singleLine = true,
                    isError = state.errorMessageRes != null,
                    supportingText = state.errorMessageRes?.let { resId ->
                        { Text(text = stringResource(id = resId), color = MaterialTheme.colorScheme.error) }
                    },
                )
                OutlinedTextField(
                    value = state.quantity,
                    onValueChange = onQuantityChange,
                    label = { Text(text = stringResource(id = R.string.pantry_item_quantity_label)) },
                    enabled = !state.isSubmitting,
                    singleLine = true,
                )
                OutlinedTextField(
                    value = state.unit,
                    onValueChange = onUnitChange,
                    label = { Text(text = stringResource(id = R.string.pantry_item_unit_label)) },
                    enabled = !state.isSubmitting,
                    singleLine = true,
                )
                OutlinedTextField(
                    value = state.expirationDate,
                    onValueChange = onExpirationChange,
                    label = { Text(text = stringResource(id = R.string.pantry_item_expiration_label)) },
                    enabled = !state.isSubmitting,
                    singleLine = true,
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
