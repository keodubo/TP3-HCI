package com.comprartir.mobile.shared.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.core.data.datastore.ThemeMode
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.R
import com.comprartir.mobile.profile.domain.AppLanguage
import com.comprartir.mobile.profile.presentation.ProfileDropdownField
import com.comprartir.mobile.shared.i18n.LanguageOption
import com.comprartir.mobile.shared.i18n.rememberLanguageOptions

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingsScreen(
        state = state,
        onThemeChanged = viewModel::onThemeSelected,
        onLanguageChanged = viewModel::onLanguageSelected,
        onNotificationsChanged = viewModel::onNotificationsToggled,
    )
}

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onThemeChanged: (String) -> Unit,
    onLanguageChanged: (String?) -> Unit,
    onNotificationsChanged: (Boolean) -> Unit,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.large),
    ) {
        Text(text = stringResource(id = R.string.settings_theme))
        // Fallback segmented control implemented with Buttons to keep compatibility
        Row {
            listOf(
                ThemeMode.LIGHT,
                ThemeMode.DARK,
                ThemeMode.SYSTEM,
            ).forEachIndexed { index, mode ->
                val selected = state.preferences.themeMode == mode
                Button(
                    onClick = { onThemeChanged(mode) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.padding(end = if (index < 2) spacing.small else 0.dp),
                ) {
                    val labelText = when (mode) {
                        ThemeMode.LIGHT -> stringResource(id = R.string.settings_theme_light)
                        ThemeMode.DARK -> stringResource(id = R.string.settings_theme_dark)
                        else -> stringResource(id = R.string.settings_theme_system)
                    }
                    Text(text = labelText)
                }
            }
        }
        Text(text = stringResource(id = R.string.settings_language))
        LanguagePreferenceDropdown(
            options = rememberLanguageOptions(),
            selectedOverride = state.preferences.languageOverride,
            onLanguageChanged = onLanguageChanged,
        )
        Text(text = stringResource(id = R.string.settings_notifications))
        Switch(
            checked = state.preferences.notificationsEnabled,
            onCheckedChange = onNotificationsChanged,
        )
        // TODO: Add accent color pickers and density controls to match web personalization options.
    }
}

@Composable
private fun LanguagePreferenceDropdown(
    options: List<LanguageOption>,
    selectedOverride: String?,
    onLanguageChanged: (String?) -> Unit,
) {
    val selectedLanguage = AppLanguage.fromCode(selectedOverride)
    val selectedLabel = options.firstOrNull { it.language == selectedLanguage }?.label
        ?: options.first().label
    ProfileDropdownField(
        value = selectedLabel,
        onValueChange = { code ->
            val override = if (code == AppLanguage.SYSTEM.code) null else code
            onLanguageChanged(override)
        },
        label = stringResource(id = R.string.settings_language),
        options = options.map { it.language.code to it.label },
        enabled = true,
    )
}
