package com.comprartir.mobile.core.util

data class FeatureFlags(
    val rf12PasswordRecovery: Boolean = false,
    val rf13History: Boolean = false,
    val rf14RecurringLists: Boolean = false,
    val rf15PantryProducts: Boolean = false,
    val rnf7Barcode: Boolean = false,
    val rnf8VoiceCommands: Boolean = false,
    val rnf9PhotoCapture: Boolean = false,
) {
    companion object {
        val Disabled = FeatureFlags()
    }
}
