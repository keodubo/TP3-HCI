package com.comprartir.mobile.core.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class AuthTokenRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val authTokenKey = stringPreferencesKey("auth_token")

    val token: Flow<String?> = dataStore.data
        .map { prefs -> prefs[authTokenKey] }
        .distinctUntilChanged()

    suspend fun currentToken(): String? = token.first()

    suspend fun updateToken(token: String) {
        dataStore.edit { prefs ->
            prefs[authTokenKey] = token
        }
    }

    suspend fun clearToken() {
        dataStore.edit { prefs ->
            prefs.remove(authTokenKey)
        }
    }
}
