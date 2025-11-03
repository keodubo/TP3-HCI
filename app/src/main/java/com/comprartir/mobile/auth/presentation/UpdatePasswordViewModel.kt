package com.comprartir.mobile.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.R
import com.comprartir.mobile.auth.data.AuthRepository
import com.comprartir.mobile.auth.domain.PasswordUpdateState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class UpdatePasswordViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PasswordUpdateState())
    val state: StateFlow<PasswordUpdateState> = _state.asStateFlow()

    fun onCurrentPasswordChanged(value: String) {
        _state.value = _state.value.copy(currentPassword = value)
    }

    fun onNewPasswordChanged(value: String) {
        _state.value = _state.value.copy(newPassword = value)
    }

    fun onConfirmPasswordChanged(value: String) {
        _state.value = _state.value.copy(confirmPassword = value)
    }

    fun updatePassword(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        viewModelScope.launch {
            val current = _state.value
            if (current.newPassword != current.confirmPassword) {
                _state.value = current.copy(errorMessageRes = R.string.passwords_do_not_match)
                return@launch
            }
            _state.value = current.copy(isLoading = true, errorMessageRes = null)
            runCatching {
                repository.updatePassword(current.currentPassword, current.newPassword)
            }.onSuccess {
                _state.value = current.copy(isLoading = false, errorMessageRes = null)
                onSuccess()
            }.onFailure { throwable ->
                _state.value = current.copy(isLoading = false)
                onError(throwable)
            }
        }
    }
}
