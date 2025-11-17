package com.comprartir.mobile.feature.auth.verify

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun VerifyRoute(
    onNavigateToLogin: () -> Unit,
    onVerifySuccess: () -> Unit,
    windowSizeClass: androidx.compose.material3.windowsizeclass.WindowSizeClass? = null,
    viewModel: VerifyViewModel = hiltViewModel(),
) {
    println("VerifyRoute: Composing VerifyRoute")
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    println("VerifyRoute: State email = ${state.email}")

    LaunchedEffect(state.isVerified) {
        if (state.isVerified) {
            onVerifySuccess()
        }
    }

    VerifyScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBackToLogin = onNavigateToLogin,
        windowSizeClass = windowSizeClass,
    )
}
