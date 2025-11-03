package com.comprartir.mobile.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.designsystem.ComprartirOutlinedTextField

@Composable
fun UpdatePasswordRoute(
    onNavigate: (com.comprartir.mobile.core.navigation.NavigationIntent) -> Unit,
    viewModel: UpdatePasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    UpdatePasswordScreen(
        state = state,
        onCurrentPasswordChanged = viewModel::onCurrentPasswordChanged,
        onNewPasswordChanged = viewModel::onNewPasswordChanged,
        onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
        onUpdate = {
            viewModel.updatePassword(
                onSuccess = { /* TODO: Navigate or show confirmation */ },
                onError = { /* TODO: Show error */ },
            )
        },
    )
}

@Composable
fun UpdatePasswordScreen(
    state: com.comprartir.mobile.auth.domain.PasswordUpdateState,
    onCurrentPasswordChanged: (String) -> Unit,
    onNewPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onUpdate: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.large, vertical = spacing.extraLarge),
        verticalArrangement = Arrangement.spacedBy(spacing.large),
    ) {
        Text(text = stringResource(id = R.string.headline_update_password))
        ComprartirOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.currentPassword,
            onValueChange = onCurrentPasswordChanged,
            label = { Text(stringResource(id = R.string.label_current_password)) },
            singleLine = true,
        )
        ComprartirOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.newPassword,
            onValueChange = onNewPasswordChanged,
            label = { Text(stringResource(id = R.string.label_new_password)) },
            singleLine = true,
        )
        ComprartirOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.confirmPassword,
            onValueChange = onConfirmPasswordChanged,
            label = { Text(stringResource(id = R.string.label_confirm_new_password)) },
            singleLine = true,
        )
        state.errorMessageRes?.let { errorRes ->
            Text(
                text = stringResource(id = errorRes),
                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
            )
        }
        Button(onClick = onUpdate, enabled = !state.isLoading) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Text(text = stringResource(id = R.string.action_save_changes))
            }
        }
    }
}
