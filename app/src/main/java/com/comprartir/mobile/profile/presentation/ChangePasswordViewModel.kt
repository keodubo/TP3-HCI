package com.comprartir.mobile.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.R
import com.comprartir.mobile.auth.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentPasswordError: Int? = null,
    val newPasswordError: Int? = null,
    val confirmPasswordError: Int? = null,
    val isLoading: Boolean = false,
    val isPasswordChanged: Boolean = false,
    val generalError: Int? = null,
    val showCurrentPassword: Boolean = false,
    val showNewPassword: Boolean = false,
    val showConfirmPassword: Boolean = false,
)

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ChangePasswordUiState())
    val state: StateFlow<ChangePasswordUiState> = _state.asStateFlow()

    fun onCurrentPasswordChanged(password: String) {
        _state.update { 
            it.copy(
                currentPassword = password,
                currentPasswordError = null,
                generalError = null,
            )
        }
    }

    fun onNewPasswordChanged(password: String) {
        _state.update { 
            it.copy(
                newPassword = password,
                newPasswordError = null,
                generalError = null,
            )
        }
    }

    fun onConfirmPasswordChanged(password: String) {
        _state.update { 
            it.copy(
                confirmPassword = password,
                confirmPasswordError = null,
                generalError = null,
            )
        }
    }

    fun onToggleCurrentPasswordVisibility() {
        _state.update { it.copy(showCurrentPassword = !it.showCurrentPassword) }
    }

    fun onToggleNewPasswordVisibility() {
        _state.update { it.copy(showNewPassword = !it.showNewPassword) }
    }

    fun onToggleConfirmPasswordVisibility() {
        _state.update { it.copy(showConfirmPassword = !it.showConfirmPassword) }
    }

    fun onSavePassword() {
        if (!validateInputs()) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, generalError = null) }
            
            try {
                authRepository.updatePassword(
                    currentPassword = _state.value.currentPassword,
                    newPassword = _state.value.newPassword,
                )
                _state.update { 
                    it.copy(
                        isLoading = false,
                        isPasswordChanged = true,
                    )
                }
            } catch (http: HttpException) {
                val errorMessage = when (http.code()) {
                    400 -> R.string.change_password_error_incorrect // Bad request - likely wrong current password
                    401 -> R.string.change_password_error_incorrect // Unauthorized
                    else -> R.string.change_password_error_generic
                }
                _state.update { 
                    it.copy(
                        isLoading = false,
                        generalError = errorMessage,
                    )
                }
            } catch (e: IllegalArgumentException) {
                // Handle validation errors from repository
                _state.update { 
                    it.copy(
                        isLoading = false,
                        generalError = R.string.change_password_error_generic,
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        generalError = R.string.change_password_error_generic,
                    )
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        val currentState = _state.value
        var hasErrors = false
        
        var currentPasswordError: Int? = null
        var newPasswordError: Int? = null
        var confirmPasswordError: Int? = null

        // Validate current password
        if (currentState.currentPassword.isBlank()) {
            currentPasswordError = R.string.change_password_error_current_empty
            hasErrors = true
        }

        // Validate new password
        if (currentState.newPassword.isBlank()) {
            newPasswordError = R.string.change_password_error_new_empty
            hasErrors = true
        } else if (currentState.newPassword.length < 6) {
            newPasswordError = R.string.change_password_error_new_too_short
            hasErrors = true
        }

        // Validate confirm password
        if (currentState.confirmPassword.isBlank()) {
            confirmPasswordError = R.string.change_password_error_confirm_empty
            hasErrors = true
        } else if (currentState.newPassword != currentState.confirmPassword) {
            confirmPasswordError = R.string.change_password_error_mismatch
            hasErrors = true
        }

        if (hasErrors) {
            _state.update { 
                it.copy(
                    currentPasswordError = currentPasswordError,
                    newPasswordError = newPasswordError,
                    confirmPasswordError = confirmPasswordError,
                )
            }
        }

        return !hasErrors
    }

    fun onErrorDismissed() {
        _state.update { it.copy(generalError = null) }
    }
}
