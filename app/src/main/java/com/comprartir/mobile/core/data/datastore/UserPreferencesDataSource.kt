package com.comprartir.mobile.core.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val languageOverrideKey = stringPreferencesKey("language_override")
    private val notificationsEnabledKey = booleanPreferencesKey("notifications_enabled")

    fun userPreferences(): Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            themeMode = prefs[themeModeKey] ?: ThemeMode.SYSTEM,
            languageOverride = prefs[languageOverrideKey],
            notificationsEnabled = prefs[notificationsEnabledKey] ?: true,
        )
    }

    suspend fun updateTheme(themeMode: String) {
        dataStore.edit { prefs ->
            prefs[themeModeKey] = themeMode
        }
    }

    suspend fun updateLanguage(languageTag: String?) {
        dataStore.edit { prefs ->
            if (languageTag == null) {
                prefs.remove(languageOverrideKey)
            } else {
                prefs[languageOverrideKey] = languageTag
            }
        }
    }

    suspend fun updateNotifications(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[notificationsEnabledKey] = enabled
        }
    }
}

data class UserPreferences(
    val themeMode: String,
    val languageOverride: String?,
    val notificationsEnabled: Boolean,
)

object ThemeMode {
    const val LIGHT = "light"
    const val DARK = "dark"
    const val SYSTEM = "system"
}
