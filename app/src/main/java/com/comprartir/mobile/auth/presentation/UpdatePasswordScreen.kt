package com.comprartir.mobile.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.designsystem.ComprartirOutlinedTextField
import com.comprartir.mobile.core.ui.rememberIsLandscape
import com.comprartir.mobile.core.ui.rememberIsTablet

@Composable
fun UpdatePasswordRoute(
    onNavigate: (com.comprartir.mobile.core.navigation.NavigationIntent) -> Unit,
    windowSizeClass: WindowSizeClass? = null,
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
        windowSizeClass = windowSizeClass,
    )
}

@Composable
fun UpdatePasswordScreen(
    state: com.comprartir.mobile.auth.domain.PasswordUpdateState,
    onCurrentPasswordChanged: (String) -> Unit,
    onNewPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onUpdate: () -> Unit,
    windowSizeClass: WindowSizeClass? = null,
) {
    val spacing = LocalSpacing.current
    val isLandscape = rememberIsLandscape()
    val isTablet = windowSizeClass?.let { rememberIsTablet(it) } ?: false
    val useWideLayout = isTablet || isLandscape
    val horizontalPadding = if (isTablet) spacing.xxl * 2 else spacing.xl
    val verticalPadding = if (isTablet) spacing.xl else spacing.large
    val cardMaxWidth = if (isTablet) 600.dp else if (useWideLayout) 520.dp else Dp.Unspecified
    val cardModifier = if (cardMaxWidth == Dp.Unspecified) {
        Modifier.fillMaxWidth()
    } else {
        Modifier
            .fillMaxWidth()
            .widthIn(max = cardMaxWidth)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        UpdatePasswordFormCard(
            state = state,
            onCurrentPasswordChanged = onCurrentPasswordChanged,
            onNewPasswordChanged = onNewPasswordChanged,
            onConfirmPasswordChanged = onConfirmPasswordChanged,
            onUpdate = onUpdate,
            modifier = cardModifier,
            showInfoPanel = useWideLayout,
            isWideLayout = useWideLayout,
            isTabletLayout = isTablet,
        )
    }
}

@Composable
private fun UpdatePasswordInfoCard(
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Card(
        modifier = modifier,
        shape = CardDefaults.shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            Text(
                text = stringResource(id = R.string.headline_update_password),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(id = R.string.profile_change_password_button),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun UpdatePasswordFormCard(
    state: com.comprartir.mobile.auth.domain.PasswordUpdateState,
    onCurrentPasswordChanged: (String) -> Unit,
    onNewPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onUpdate: () -> Unit,
    modifier: Modifier = Modifier,
    showInfoPanel: Boolean,
    isWideLayout: Boolean,
    isTabletLayout: Boolean,
) {
    val spacing = LocalSpacing.current
    val horizontalPadding = if (isTabletLayout) spacing.xl else spacing.large
    val verticalPadding = if (isTabletLayout) spacing.xl else spacing.large
    Card(
        modifier = modifier,
        shape = CardDefaults.shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            verticalArrangement = Arrangement.spacedBy(spacing.large),
        ) {
            if (showInfoPanel) {
                UpdatePasswordInfoCard(modifier = Modifier.fillMaxWidth())
            } else {
                Text(
                    text = stringResource(id = R.string.headline_update_password),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            ComprartirOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.currentPassword,
                onValueChange = onCurrentPasswordChanged,
                singleLine = true,
                placeholder = { Text(stringResource(id = R.string.label_current_password)) },
            )
            if (isWideLayout) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                ) {
                    ComprartirOutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = state.newPassword,
                        onValueChange = onNewPasswordChanged,
                        singleLine = true,
                        placeholder = { Text(stringResource(id = R.string.label_new_password)) },
                    )
                    ComprartirOutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = state.confirmPassword,
                        onValueChange = onConfirmPasswordChanged,
                        singleLine = true,
                        placeholder = { Text(stringResource(id = R.string.label_confirm_new_password)) },
                    )
                }
            } else {
                ComprartirOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.newPassword,
                    onValueChange = onNewPasswordChanged,
                    singleLine = true,
                    placeholder = { Text(stringResource(id = R.string.label_new_password)) },
                )
                ComprartirOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.confirmPassword,
                    onValueChange = onConfirmPasswordChanged,
                    singleLine = true,
                    placeholder = { Text(stringResource(id = R.string.label_confirm_new_password)) },
                )
            }
            state.errorMessageRes?.let { errorRes ->
                Text(
                    text = stringResource(id = errorRes),
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Button(onClick = onUpdate, enabled = !state.isLoading) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                } else {
                    Text(text = stringResource(id = R.string.action_save_changes))
                }
            }
        }
    }
}
