package com.comprartir.mobile.profile.domain

enum class AppLanguage(val code: String, val displayKey: String) {
    SYSTEM("system", "language_system"),
    SPANISH_AR("es-AR", "language_spanish_ar"),
    ENGLISH_US("en-US", "language_english_us");

    companion object {
        fun fromCode(code: String?): AppLanguage = when (code) {
            "es-AR", "es" -> SPANISH_AR
            "en-US", "en" -> ENGLISH_US
            else -> SYSTEM
        }
    }
}

enum class AppTheme(val code: String, val displayKey: String) {
    SYSTEM("system", "theme_system"),
    LIGHT("light", "theme_light"),
    DARK("dark", "theme_dark");

    companion object {
        fun fromCode(code: String?): AppTheme = when (code) {
            "light" -> LIGHT
            "dark" -> DARK
            else -> SYSTEM
        }
    }
}

enum class ProfileField {
    NAME,
    SURNAME
}

data class FieldError(
    val field: ProfileField,
    val messageRes: Int
)
