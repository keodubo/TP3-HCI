package com.comprartir.mobile.pantry.presentation

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.comprartir.mobile.core.designsystem.darkNavy
import com.comprartir.mobile.core.designsystem.surfaceCard
import com.comprartir.mobile.core.designsystem.textMuted
import com.comprartir.mobile.core.ui.LocalAppBarTitle
import com.comprartir.mobile.shared.components.AddFab
import com.comprartir.mobile.pantry.data.PantryItem
import com.comprartir.mobile.core.ui.rememberIsLandscape
import com.comprartir.mobile.core.ui.rememberIsTablet
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PantryDetailRoute(
    pantryId: String,
    onNavigateBack: () -> Unit,
    windowSizeClass: WindowSizeClass? = null,
    viewModel: PantryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val appBarTitleState = LocalAppBarTitle.current
    val pantryTitle = state.selectedPantry?.name?.takeIf { it.isNotBlank() }
        ?: context.getString(R.string.title_pantry)

    LaunchedEffect(pantryTitle) {
        appBarTitleState.value = pantryTitle
    }
    DisposableEffect(Unit) {
        onDispose { appBarTitleState.value = null }
    }
    
    LaunchedEffect(pantryId) {
        viewModel.onSelectPantry(pantryId)
    }
    val isTablet = windowSizeClass?.let { rememberIsTablet(it) } ?: false
    
    PantryDetailScreen(
        pantry = state.selectedPantry,
        items = state.items,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        itemDialog = state.itemDialog,
        isTablet = isTablet,
        onNavigateBack = onNavigateBack,
        onRefresh = viewModel::refresh,
        onClearError = viewModel::clearError,
        onShowItemDialog = viewModel::showItemDialog,
        onItemNameChange = viewModel::onItemNameChanged,
        onItemQuantityChange = viewModel::onItemQuantityChanged,
        onItemUnitChange = viewModel::onItemUnitChanged,
        onItemExpirationChange = viewModel::onItemExpirationChanged,
        onSaveItem = viewModel::saveItem,
        onDismissItemDialog = viewModel::dismissItemDialog,
        onDeleteItem = viewModel::deleteItem,
        onIncreaseQuantity = viewModel::increaseItemQuantity,
        onDecreaseQuantity = viewModel::decreaseItemQuantity,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryDetailScreen(
    pantry: com.comprartir.mobile.pantry.data.PantrySummary?,
    items: List<PantryItem>,
    isLoading: Boolean,
    errorMessage: String?,
    itemDialog: PantryItemDialogState,
    isTablet: Boolean,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onClearError: () -> Unit,
    onShowItemDialog: (String?) -> Unit,
    onItemNameChange: (String) -> Unit,
    onItemQuantityChange: (String) -> Unit,
    onItemUnitChange: (String) -> Unit,
    onItemExpirationChange: (String) -> Unit,
    onSaveItem: () -> Unit,
    onDismissItemDialog: () -> Unit,
    onDeleteItem: (String) -> Unit,
    onIncreaseQuantity: (String) -> Unit,
    onDecreaseQuantity: (String) -> Unit,
) {
    val spacing = LocalSpacing.current
    val isLandscape = rememberIsLandscape()
    val showWideLayout = isTablet || isLandscape

    LaunchedEffect(showWideLayout, itemDialog.isVisible) {
        if (showWideLayout && !itemDialog.isVisible) {
            onShowItemDialog(null)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = pantry?.name ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.common_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = stringResource(id = R.string.common_retry),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            if (!showWideLayout) {
                AddFab(
                    onClick = { onShowItemDialog(null) },
                    contentDescription = stringResource(id = R.string.pantry_add_item),
                )
            }
        },
    ) { padding ->
        if (showWideLayout) {
            PantryDetailWideContent(
                pantry = pantry,
                items = items,
                isLoading = isLoading,
                errorMessage = errorMessage,
                itemDialog = itemDialog,
                padding = padding,
                isTablet = isTablet,
                onClearError = onClearError,
                onShowItemDialog = onShowItemDialog,
                onItemNameChange = onItemNameChange,
                onItemQuantityChange = onItemQuantityChange,
                onItemUnitChange = onItemUnitChange,
                onItemExpirationChange = onItemExpirationChange,
                onSaveItem = onSaveItem,
                onDismissItemDialog = onDismissItemDialog,
                onDeleteItem = onDeleteItem,
                onIncreaseQuantity = onIncreaseQuantity,
                onDecreaseQuantity = onDecreaseQuantity,
            )
        } else {
            PantryDetailPortrait(
                pantry = pantry,
                items = items,
                isLoading = isLoading,
                errorMessage = errorMessage,
                itemDialog = itemDialog,
                padding = padding,
                onClearError = onClearError,
                onShowItemDialog = onShowItemDialog,
                onDeleteItem = onDeleteItem,
                onIncreaseQuantity = onIncreaseQuantity,
                onDecreaseQuantity = onDecreaseQuantity,
            )
        }
    }

    if (!showWideLayout && itemDialog.isVisible) {
        PantryItemDialog(
            state = itemDialog,
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
private fun PantryDetailPortrait(
    pantry: com.comprartir.mobile.pantry.data.PantrySummary?,
    items: List<PantryItem>,
    isLoading: Boolean,
    errorMessage: String?,
    itemDialog: PantryItemDialogState,
    padding: PaddingValues,
    onClearError: () -> Unit,
    onShowItemDialog: (String?) -> Unit,
    onDeleteItem: (String) -> Unit,
    onIncreaseQuantity: (String) -> Unit,
    onDecreaseQuantity: (String) -> Unit,
) {
    val spacing = LocalSpacing.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(
            start = spacing.large,
            end = spacing.large,
            top = spacing.medium,
            bottom = 80.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        if (errorMessage != null) {
            item(key = "error") {
                ErrorBanner(
                    message = errorMessage,
                    onDismiss = onClearError,
                )
            }
        }
        if (pantry != null && !pantry.description.isNullOrBlank()) {
            item(key = "description") {
                PantryDescriptionCard(description = pantry.description)
            }
        }
        if (isLoading && items.isEmpty()) {
            item(key = "loading") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        if (!isLoading && items.isEmpty()) {
            item(key = "empty") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.medium),
                ) {
                    Text(
                        text = stringResource(id = R.string.pantry_empty_items),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        items(items, key = { it.id }) { item ->
            PantryItemCard(
                item = item,
                onEdit = { onShowItemDialog(item.id) },
                onDelete = { onDeleteItem(item.id) },
                onIncrease = { onIncreaseQuantity(item.id) },
                onDecrease = { onDecreaseQuantity(item.id) },
            )
        }
    }
}

@Composable
private fun PantryDetailWideContent(
    pantry: com.comprartir.mobile.pantry.data.PantrySummary?,
    items: List<PantryItem>,
    isLoading: Boolean,
    errorMessage: String?,
    itemDialog: PantryItemDialogState,
    padding: PaddingValues,
    isTablet: Boolean,
    onClearError: () -> Unit,
    onShowItemDialog: (String?) -> Unit,
    onItemNameChange: (String) -> Unit,
    onItemQuantityChange: (String) -> Unit,
    onItemUnitChange: (String) -> Unit,
    onItemExpirationChange: (String) -> Unit,
    onSaveItem: () -> Unit,
    onDismissItemDialog: () -> Unit,
    onDeleteItem: (String) -> Unit,
    onIncreaseQuantity: (String) -> Unit,
    onDecreaseQuantity: (String) -> Unit,
) {
    val spacing = LocalSpacing.current
    val sidebarWeight = if (isTablet) 0.3f else 0.4f
    val contentWeight = 1f - sidebarWeight
    val horizontalPadding = if (isTablet) spacing.xl else spacing.large
    val verticalPadding = if (isTablet) spacing.large else spacing.medium
    val sectionSpacing = if (isTablet) spacing.xl else spacing.large
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        horizontalArrangement = Arrangement.spacedBy(sectionSpacing),
    ) {
        Column(
            modifier = Modifier
                .weight(sidebarWeight)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(if (isTablet) spacing.large else spacing.medium),
        ) {
            pantry?.let {
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                it.description?.takeIf { desc -> desc.isNotBlank() }?.let { desc ->
                    PantryDescriptionCard(description = desc)
                }
            }
            if (errorMessage != null) {
                ErrorBanner(message = errorMessage, onDismiss = onClearError)
            }
            PantryItemInlineForm(
                state = itemDialog,
                onNameChange = onItemNameChange,
                onQuantityChange = onItemQuantityChange,
                onUnitChange = onItemUnitChange,
                onExpirationChange = onItemExpirationChange,
                onSave = onSaveItem,
                onCancel = onDismissItemDialog,
            )
        }
        Column(
            modifier = Modifier
                .weight(contentWeight)
                .fillMaxHeight(),
        ) {
            PantryItemsGrid(
                items = items,
                isLoading = isLoading,
                columns = if (isTablet) 2 else 1,
                onShowItemDialog = onShowItemDialog,
                onDeleteItem = onDeleteItem,
                onIncreaseQuantity = onIncreaseQuantity,
                onDecreaseQuantity = onDecreaseQuantity,
            )
        }
    }
}

@Composable
private fun PantryItemsGrid(
    items: List<PantryItem>,
    isLoading: Boolean,
    columns: Int,
    onShowItemDialog: (String?) -> Unit,
    onDeleteItem: (String) -> Unit,
    onIncreaseQuantity: (String) -> Unit,
    onDecreaseQuantity: (String) -> Unit,
) {
    val spacing = LocalSpacing.current
    val gridSpacing = if (columns > 1) spacing.large else spacing.medium
    when {
        isLoading && items.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        !isLoading && items.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(id = R.string.pantry_empty_items),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        else -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(gridSpacing),
                horizontalArrangement = Arrangement.spacedBy(gridSpacing),
            ) {
                items(items, key = { it.id }) { item ->
                    PantryItemCard(
                        item = item,
                        onEdit = { onShowItemDialog(item.id) },
                        onDelete = { onDeleteItem(item.id) },
                        onIncrease = { onIncreaseQuantity(item.id) },
                        onDecrease = { onDecreaseQuantity(item.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PantryDescriptionCard(description: String) {
    val spacing = LocalSpacing.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(spacing.medium),
        )
    }
}

@Composable
private fun PantryItemInlineForm(
    state: PantryItemDialogState,
    onNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onExpirationChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceCard),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.large),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            Text(
                text = if (state.isEditing) {
                    stringResource(id = R.string.pantry_edit_item)
                } else {
                    stringResource(id = R.string.pantry_add_item)
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                enabled = !state.isSubmitting,
                singleLine = true,
                placeholder = { Text(text = stringResource(id = R.string.pantry_item_name_label)) },
                isError = state.errorMessageRes != null,
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
            state.errorMessageRes?.let { resId ->
                Text(
                    text = stringResource(id = resId),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
            ) {
                Button(
                    onClick = onSave,
                    enabled = !state.isSubmitting,
                    modifier = Modifier.weight(1f),
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(text = stringResource(id = R.string.dialog_save))
                    }
                }
                TextButton(
                    onClick = onCancel,
                    enabled = !state.isSubmitting,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(id = R.string.dialog_cancel))
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
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val expirationLabel = androidx.compose.runtime.remember(item.expiresAt) {
        item.expiresAt?.let { expiration ->
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
            formatter.format(expiration.atZone(ZoneId.systemDefault()).toLocalDate())
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Product info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(spacing.tiny),
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                
                val quantityLabel = if (item.unit.isNullOrBlank()) {
                    "${item.quantity}"
                } else {
                    "${item.quantity} ${item.unit}"
                }
                
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.darkNavy,
                ) {
                    Text(
                        text = quantityLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
                
                if (expirationLabel != null) {
                    Text(
                        text = stringResource(id = R.string.pantry_item_expires, expirationLabel),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.textMuted,
                    )
                }
            }
            
            // Quantity controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.tiny),
            ) {
                // Increase button
                Surface(
                    onClick = onIncrease,
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(id = R.string.pantry_increase_quantity),
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                
                // Decrease button
                Surface(
                    onClick = onDecrease,
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Remove,
                        contentDescription = stringResource(id = R.string.pantry_decrease_quantity),
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
            
            // Actions
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(spacing.tiny),
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = stringResource(id = R.string.pantry_edit_item),
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(id = R.string.pantry_delete_item),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
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
    val spacing = LocalSpacing.current
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.medium),
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
