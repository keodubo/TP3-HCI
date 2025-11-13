package com.comprartir.mobile.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.R
import com.comprartir.mobile.auth.data.AuthRepository
import com.comprartir.mobile.auth.domain.ForgotPasswordState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    fun onEmailChanged(value: String) {
        _state.value = _state.value.copy(email = value, errorMessageRes = null, isSuccess = false)
    }

    fun sendRecoveryCode(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val current = _state.value
            if (current.email.isBlank()) {
                _state.value = current.copy(errorMessageRes = R.string.error_email_required)
                return@launch
            }

            _state.value = current.copy(isLoading = true, errorMessageRes = null, isSuccess = false)
            runCatching {
                repository.sendPasswordRecoveryCode(current.email)
            }.onSuccess {
                _state.value = current.copy(isLoading = false, isSuccess = true, errorMessageRes = null)
                // Navigate to reset password screen after a brief delay
                kotlinx.coroutines.delay(500)
                onSuccess(current.email)
            }.onFailure { throwable ->
                throwable.printStackTrace() // Log the error for debugging
                val errorMessage = when {
                    throwable.message?.contains("404", ignoreCase = true) == true -> R.string.error_user_not_found
                    throwable.message?.contains("400", ignoreCase = true) == true -> R.string.error_email_invalid
                    throwable.message?.contains("network", ignoreCase = true) == true -> R.string.error_network
                    throwable is java.net.UnknownHostException -> R.string.error_network
                    throwable is java.net.SocketTimeoutException -> R.string.error_network
                    else -> {
                        println("ForgotPasswordViewModel: Error - ${throwable.message}")
                        R.string.error_generic
                    }
                }
                _state.value = current.copy(isLoading = false, errorMessageRes = errorMessage, isSuccess = false)
            }
        }
    }
}

