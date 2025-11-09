package com.comprartir.mobile.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.R
import com.comprartir.mobile.auth.data.AuthRepository
import com.comprartir.mobile.auth.domain.RegistrationState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RegistrationState())
    val state: StateFlow<RegistrationState> = _state.asStateFlow()

    fun onEmailChanged(email: String) {
        val current = _state.value
        _state.value = current.copy(credentials = current.credentials.copy(email = email))
    }

    fun onPasswordChanged(password: String) {
        val current = _state.value
        _state.value = current.copy(credentials = current.credentials.copy(password = password))
    }

    fun onConfirmPasswordChanged(password: String) {
        _state.value = _state.value.copy(confirmPassword = password)
    }

    fun register(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        viewModelScope.launch {
            val current = _state.value
            if (current.credentials.password != current.confirmPassword) {
                _state.value = current.copy(errorMessageRes = R.string.passwords_do_not_match)
                return@launch
            }
            _state.value = current.copy(isLoading = true, errorMessageRes = null)
            println("RegisterViewModel: Starting registration for ${current.credentials.email}")
            
            // Extract name from email as fallback for old registration screen
            val emailPrefix = current.credentials.email.substringBefore("@")
            val name = emailPrefix.substringBefore(".").replaceFirstChar { it.uppercase() }
            val surname = emailPrefix.substringAfter(".", name).replaceFirstChar { it.uppercase() }
            
            runCatching {
                repository.register(
                    current.credentials.email,
                    current.credentials.password,
                    name,
                    surname
                )
            }.onSuccess {
                println("RegisterViewModel: Registration successful, calling onSuccess()")
                _state.value = _state.value.copy(isLoading = false, errorMessageRes = null)
                onSuccess()
            }.onFailure { throwable ->
                println("RegisterViewModel: Registration failed with error: ${throwable.message}")
                throwable.printStackTrace()
                _state.value = _state.value.copy(isLoading = false)
                onError(throwable)
            }
        }
    }
}
