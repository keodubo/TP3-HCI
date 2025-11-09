package com.comprartir.mobile.feature.auth.register

sealed interface RegisterEvent {
    data class NameChanged(val name: String) : RegisterEvent
    data class LastNameChanged(val lastName: String) : RegisterEvent
    data class EmailChanged(val email: String) : RegisterEvent
    data class PasswordChanged(val password: String) : RegisterEvent
    data class ConfirmPasswordChanged(val password: String) : RegisterEvent
    data object Submit : RegisterEvent
    data object DismissSuccess : RegisterEvent
    data object DismissError : RegisterEvent
}
