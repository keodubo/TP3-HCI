package com.comprartir.mobile.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.auth.data.AuthRepository
import com.comprartir.mobile.auth.domain.Credentials
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SignInUiState())
    val state: StateFlow<SignInUiState> = _state.asStateFlow()

    fun onEmailChanged(email: String) {
        _state.value = _state.value.copy(credentials = _state.value.credentials.copy(email = email))
    }

    fun onPasswordChanged(password: String) {
        _state.value = _state.value.copy(credentials = _state.value.credentials.copy(password = password))
    }

    fun signIn(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                repository.signIn(_state.value.credentials.email, _state.value.credentials.password)
            }.onSuccess {
                _state.value = _state.value.copy(isLoading = false)
                onSuccess()
            }.onFailure { throwable ->
                _state.value = _state.value.copy(isLoading = false, errorMessage = throwable.message)
                onError(throwable)
            }
        }
    }
}

data class SignInUiState(
    val credentials: Credentials = Credentials(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
