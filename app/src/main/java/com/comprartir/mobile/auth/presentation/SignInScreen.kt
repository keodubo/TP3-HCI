package com.comprartir.mobile.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.designsystem.ComprartirOutlinedTextField
import com.comprartir.mobile.core.navigation.AppDestination
import com.comprartir.mobile.core.navigation.NavigationIntent

@Composable
fun SignInRoute(
    onNavigate: (NavigationIntent) -> Unit,
    viewModel: SignInViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SignInScreen(
        state = state,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onSignIn = {
            viewModel.signIn(
                onSuccess = { onNavigate(NavigationIntent(AppDestination.Dashboard)) },
                onError = { /* TODO: Surface error feedback */ },
            )
        },
        onRegister = { onNavigate(NavigationIntent(AppDestination.Register)) },
        onForgotPassword = {
            // TODO: Wire optional password recovery when RF12 is enabled.
        },
    )
}

@Composable
fun SignInScreen(
    state: SignInUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSignIn: () -> Unit,
    onRegister: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.large, vertical = spacing.extraLarge),
        verticalArrangement = Arrangement.spacedBy(spacing.large),
    ) {
        Text(text = stringResource(id = R.string.app_name))
        ComprartirOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.credentials.email,
            onValueChange = onEmailChanged,
            label = { Text(text = stringResource(id = R.string.label_email)) },
            singleLine = true,
        )
        ComprartirOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.credentials.password,
            onValueChange = onPasswordChanged,
            label = { Text(text = stringResource(id = R.string.label_password)) },
            singleLine = true,
            isError = state.errorMessage != null,
            supportingText = state.errorMessage?.let { message ->
                {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
        )
        Button(onClick = onSignIn, enabled = !state.isLoading) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Text(text = stringResource(id = R.string.action_sign_in))
            }
        }
        Button(onClick = onRegister) {
            Text(text = stringResource(id = R.string.action_register))
        }
        Button(onClick = onForgotPassword) {
            Text(text = stringResource(id = R.string.action_forgot_password))
        }
    }
}
