package com.comprartir.mobile.auth.domain

data class Credentials(
    val email: String = "",
    val password: String = "",
)

data class RegistrationState(
    val credentials: Credentials = Credentials(),
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessageRes: Int? = null,
)

data class VerificationState(
    val code: String = "",
    val isLoading: Boolean = false,
    val errorMessageRes: Int? = null,
)

data class PasswordUpdateState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessageRes: Int? = null,
)

data class ForgotPasswordState(
    val email: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessageRes: Int? = null,
)

data class ResetPasswordState(
    val email: String = "",
    val code: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessageRes: Int? = null,
)

