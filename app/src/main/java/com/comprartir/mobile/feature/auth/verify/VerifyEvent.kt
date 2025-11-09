package com.comprartir.mobile.feature.auth.verify

sealed interface VerifyEvent {
    data class CodeChanged(val code: String) : VerifyEvent
    data object Submit : VerifyEvent
    data object ResendCode : VerifyEvent
    data object DismissError : VerifyEvent
}
