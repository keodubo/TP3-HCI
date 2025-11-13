package com.comprartir.mobile.profile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.LocalSpacing

@Composable
fun ChangePasswordRoute(
    onNavigateBackWithSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ChangePasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(state.isPasswordChanged) {
        if (state.isPasswordChanged) {
            // Navigate back with success flag
            onNavigateBackWithSuccess()
        }
    }

    LaunchedEffect(state.generalError) {
        state.generalError?.let { errorRes ->
            val message = context.getString(errorRes)
            snackbarHostState.showSnackbar(message = message)
            viewModel.onErrorDismissed()
        }
    }

    ChangePasswordScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onCurrentPasswordChanged = viewModel::onCurrentPasswordChanged,
        onNewPasswordChanged = viewModel::onNewPasswordChanged,
        onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
        onToggleCurrentPasswordVisibility = viewModel::onToggleCurrentPasswordVisibility,
        onToggleNewPasswordVisibility = viewModel::onToggleNewPasswordVisibility,
        onToggleConfirmPasswordVisibility = viewModel::onToggleConfirmPasswordVisibility,
        onSaveClick = viewModel::onSavePassword,
        onCancelClick = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    state: ChangePasswordUiState,
    snackbarHostState: SnackbarHostState,
    onCurrentPasswordChanged: (String) -> Unit,
    onNewPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onToggleCurrentPasswordVisibility: () -> Unit,
    onToggleNewPasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    val spacing = LocalSpacing.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.title_change_password),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = spacing.medium,
                        vertical = spacing.large,
                    ),
                verticalArrangement = Arrangement.spacedBy(spacing.medium),
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp,
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.large),
                        verticalArrangement = Arrangement.spacedBy(spacing.medium),
                    ) {
                        Text(
                            text = stringResource(R.string.title_change_password),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        // Current password field
                        OutlinedTextField(
                            value = state.currentPassword,
                            onValueChange = onCurrentPasswordChanged,
                            label = { Text(stringResource(R.string.change_password_current)) },
                            visualTransformation = if (state.showCurrentPassword) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(onClick = onToggleCurrentPasswordVisibility) {
                                    Icon(
                                        imageVector = if (state.showCurrentPassword) {
                                            Icons.Filled.Visibility
                                        } else {
                                            Icons.Filled.VisibilityOff
                                        },
                                        contentDescription = if (state.showCurrentPassword) {
                                            stringResource(R.string.action_hide_password)
                                        } else {
                                            stringResource(R.string.action_show_password)
                                        },
                                    )
                                }
                            },
                            isError = state.currentPasswordError != null,
                            supportingText = state.currentPasswordError?.let { errorRes ->
                                { Text(text = stringResource(errorRes)) }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isLoading,
                            singleLine = true,
                        )

                        // New password field
                        OutlinedTextField(
                            value = state.newPassword,
                            onValueChange = onNewPasswordChanged,
                            label = { Text(stringResource(R.string.change_password_new)) },
                            visualTransformation = if (state.showNewPassword) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(onClick = onToggleNewPasswordVisibility) {
                                    Icon(
                                        imageVector = if (state.showNewPassword) {
                                            Icons.Filled.Visibility
                                        } else {
                                            Icons.Filled.VisibilityOff
                                        },
                                        contentDescription = if (state.showNewPassword) {
                                            stringResource(R.string.action_hide_password)
                                        } else {
                                            stringResource(R.string.action_show_password)
                                        },
                                    )
                                }
                            },
                            isError = state.newPasswordError != null,
                            supportingText = state.newPasswordError?.let { errorRes ->
                                { Text(text = stringResource(errorRes)) }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isLoading,
                            singleLine = true,
                        )

                        // Confirm password field
                        OutlinedTextField(
                            value = state.confirmPassword,
                            onValueChange = onConfirmPasswordChanged,
                            label = { Text(stringResource(R.string.change_password_confirm)) },
                            visualTransformation = if (state.showConfirmPassword) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(onClick = onToggleConfirmPasswordVisibility) {
                                    Icon(
                                        imageVector = if (state.showConfirmPassword) {
                                            Icons.Filled.Visibility
                                        } else {
                                            Icons.Filled.VisibilityOff
                                        },
                                        contentDescription = if (state.showConfirmPassword) {
                                            stringResource(R.string.action_hide_password)
                                        } else {
                                            stringResource(R.string.action_show_password)
                                        },
                                    )
                                }
                            },
                            isError = state.confirmPasswordError != null,
                            supportingText = state.confirmPasswordError?.let { errorRes ->
                                { Text(text = stringResource(errorRes)) }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isLoading,
                            singleLine = true,
                        )

                        Spacer(modifier = Modifier.height(spacing.small))

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.small),
                        ) {
                            // Cancel button
                            OutlinedButton(
                                onClick = onCancelClick,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(999.dp),
                                enabled = !state.isLoading,
                            ) {
                                Text(stringResource(R.string.change_password_cancel_button))
                            }

                            // Save button
                            Button(
                                onClick = onSaveClick,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(999.dp),
                                enabled = !state.isLoading,
                            ) {
                                if (state.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Text(stringResource(R.string.change_password_save_button))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
