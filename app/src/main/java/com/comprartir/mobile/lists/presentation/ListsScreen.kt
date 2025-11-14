package com.comprartir.mobile.lists.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.navigation.AppDestination
import com.comprartir.mobile.core.navigation.NavigationIntent
import com.comprartir.mobile.feature.lists.ui.components.CreateListDialog
import com.comprartir.mobile.feature.lists.ui.components.EditListDialog
import com.comprartir.mobile.feature.lists.ui.components.DeleteListDialog
import com.comprartir.mobile.lists.data.ShoppingList

@Composable
fun ListsRoute(
    onNavigate: (NavigationIntent) -> Unit,
    viewModel: ListsViewModel = hiltViewModel(),
    openCreateDialog: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
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
    
    ListsScreen(
        state = state,
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
        onShowDeleteDialog = viewModel::showDeleteDialog,
        onDismissDeleteDialog = viewModel::dismissDeleteDialog,
        onConfirmDeleteList = viewModel::confirmDeleteList,
        onListSelected = { listId ->
            onNavigate(NavigationIntent(AppDestination.ListDetails, mapOf("listId" to listId)))
        },
        onShareList = { listId ->
            onNavigate(NavigationIntent(AppDestination.ShareList, mapOf("listId" to listId)))
        },
        onRefresh = viewModel::refresh,
        onClearError = viewModel::clearError,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    state: ListsUiState,
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
    onShowDeleteDialog: (ShoppingList) -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDeleteList: () -> Unit,
    onListSelected: (String) -> Unit,
    onShareList: (String) -> Unit,
    onRefresh: () -> Unit,
    onClearError: () -> Unit,
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = contentPadding.calculateTopPadding() + spacing.medium,
                bottom = contentPadding.calculateBottomPadding(),
                start = spacing.large,
                end = spacing.large
            ),
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            text = stringResource(id = R.string.lists_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.size(spacing.large))

        if (state.isLoading && state.lists.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        if (state.errorMessage != null && state.lists.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
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

        if (!state.isLoading && state.errorMessage == null && state.lists.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(spacing.large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.lists_empty_title),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(id = R.string.lists_empty_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = spacing.small)
                )
                Spacer(modifier = Modifier.padding(top = spacing.large))
                // FAB available in bottom-right corner
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(state.lists, key = { it.id }) { list ->
                ShoppingListCard(
                    list = list,
                    onListClick = { onListSelected(list.id) },
                    onEditClick = { onShowEditDialog(list) },
                    onDeleteClick = { onShowDeleteDialog(list) },
                    onShareClick = { onShareList(list.id) },
                )
            }
        }
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
}

@Composable
private fun ShoppingListCard(
    list: ShoppingList,
    onListClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    val spacing = LocalSpacing.current
    
    Card(
        onClick = onListClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            // Header: Name
            Text(
                text = list.name.ifBlank { stringResource(id = R.string.lists_default_title) },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            
            // Description
            Text(
                text = list.description?.takeIf { it.isNotBlank() } 
                    ?: stringResource(id = R.string.lists_no_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = spacing.small)
            )
            
            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalIconButton(
                    onClick = { onShareClick() },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(id = R.string.lists_share),
                        modifier = Modifier.size(18.dp),
                    )
                }
                
                FilledTonalIconButton(
                    onClick = { onEditClick() },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(id = R.string.lists_edit),
                        modifier = Modifier.size(18.dp),
                    )
                }
                
                FilledTonalIconButton(
                    onClick = { onDeleteClick() },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.lists_delete),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
