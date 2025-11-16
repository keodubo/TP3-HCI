package com.comprartir.mobile.core.util

/**
 * Centralizes the wiring for optional hardware integrations (RNF7-RNF9).
 *
 * The real implementations will replace the current simulated handlers, so we keep a small
 * publish/subscribe model that UI layers can hook into without changing public APIs.
 */
object IntegrationPlaceholders {

    interface Listener {
        fun onBarcodeRequested() {}
        fun onVoiceRequested() {}
        fun onPhotoRequested() {}
    }

    private val listeners = mutableSetOf<Listener>()

    fun registerListener(listener: Listener) {
        listeners += listener
    }

    fun unregisterListener(listener: Listener) {
        listeners -= listener
    }

    fun launchBarcodeScanner() {
        if (listeners.isEmpty()) return
        listeners.toList().forEach { it.onBarcodeRequested() }
    }

    fun startVoiceCommandSession() {
        if (listeners.isEmpty()) return
        listeners.toList().forEach { it.onVoiceRequested() }
    }

    fun captureProductPhoto() {
        if (listeners.isEmpty()) return
        listeners.toList().forEach { it.onPhotoRequested() }
    }
}
