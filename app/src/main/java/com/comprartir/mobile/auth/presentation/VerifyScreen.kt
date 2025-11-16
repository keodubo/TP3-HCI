package com.comprartir.mobile.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun VerifyScreen(
    viewModel: VerifyViewModel = hiltViewModel(),
    onVerificationSuccess: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isVerificationSuccess) {
        if (uiState.isVerificationSuccess) {
            println("VerifyScreen: Verification successful, navigating to SignIn")
            onVerificationSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Verify your account",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        OutlinedTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChange,
            singleLine = true,
            isError = uiState.error != null,
            placeholder = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.code,
            onValueChange = viewModel::onCodeChange,
            singleLine = true,
            isError = uiState.error != null,
            placeholder = { Text("Verification Code") },
            modifier = Modifier.fillMaxWidth()
        )
        uiState.error?.let {
            Text(
                text = it,
                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
        }
        Button(
            onClick = viewModel::verify,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Verify")
        }
    }
}
