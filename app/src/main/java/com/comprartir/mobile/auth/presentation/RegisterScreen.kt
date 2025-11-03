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
import com.comprartir.mobile.core.navigation.AppDestination
import com.comprartir.mobile.core.navigation.NavigationIntent

@Composable
fun RegisterRoute(
    onNavigate: (NavigationIntent) -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RegisterScreen(
        state = state,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
        onRegister = {
            viewModel.register(
                onSuccess = { onNavigate(NavigationIntent(AppDestination.Verify)) },
                onError = { /* TODO: Surface error */ },
            )
        },
        onSignIn = { onNavigate(NavigationIntent(AppDestination.SignIn)) },
    )
}

@Composable
fun RegisterScreen(
    state: com.comprartir.mobile.auth.domain.RegistrationState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onRegister: () -> Unit,
    onSignIn: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.large, vertical = spacing.extraLarge),
        verticalArrangement = Arrangement.spacedBy(spacing.large),
    ) {
        Text(text = stringResource(id = R.string.headline_register))
        ComprartirOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.credentials.email,
            onValueChange = onEmailChanged,
            label = { Text(stringResource(id = R.string.label_email)) },
            singleLine = true,
        )
        ComprartirOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.credentials.password,
            onValueChange = onPasswordChanged,
            label = { Text(stringResource(id = R.string.label_password)) },
            singleLine = true,
        )
        ComprartirOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.confirmPassword,
            onValueChange = onConfirmPasswordChanged,
            label = { Text(stringResource(id = R.string.label_confirm_password)) },
            singleLine = true,
        )
        state.errorMessageRes?.let { errorRes ->
            Text(
                text = stringResource(id = errorRes),
                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
            )
        }
        Button(onClick = onRegister, enabled = !state.isLoading) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Text(text = stringResource(id = R.string.action_register))
            }
        }
        Button(onClick = onSignIn) {
            Text(text = stringResource(id = R.string.action_back_to_sign_in))
        }
    }
}
