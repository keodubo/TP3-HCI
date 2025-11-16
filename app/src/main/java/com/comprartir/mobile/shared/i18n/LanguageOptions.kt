package com.comprartir.mobile.shared.i18n

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.comprartir.mobile.R
import com.comprartir.mobile.profile.domain.AppLanguage

data class LanguageOption(
    val language: AppLanguage,
    val label: String,
)

@Composable
fun rememberLanguageOptions(): List<LanguageOption> {
    return listOf(
        LanguageOption(AppLanguage.SYSTEM, stringResource(id = R.string.language_system)),
        LanguageOption(AppLanguage.SPANISH_AR, stringResource(id = R.string.language_spanish_ar)),
        LanguageOption(AppLanguage.ENGLISH_US, stringResource(id = R.string.language_english_us)),
    )
}
