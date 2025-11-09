package com.comprartir.mobile.feature.auth.register

import android.content.Context
import android.util.Patterns
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
import retrofit2.HttpException

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private var nameTouched = false
    private var lastNameTouched = false
    private var emailTouched = false
    private var passwordTouched = false
    private var confirmPasswordTouched = false

    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.NameChanged -> onNameChanged(event.name)
            is RegisterEvent.LastNameChanged -> onLastNameChanged(event.lastName)
            is RegisterEvent.EmailChanged -> onEmailChanged(event.email)
            is RegisterEvent.PasswordChanged -> onPasswordChanged(event.password)
            is RegisterEvent.ConfirmPasswordChanged -> onConfirmPasswordChanged(event.password)
            RegisterEvent.Submit -> submit()
            RegisterEvent.DismissSuccess -> dismissSuccess()
            RegisterEvent.DismissError -> dismissError()
        }
    }

    private fun onNameChanged(name: String) {
        nameTouched = true
        dismissSuccess()
        _uiState.update { it.copy(name = name) }
        validateFields()
    }

    private fun onLastNameChanged(lastName: String) {
        lastNameTouched = true
        dismissSuccess()
        _uiState.update { it.copy(lastName = lastName) }
        validateFields()
    }

    private fun onEmailChanged(email: String) {
        emailTouched = true
        dismissSuccess()
        _uiState.update { it.copy(email = email) }
        validateFields()
    }

    private fun onPasswordChanged(password: String) {
        passwordTouched = true
        dismissSuccess()
        _uiState.update { it.copy(password = password) }
        validateFields()
    }

    private fun onConfirmPasswordChanged(confirmPassword: String) {
        confirmPasswordTouched = true
        dismissSuccess()
        _uiState.update { it.copy(confirmPassword = confirmPassword) }
        validateFields()
    }

    private fun submit() {
        validateFields(forceAllErrors = true)
        val current = _uiState.value
        if (!current.isSubmitEnabled || current.isLoading) return

        val email = current.email.trim()
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isSubmitEnabled = false,
                    isSuccess = false,
                    successMessage = null,
                    errorMessage = null,
                )
            }
            val result = runCatching {
                authRepository.register(
                    email = email,
                    password = current.password,
                    name = current.name.trim(),
                    surname = current.lastName.trim(),
                )
            }
            result.onSuccess {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isSuccess = true,
                        successMessage = context.getString(R.string.register_success_message),
                        errorMessage = null,
                    )
                }
            }.onFailure { error ->
                error.printStackTrace()
                val errorMsg = when {
                    error is HttpException && error.code() == 409 -> context.getString(R.string.error_email_already_registered)
                    error.message?.contains("409") == true || error.message?.contains("already exists", ignoreCase = true) == true ->
                        context.getString(R.string.error_email_already_registered)
                    else -> context.getString(R.string.registration_failed)
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
            }
            validateFields()
        }
    }

    private fun validateFields(forceAllErrors: Boolean = false) {
        val current = _uiState.value
        val validatedName = current.name.trim()
        val validatedLastName = current.lastName.trim()
        val validatedEmail = current.email.trim()
        val showNameError = forceAllErrors || nameTouched
        val showLastNameError = forceAllErrors || lastNameTouched
        val showEmailError = forceAllErrors || emailTouched
        val showPasswordError = forceAllErrors || passwordTouched
        val showConfirmPasswordError = forceAllErrors || confirmPasswordTouched

        val nameError = if (showNameError && validatedName.isEmpty()) {
            context.getString(R.string.error_name_required)
        } else {
            null
        }
        val lastNameError = if (showLastNameError && validatedLastName.isEmpty()) {
            context.getString(R.string.error_last_name_required)
        } else {
            null
        }
        val emailError = if (showEmailError && !Patterns.EMAIL_ADDRESS.matcher(validatedEmail).matches()) {
            context.getString(R.string.error_email_invalid)
        } else {
            null
        }
        val passwordError = if (showPasswordError && current.password.length < MIN_PASSWORD_LENGTH) {
            context.getString(R.string.error_password_too_short)
        } else {
            null
        }
        val passwordsMatch = current.confirmPassword == current.password && current.confirmPassword.isNotEmpty()
        val confirmPasswordError = if (showConfirmPasswordError && !passwordsMatch) {
            context.getString(R.string.error_passwords_dont_match)
        } else {
            null
        }
        val isSubmitEnabled = validatedName.isNotEmpty() &&
            validatedLastName.isNotEmpty() &&
            Patterns.EMAIL_ADDRESS.matcher(validatedEmail).matches() &&
            current.password.length >= MIN_PASSWORD_LENGTH &&
            passwordsMatch

        _uiState.update {
            it.copy(
                nameError = nameError,
                lastNameError = lastNameError,
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError,
                isSubmitEnabled = isSubmitEnabled && !current.isLoading,
            )
        }
    }

    private fun dismissSuccess() {
        val current = _uiState.value
        if (!current.isSuccess && current.successMessage == null) return
        _uiState.update {
            it.copy(isSuccess = false, successMessage = null)
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
    }
}
