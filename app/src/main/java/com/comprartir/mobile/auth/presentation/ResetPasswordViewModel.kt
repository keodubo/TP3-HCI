package com.comprartir.mobile.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.R
import com.comprartir.mobile.auth.data.AuthRepository
import com.comprartir.mobile.auth.domain.ResetPasswordState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ResetPasswordState())
    val state: StateFlow<ResetPasswordState> = _state.asStateFlow()

    fun onEmailChanged(value: String) {
        _state.value = _state.value.copy(email = value)
    }

    fun onCodeChanged(value: String) {
        _state.value = _state.value.copy(code = value, errorMessageRes = null, isSuccess = false)
    }

    fun onPasswordChanged(value: String) {
        _state.value = _state.value.copy(newPassword = value, errorMessageRes = null, isSuccess = false)
    }

    fun onConfirmPasswordChanged(value: String) {
        _state.value = _state.value.copy(confirmPassword = value, errorMessageRes = null, isSuccess = false)
    }

    fun resetPassword(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val current = _state.value

            // Validation
            if (current.code.isBlank()) {
                _state.value = current.copy(errorMessageRes = R.string.error_code_required)
                return@launch
            }

            if (current.newPassword.isBlank()) {
                _state.value = current.copy(errorMessageRes = R.string.error_password_required)
                return@launch
            }

            if (current.newPassword.length < 6) {
                _state.value = current.copy(errorMessageRes = R.string.error_password_too_short)
                return@launch
            }

            if (current.newPassword != current.confirmPassword) {
                _state.value = current.copy(errorMessageRes = R.string.passwords_do_not_match)
                return@launch
            }

            _state.value = current.copy(isLoading = true, errorMessageRes = null, isSuccess = false)
            runCatching {
                repository.resetPassword(current.email, current.code, current.newPassword)
            }.onSuccess {
                println("ResetPasswordViewModel: Password reset successful")
                _state.value = current.copy(isLoading = false, isSuccess = true, errorMessageRes = null)
                // Navigate to login after a brief delay
                kotlinx.coroutines.delay(1500)
                onSuccess()
            }.onFailure { throwable ->
                throwable.printStackTrace()
                println("ResetPasswordViewModel: Reset failed - ${throwable.message}")
                val errorMessage = when {
                    throwable.message?.contains("400", ignoreCase = true) == true ||
                    throwable.message?.contains("invalid", ignoreCase = true) == true -> R.string.error_invalid_code
                    throwable.message?.contains("404", ignoreCase = true) == true -> R.string.error_user_not_found
                    throwable.message?.contains("network", ignoreCase = true) == true -> R.string.error_network
                    throwable is java.net.UnknownHostException -> R.string.error_network
                    throwable is java.net.SocketTimeoutException -> R.string.error_network
                    else -> {
                        println("ResetPasswordViewModel: Unhandled error type: ${throwable::class.simpleName}")
                        R.string.error_generic
                    }
                }
                _state.value = current.copy(isLoading = false, errorMessageRes = errorMessage, isSuccess = false)
            }
        }
    }
}

