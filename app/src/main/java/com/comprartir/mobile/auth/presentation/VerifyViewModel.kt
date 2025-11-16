package com.comprartir.mobile.auth.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.R
import com.comprartir.mobile.auth.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VerifyUiState(
    val email: String = "",
    val code: String = "",
    val error: String? = null,
    val isVerificationSuccess: Boolean = false
)

@HiltViewModel
class VerifyViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VerifyUiState())
    val uiState: StateFlow<VerifyUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun onCodeChange(code: String) {
        _uiState.update { it.copy(code = code, error = null) }
    }

    fun verify() {
        viewModelScope.launch {
            try {
                val currentState = uiState.value
                if (currentState.email.isBlank()) {
                    _uiState.update { it.copy(error = context.getString(R.string.error_email_required)) }
                    return@launch
                }
                if (currentState.code.isBlank()) {
                    _uiState.update { it.copy(error = context.getString(R.string.error_code_required)) }
                    return@launch
                }
                println("VerifyViewModel: Starting verification for ${currentState.email} with code: ${currentState.code}")
                authRepository.verify(currentState.email, currentState.code)
                println("VerifyViewModel: Verification successful")
                _uiState.update { it.copy(isVerificationSuccess = true) }
            } catch (e: Exception) {
                println("VerifyViewModel: Verification failed with error: ${e.message}")
                e.printStackTrace()
                _uiState.update { it.copy(error = e.message ?: context.getString(R.string.verify_error_generic)) }
            }
        }
    }
}
