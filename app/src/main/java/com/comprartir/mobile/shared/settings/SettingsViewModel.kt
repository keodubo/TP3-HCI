package com.comprartir.mobile.shared.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.core.data.datastore.AppTheme
import com.comprartir.mobile.core.data.datastore.UserPreferencesDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesDataSource.userPreferences().collect { preferences ->
                _state.value = _state.value.copy(appTheme = preferences.appTheme)
            }
        }
    }

    fun onThemeSelected(theme: AppTheme) {
        viewModelScope.launch {
            userPreferencesDataSource.updateTheme(theme)
        }
    }
}

data class SettingsUiState(
    val appTheme: AppTheme = AppTheme.LIGHT,
)
