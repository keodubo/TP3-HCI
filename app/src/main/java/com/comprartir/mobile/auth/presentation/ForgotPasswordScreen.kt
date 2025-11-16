package com.comprartir.mobile.auth.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.R
import com.comprartir.mobile.auth.domain.ForgotPasswordState
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.designsystem.ComprartirOutlinedTextField

@Composable
fun ForgotPasswordRoute(
    onNavigateToLogin: () -> Unit,
    onNavigateToResetPassword: (String) -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ForgotPasswordScreen(
        state = state,
        onEmailChanged = viewModel::onEmailChanged,
        onSendCode = {
            viewModel.sendRecoveryCode(
                onSuccess = { email ->
                    onNavigateToResetPassword(email)
                }
            )
        },
        onNavigateToLogin = onNavigateToLogin,
    )
}

@Composable
fun ForgotPasswordScreen(
    state: ForgotPasswordState,
    onEmailChanged: (String) -> Unit,
    onSendCode: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val spacing = LocalSpacing.current

    Box(modifier = Modifier.fillMaxSize()) {
        // Close button at top right
        IconButton(
            onClick = onNavigateToLogin,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(spacing.medium)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.action_close),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.extraLarge, vertical = spacing.extraLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(spacing.large))

            // Title
            Text(
                text = stringResource(R.string.forgot_password_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(spacing.small))

            // Description
            Text(
                text = stringResource(R.string.forgot_password_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(spacing.extraLarge))

            // Success message if code was sent
            if (state.isSuccess) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(spacing.small)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = stringResource(R.string.forgot_password_success),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(spacing.medium))
            }

            // Email input
            ComprartirOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.email,
                onValueChange = onEmailChanged,
                singleLine = true,
                placeholder = { Text(stringResource(R.string.label_email)) },
            )

            // Error message
            state.errorMessageRes?.let { errorRes ->
                Spacer(modifier = Modifier.height(spacing.small))
                Text(
                    text = stringResource(id = errorRes),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(modifier = Modifier.height(spacing.large))

            // Send button
            Button(
                onClick = onSendCode,
                enabled = !state.isLoading && state.email.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(text = stringResource(R.string.forgot_password_send_button))
                }
            }

            Spacer(modifier = Modifier.height(spacing.medium))

            // Back to login
            TextButton(onClick = onNavigateToLogin) {
                Text(text = stringResource(R.string.forgot_password_back_to_login))
            }
        }
    }
}
