package com.comprartir.mobile.feature.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.auth.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> onEmailChanged(event.value)
            is LoginEvent.PasswordChanged -> onPasswordChanged(event.value)
            is LoginEvent.Submit -> submit(event.onSuccess)
        }
    }

    private fun onEmailChanged(email: String) {
        _uiState.update { current ->
            val sanitized = email.trim()
            current.copy(
                email = sanitized,
                errorMessage = null,
                isLoginEnabled = isLoginEnabled(sanitized, current.password),
            )
        }
    }

    private fun onPasswordChanged(password: String) {
        _uiState.update { current ->
            current.copy(
                password = password,
                errorMessage = null,
                isLoginEnabled = isLoginEnabled(current.email, password),
            )
        }
    }

    private fun submit(onSuccess: () -> Unit) {
        val current = _uiState.value
        if (!current.isLoginEnabled || current.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            println("LoginViewModel: Starting login for email: ${current.email}")
            runCatching {
                authRepository.signIn(current.email, current.password)
            }.onSuccess {
                println("LoginViewModel: Login successful! Calling onSuccess()")
                _uiState.update { state -> state.copy(isLoading = false) }
                onSuccess()
                println("LoginViewModel: onSuccess() called")
            }.onFailure { throwable ->
                println("LoginViewModel: Login failed with error: ${throwable.message}")
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Authentication failed",
                    )
                }
            }
        }
    }

    private fun isLoginEnabled(email: String, password: String): Boolean =
        email.isNotBlank() && password.isNotBlank()
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoginEnabled: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface LoginEvent {
    data class EmailChanged(val value: String) : LoginEvent
    data class PasswordChanged(val value: String) : LoginEvent
    data class Submit(val onSuccess: () -> Unit) : LoginEvent
}
