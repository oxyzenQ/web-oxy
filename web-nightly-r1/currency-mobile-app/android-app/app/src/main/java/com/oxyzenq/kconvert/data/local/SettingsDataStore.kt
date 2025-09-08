package com.oxyzenq.kconvert.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "kconvert_settings"

val Context.settingsDataStore by preferencesDataStore(name = DATASTORE_NAME)

object SettingsKeys {
    val FULL_SCREEN: Preferences.Key<Boolean> = booleanPreferencesKey("full_screen")
    val HAPTICS_ENABLED: Preferences.Key<Boolean> = booleanPreferencesKey("haptics_enabled")
    val AUTO_UPDATE: Preferences.Key<Boolean> = booleanPreferencesKey("auto_update")
    val DARK_MODE: Preferences.Key<Boolean> = booleanPreferencesKey("dark_mode")
}

class SettingsDataStore(private val context: Context) {
    val fullScreenFlow: Flow<Boolean> =
        context.settingsDataStore.data.map { prefs ->
            prefs[SettingsKeys.FULL_SCREEN] ?: true
        }

    val hapticsFlow: Flow<Boolean> =
        context.settingsDataStore.data.map { prefs ->
            prefs[SettingsKeys.HAPTICS_ENABLED] ?: true
        }

    val autoUpdateFlow: Flow<Boolean> =
        context.settingsDataStore.data.map { prefs ->
            prefs[SettingsKeys.AUTO_UPDATE] ?: true
        }

    val darkModeFlow: Flow<Boolean> =
        context.settingsDataStore.data.map { prefs ->
            prefs[SettingsKeys.DARK_MODE] ?: true
        }

    suspend fun setFullScreen(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.FULL_SCREEN] = enabled
        }
    }

    suspend fun setHaptics(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun setAutoUpdate(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.AUTO_UPDATE] = enabled
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.DARK_MODE] = enabled
        }
    }
}
