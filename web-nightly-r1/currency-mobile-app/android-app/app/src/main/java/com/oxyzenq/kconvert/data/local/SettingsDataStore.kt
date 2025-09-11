package com.oxyzenq.kconvert.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val DATASTORE_NAME = "kconvert_settings"

val Context.settingsDataStore by preferencesDataStore(name = DATASTORE_NAME)

object SettingsKeys {
    val FULL_SCREEN: Preferences.Key<Boolean> = booleanPreferencesKey("full_screen")
    val HAPTICS_ENABLED: Preferences.Key<Boolean> = booleanPreferencesKey("haptics_enabled")
    val AUTO_UPDATE: Preferences.Key<Boolean> = booleanPreferencesKey("auto_update")
    val DARK_MODE: Preferences.Key<Boolean> = booleanPreferencesKey("dark_mode")
    val DARK_LEVEL: Preferences.Key<Int> = intPreferencesKey("dark_level")
    val CACHE_SIZE: Preferences.Key<Long> = longPreferencesKey("cache_size")
    val CACHE_LAST_SCAN: Preferences.Key<String> = stringPreferencesKey("cache_last_scan")
    val NAVBAR_AUTO_HIDE: Preferences.Key<Boolean> = booleanPreferencesKey("navbar_auto_hide")
    val METEOR_ANIMATION: Preferences.Key<Boolean> = booleanPreferencesKey("meteor_animation")
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

    val cacheSizeFlow: Flow<Long> =
        context.settingsDataStore.data.map { prefs ->
            prefs[SettingsKeys.CACHE_SIZE] ?: 0L
        }

    val darkLevelFlow: Flow<Int> =
        context.settingsDataStore.data.map { prefs ->
            prefs[SettingsKeys.DARK_LEVEL] ?: 0
        }

    val cacheLastScanFlow: Flow<String> =
        context.settingsDataStore.data.map { prefs ->
            prefs[SettingsKeys.CACHE_LAST_SCAN] ?: ""
        }

    val navbarAutoHideFlow: Flow<Boolean> =
        context.settingsDataStore.data.map { prefs ->
            prefs[SettingsKeys.NAVBAR_AUTO_HIDE] ?: true
        }

    val meteorAnimationFlow: Flow<Boolean> =
        context.settingsDataStore.data.map { prefs ->
            prefs[SettingsKeys.METEOR_ANIMATION] ?: true
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

    suspend fun setCacheSize(size: Long) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.CACHE_SIZE] = size
        }
    }

    suspend fun setDarkLevel(level: Int) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.DARK_LEVEL] = level
        }
    }

    suspend fun setCacheLastScan(timestamp: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.CACHE_LAST_SCAN] = timestamp
        }
    }

    suspend fun setNavbarAutoHide(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.NAVBAR_AUTO_HIDE] = enabled
        }
    }

    suspend fun setMeteorAnimation(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.METEOR_ANIMATION] = enabled
        }
    }

}
