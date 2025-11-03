package com.comprartir.mobile.profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.R
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.core.designsystem.ComprartirOutlinedTextField

@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(state.snackbarMessageRes) {
        state.snackbarMessageRes?.let { messageRes ->
            val message = context.getString(messageRes)
            snackbarHostState.showSnackbar(message = message)
            viewModel.onSnackbarConsumed()
        }
    }

    ProfileScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onNameChanged = viewModel::onNameChanged,
        onEmailChanged = viewModel::onEmailChanged,
        onSaveProfile = viewModel::saveProfile,
    )
}

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    snackbarHostState: SnackbarHostState,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onSaveProfile: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.large, vertical = spacing.large),
        verticalArrangement = Arrangement.spacedBy(spacing.large),
    ) {
        Text(text = stringResource(id = R.string.label_profile_heading))
        ComprartirOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.profile.name,
            onValueChange = onNameChanged,
            label = { Text(stringResource(id = R.string.label_name)) },
        )
        ComprartirOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.profile.email,
            onValueChange = onEmailChanged,
            label = { Text(stringResource(id = R.string.label_email)) },
        )
        Button(onClick = onSaveProfile) {
            Text(text = stringResource(id = R.string.label_save))
        }
        SnackbarHost(hostState = snackbarHostState)
    }
}
