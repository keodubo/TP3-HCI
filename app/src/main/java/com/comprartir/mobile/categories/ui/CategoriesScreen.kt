package com.comprartir.mobile.categories.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CategoriesRoute(
    onBackClick: () -> Unit = {},
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
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    state: CategoriesUiState,
    onEvent: (CategoriesEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit = {},
) {
    val spacing = LocalSpacing.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.title_categories)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = stringResource(id = R.string.cd_back))
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(CategoriesEvent.ShowCreateDialog) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = stringResource(id = R.string.categories_new))
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
            )
        },
    ) { paddingValues ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)

        when {
            state.isLoading -> {
                Box(modifier = contentModifier, contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.categories.isEmpty() -> {
                CategoriesEmptyState(modifier = contentModifier)
            }
            else -> {
                CategoryList(
                    categories = state.categories,
                    onEdit = { category -> onEvent(CategoriesEvent.ShowEditDialog(category.id)) },
                    onDelete = { category -> onEvent(CategoriesEvent.RequestDelete(category.id)) },
                    modifier = contentModifier,
                )
            }
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
}

@Composable
private fun CategoriesEmptyState(modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = spacing.extraLarge),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.categories_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CategoryList(
    categories: List<CategoryItemUi>,
    onEdit: (CategoryItemUi) -> Unit,
    onDelete: (CategoryItemUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(spacing.large),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        items(categories, key = { it.id }) { category ->
            CategoryItemRow(
                category = category,
                onEdit = { onEdit(category) },
                onDelete = { onDelete(category) },
            )
        }
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
