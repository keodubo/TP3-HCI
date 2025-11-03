package com.comprartir.mobile.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.auth.data.AuthRepository
import com.comprartir.mobile.auth.domain.VerificationState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class VerifyViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(VerificationState())
    val state: StateFlow<VerificationState> = _state.asStateFlow()

    fun onCodeChanged(code: String) {
        _state.value = _state.value.copy(code = code.take(6))
    }

    fun verify(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        viewModelScope.launch {
            val current = _state.value
            _state.value = current.copy(isLoading = true, errorMessageRes = null)
            runCatching {
                repository.verify(current.code)
            }.onSuccess {
                _state.value = current.copy(isLoading = false)
                onSuccess()
            }.onFailure { throwable ->
                // Map throwable to a resource id when possible. For now set a generic null
                // and surface the throwable via the callback so the UI can decide.
                _state.value = current.copy(isLoading = false, errorMessageRes = null)
                onError(throwable)
            }
        }
    }
}
