package com.comprartir.mobile.lists.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.ComprartirPillShape
import com.comprartir.mobile.core.designsystem.LocalSpacing

@Composable
fun ShareListRoute(
    onBack: () -> Unit,
    viewModel: ShareListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(state.infoMessageRes, state.errorMessage) {
        state.infoMessageRes?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            viewModel.onMessageConsumed()
        }
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onErrorConsumed()
        }
    }

    ShareListScreen(
        state = state,
        onBack = onBack,
        onRefresh = viewModel::refreshSharedUsers,
        onEmailChange = viewModel::onInviteEmailChanged,
        onInvite = viewModel::onInviteClicked,
        onRemoveUser = viewModel::onRemoveSharedUserClicked,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
fun ShareListScreen(
    state: ShareListUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onEmailChange: (String) -> Unit,
    onInvite: () -> Unit,
    onRemoveUser: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val spacing = LocalSpacing.current
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.share_list_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = stringResource(id = R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(imageVector = Icons.Outlined.Refresh, contentDescription = stringResource(id = R.string.common_retry))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.large),
        ) {
            Text(
                text = stringResource(id = R.string.share_list_heading, state.listId.ifBlank { "N/A" }),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = stringResource(id = R.string.share_list_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(spacing.small),
                ) {
                    Text(text = stringResource(id = R.string.share_list_link_label), style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = state.shareLink.ifBlank { stringResource(id = R.string.share_list_link_placeholder) },
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    TextButton(onClick = {
                        if (state.shareLink.isNotBlank()) {
                            clipboardManager.setText(AnnotatedString(state.shareLink))
                        }
                    }) {
                        Text(text = stringResource(id = R.string.share_list_copy_link))
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
                Text(
                    text = stringResource(id = R.string.share_list_invite_label),
                    style = MaterialTheme.typography.labelMedium,
                )
                OutlinedTextField(
                    value = state.inviteEmail,
                    onValueChange = onEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = ComprartirPillShape,
                    placeholder = { Text(text = stringResource(id = R.string.share_list_invite_placeholder)) },
                )
                Button(
                    onClick = onInvite,
                    enabled = !state.isInviting,
                    shape = ComprartirPillShape,
                ) {
                    if (state.isInviting) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = spacing.xs),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    }
                    Text(text = stringResource(id = R.string.share_list_invite_action))
                }
            }
            Divider()
            Text(
                text = stringResource(id = R.string.share_list_shared_section),
                style = MaterialTheme.typography.titleMedium,
            )
            if (state.isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.sharedUsers.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.share_list_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.small),
                ) {
                    items(state.sharedUsers, key = { it.id }) { user ->
                        SharedUserRow(
                            user = user,
                            isRemoving = state.removingUserId == user.id,
                            onRemove = { onRemoveUser(user.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SharedUserRow(
    user: SharedUserUi,
    isRemoving: Boolean,
    onRemove: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(spacing.tiny),
            ) {
                Text(text = user.displayName, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(
                onClick = onRemove,
                enabled = !isRemoving,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                if (isRemoving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.error,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(id = R.string.share_list_remove),
                    )
                }
            }
        }
    }
}
