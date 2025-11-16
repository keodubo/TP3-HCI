package com.comprartir.mobile.shared.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.comprartir.mobile.R
import com.comprartir.mobile.core.data.datastore.AppTheme
import com.comprartir.mobile.core.designsystem.LocalSpacing

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingsScreen(
        state = state,
        onThemeChanged = viewModel::onThemeSelected,
    )
}

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onThemeChanged: (AppTheme) -> Unit,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.large),
        verticalArrangement = Arrangement.spacedBy(spacing.large),
    ) {
        Text(
            text = stringResource(id = R.string.settings_theme),
            style = MaterialTheme.typography.titleMedium,
        )
        ThemeToggleRow(
            selectedTheme = state.appTheme,
            onThemeChanged = onThemeChanged,
        )
    }
}

@Composable
private fun ThemeToggleRow(
    selectedTheme: AppTheme,
    onThemeChanged: (AppTheme) -> Unit,
) {
    val spacing = LocalSpacing.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing.small),
        modifier = Modifier.fillMaxWidth(),
    ) {
        AppTheme.entries.forEach { theme ->
            val selected = selectedTheme == theme
            Button(
                onClick = { onThemeChanged(theme) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    contentColor = if (selected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                ),
                modifier = Modifier.weight(1f),
            ) {
                val labelRes = when (theme) {
                    AppTheme.LIGHT -> R.string.settings_theme_light
                    AppTheme.DARK -> R.string.settings_theme_dark
                }
                Text(text = stringResource(id = labelRes))
            }
        }
    }
}
