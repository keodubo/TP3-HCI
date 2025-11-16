package com.comprartir.mobile

import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import android.view.ContextThemeWrapper
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.core.view.WindowCompat
import com.comprartir.mobile.core.data.datastore.UserPreferencesDataSource
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var userPreferencesDataSource: UserPreferencesDataSource

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val initialPreferences = runBlocking {
            userPreferencesDataSource.userPreferences().first()
        }

        setContent {
            val preferences by userPreferencesDataSource.userPreferences()
                .collectAsState(initial = initialPreferences)
            val localizedConfiguration = remember(preferences.languageOverride) {
                val baseConfig = Configuration(resources.configuration)
                val override = preferences.languageOverride
                if (!override.isNullOrBlank()) {
                    val compat = LocaleListCompat.forLanguageTags(override)
                    val localeList = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        LocaleList.forLanguageTags(override)
                    } else {
                        @Suppress("DEPRECATION")
                        LocaleList(compat.get(0))
                    }
                    baseConfig.setLocales(localeList)
                } else {
                    // Si no hay override, usamos la configuración del dispositivo.
                    baseConfig.setLocales(resources.configuration.locales)
                }
                baseConfig
            }
            val localizedContext = remember(localizedConfiguration) {
                ContextThemeWrapper(this@MainActivity, theme).apply {
                    applyOverrideConfiguration(localizedConfiguration)
                }
            }

            CompositionLocalProvider(
                LocalConfiguration provides localizedConfiguration,
                LocalContext provides localizedContext,
            ) {
                val windowSizeClass = calculateWindowSizeClass(this@MainActivity)
                ComprartirApp(
                    windowSizeClass = windowSizeClass,
                    appTheme = preferences.appTheme,
                )
            }
        }
    }
}
