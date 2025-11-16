package com.comprartir.mobile.core.util

data class FeatureFlags(
    val rf12PasswordRecovery: Boolean = false,
    val rf15PantryProducts: Boolean = true,
    val rf13History: Boolean = true,          
    val rf14RecurringLists: Boolean = true, 
    val rnf7Barcode: Boolean = true,
    val rnf8VoiceCommands: Boolean = true,
    val rnf9PhotoCapture: Boolean = true,
) {
    companion object {
        val Disabled = FeatureFlags()
    }
}
