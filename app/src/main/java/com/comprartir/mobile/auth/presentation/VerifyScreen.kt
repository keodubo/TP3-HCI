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
fun VerifyRoute(
    onNavigate: (NavigationIntent) -> Unit,
    viewModel: VerifyViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    VerifyScreen(
        state = state,
        onCodeChanged = viewModel::onCodeChanged,
        onVerify = {
            viewModel.verify(
                onSuccess = { onNavigate(NavigationIntent(AppDestination.Dashboard)) },
                onError = { /* TODO: Show error */ },
            )
        },
        onResendCode = { /* TODO: Trigger resend */ },
    )
}

@Composable
fun VerifyScreen(
    state: com.comprartir.mobile.auth.domain.VerificationState,
    onCodeChanged: (String) -> Unit,
    onVerify: () -> Unit,
    onResendCode: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.large, vertical = spacing.extraLarge),
        verticalArrangement = Arrangement.spacedBy(spacing.large),
    ) {
        Text(text = stringResource(id = R.string.headline_verify))
        ComprartirOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.code,
            onValueChange = onCodeChanged,
            label = { Text(stringResource(id = R.string.label_verification_code)) },
            singleLine = true,
        )
        Button(onClick = onVerify, enabled = !state.isLoading) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Text(text = stringResource(id = R.string.action_verify))
            }
        }
        Button(onClick = onResendCode) {
            Text(text = stringResource(id = R.string.action_resend_code))
        }
    }
}
