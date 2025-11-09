package com.comprartir.mobile.feature.auth.verify

data class VerifyUiState(
    val email: String = "",
    val code: String = "",
    val codeError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val countdownSeconds: Int = 0,
    val isResendEnabled: Boolean = true,
    val isVerified: Boolean = false,
)
