package com.comprartir.mobile.lists.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.designsystem.ComprartirOutlinedTextField
import com.comprartir.mobile.core.navigation.AppDestination
import com.comprartir.mobile.core.navigation.NavigationIntent

@Composable
fun ListsRoute(
    onNavigate: (NavigationIntent) -> Unit,
    viewModel: ListsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ListsScreen(
        state = state,
        onCreateList = viewModel::createList,
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
    onCreateList: (String) -> Unit,
    onListSelected: (String) -> Unit,
    onShareList: (String) -> Unit,
    onRefresh: () -> Unit,
    onClearError: () -> Unit,
) {
    val spacing = LocalSpacing.current
    var newListName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.large, vertical = spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.large),
    ) {
        ComprartirOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = newListName,
            onValueChange = { newListName = it },
            label = { Text(stringResource(id = R.string.lists_new_name)) },
            isError = state.errorMessage != null,
            supportingText = state.errorMessage?.let { message ->
                { Text(text = message, color = MaterialTheme.colorScheme.error) }
            },
        )
        Button(onClick = {
            if (newListName.isNotBlank()) {
                onCreateList(newListName)
                newListName = ""
            }
        }, enabled = !state.isLoading) {
            Text(text = stringResource(id = R.string.lists_create))
        }
        if (state.isLoading && state.lists.isEmpty()) {
            CircularProgressIndicator()
        }
        if (state.errorMessage != null && state.lists.isEmpty()) {
            Button(onClick = {
                onClearError()
                onRefresh()
            }) {
                Text(text = stringResource(id = R.string.common_retry))
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
            items(state.lists, key = { it.id }) { list ->
                Card(onClick = { onListSelected(list.id) }) {
                    Column(modifier = Modifier.padding(spacing.medium)) {
                        Text(text = list.name.ifBlank { stringResource(id = R.string.lists_default_title) })
                        Button(onClick = { onShareList(list.id) }) {
                            Text(text = stringResource(id = R.string.lists_share))
                        }
                    }
                }
            }
        }
    }
}
