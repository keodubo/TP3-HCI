package com.comprartir.mobile.feature.auth.verify

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.R
import com.comprartir.mobile.auth.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class VerifyViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val email: String = savedStateHandle.get<String>("email").orEmpty()

    init {
        println("VerifyViewModel: Initialized with email: $email")
        if (email.isEmpty()) {
            println("VerifyViewModel: WARNING - Email is empty!")
        }
    }

    private val _uiState = MutableStateFlow(VerifyUiState(email = email))
    val uiState: StateFlow<VerifyUiState> = _uiState.asStateFlow()

    private var resendJob: Job? = null

    fun onEvent(event: VerifyEvent) {
        when (event) {
            is VerifyEvent.CodeChanged -> onCodeChanged(event.code)
            VerifyEvent.Submit -> submit()
            VerifyEvent.ResendCode -> resendCode()
            VerifyEvent.DismissError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun onCodeChanged(code: String) {
        _uiState.update {
            it.copy(
                code = code,
                codeError = null,
                errorMessage = null,
            )
        }
    }

    private fun submit() {
        val sanitized = _uiState.value.code.trim()
        if (sanitized.length != CODE_LENGTH) {
            _uiState.update {
                it.copy(codeError = context.getString(R.string.verify_error_code_length))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, codeError = null, errorMessage = null) }
            runCatching {
                authRepository.verify(email = email, code = sanitized)
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, isVerified = true) }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = context.getString(R.string.verify_error_invalid_code),
                    )
                }
            }
        }
    }

    private fun resendCode() {
        val state = _uiState.value
        if (!state.isResendEnabled) return

        _uiState.update {
            it.copy(
                isResendEnabled = false,
                countdownSeconds = RESEND_COOLDOWN_SECONDS,
                errorMessage = null,
            )
        }

        resendJob?.cancel()
        resendJob = viewModelScope.launch {
            var remaining = RESEND_COOLDOWN_SECONDS
            while (remaining > 0) {
                delay(1_000)
                remaining -= 1
                _uiState.update { current -> current.copy(countdownSeconds = remaining) }
            }
            _uiState.update { it.copy(isResendEnabled = true, countdownSeconds = 0) }
        }

        // TODO: authRepository.resendVerification(email)
    }

    override fun onCleared() {
        super.onCleared()
        resendJob?.cancel()
    }

    companion object {
        private const val CODE_LENGTH = 16
        private const val RESEND_COOLDOWN_SECONDS = 60
    }
}
