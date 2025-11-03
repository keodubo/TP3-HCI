package com.comprartir.mobile.shared.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.core.data.datastore.ThemeMode
import com.comprartir.mobile.core.designsystem.LocalSpacing
import com.comprartir.mobile.R

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

@OptIn(ExperimentalMaterial3Api::class)
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
                    modifier = Modifier.then(if (index > 0) Modifier.width(8.dp) else Modifier),
                ) {
                    val labelText = when (mode) {
                        ThemeMode.LIGHT -> stringResource(id = R.string.settings_theme_light)
                        ThemeMode.DARK -> stringResource(id = R.string.settings_theme_dark)
                        else -> stringResource(id = R.string.settings_theme_system)
                    }
                    Text(text = labelText)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        Text(text = stringResource(id = R.string.settings_language))
        Row {
            listOf(null, "es", "en").forEachIndexed { index, locale ->
                val selected = state.preferences.languageOverride == locale
                Button(
                    onClick = { onLanguageChanged(locale) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.then(if (index > 0) Modifier.width(8.dp) else Modifier),
                ) {
                    val labelText = when (locale) {
                        null -> stringResource(id = R.string.settings_language_system)
                        "es" -> stringResource(id = R.string.settings_language_es)
                        "en" -> stringResource(id = R.string.settings_language_en)
                        else -> locale
                    }
                    Text(text = labelText ?: stringResource(id = R.string.settings_language_system))
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        Text(text = stringResource(id = R.string.settings_notifications))
        Switch(
            checked = state.preferences.notificationsEnabled,
            onCheckedChange = onNotificationsChanged,
        )
        // TODO: Add accent color pickers and density controls to match web personalization options.
    }
}
