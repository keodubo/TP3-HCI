package com.comprartir.mobile.categories.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.R
import com.comprartir.mobile.categories.model.CategoriesEffect
import com.comprartir.mobile.categories.model.CategoriesEvent
import com.comprartir.mobile.categories.model.CategoriesUiState
import com.comprartir.mobile.categories.model.CategoryDialogState
import com.comprartir.mobile.categories.model.CategoryItemUi
import com.comprartir.mobile.categories.model.DeleteCategoryState
import com.comprartir.mobile.categories.viewmodel.CategoriesViewModel
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.shared.components.AddFab
import com.comprartir.mobile.shared.components.EmptyStateMessage
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CategoriesRoute(
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is CategoriesEffect.ShowMessage ->
                    snackbarHostState.showSnackbar(context.getString(effect.messageRes))
                is CategoriesEffect.ShowError ->
                    snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    CategoriesScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        contentPadding = contentPadding,
    )
}

@Composable
fun CategoriesScreen(
    state: CategoriesUiState,
    onEvent: (CategoriesEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val spacing = LocalSpacing.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = spacing.large,
                    end = spacing.large,
                    top = spacing.medium,
                ),
            verticalArrangement = Arrangement.Top,
        ) {
            if (state.isLoading && state.categories.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            if (!state.isLoading && state.categories.isEmpty()) {
                EmptyStateMessage(
                    title = stringResource(id = R.string.categories_empty_title),
                    subtitle = stringResource(id = R.string.categories_empty_subtitle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(spacing.medium),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(state.categories, key = { it.id }) { category ->
                    CategoryItemRow(
                        category = category,
                        onEdit = { onEvent(CategoriesEvent.ShowEditDialog(category.id)) },
                        onDelete = { onEvent(CategoriesEvent.RequestDelete(category.id)) },
                    )
                }
            }
        }

        AddFab(
            onClick = { onEvent(CategoriesEvent.ShowCreateDialog) },
            contentDescription = stringResource(id = R.string.categories_new),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = spacing.large,
                    bottom = spacing.large,
                ),
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = spacing.large)
                .padding(bottom = spacing.large),
        )
    }

    if (state.dialogState.isVisible) {
        CategoryDialog(
            state = state.dialogState,
            onNameChange = { onEvent(CategoriesEvent.DialogNameChanged(it)) },
            onDismiss = { onEvent(CategoriesEvent.DismissDialog) },
            onConfirm = { onEvent(CategoriesEvent.ConfirmDialog) },
        )
    }

    if (state.deleteState.isVisible) {
        DeleteCategoryDialog(
            state = state.deleteState,
            onDismiss = { onEvent(CategoriesEvent.DismissDelete) },
            onConfirm = { onEvent(CategoriesEvent.ConfirmDelete) },
        )
    }
}

@Composable
private fun CategoryItemRow(
    category: CategoryItemUi,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = category.name, style = MaterialTheme.typography.titleMedium)
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(imageVector = Icons.Outlined.Edit, contentDescription = stringResource(id = R.string.categories_edit))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Outlined.Delete, contentDescription = stringResource(id = R.string.categories_delete))
                    }
                }
            }
            val description = category.description
            if (!description.isNullOrBlank()) {
                Text(text = description, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun CategoryDialog(
    state: CategoryDialogState,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val titleRes = if (state.categoryId == null) {
        R.string.categories_new
    } else {
        R.string.categories_edit
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = state.canSubmit) {
                Text(text = stringResource(id = R.string.categories_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.categories_cancel))
            }
        },
        title = { Text(text = stringResource(id = titleRes)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(id = R.string.list_detail_category_label)) },
                    singleLine = true,
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
private fun DeleteCategoryDialog(
    state: DeleteCategoryState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.categories_delete_title)) },
        text = { Text(text = stringResource(id = R.string.categories_delete_message, state.categoryName)) },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !state.isSubmitting) {
                Text(text = stringResource(id = R.string.categories_delete_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.categories_cancel))
            }
        },
    )
}
