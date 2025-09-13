package com.oxyzenq.kconvert.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsKeys {
    val FULL_SCREEN = booleanPreferencesKey("full_screen")
    val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
    val AUTO_UPDATE = booleanPreferencesKey("auto_update")
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val DARK_LEVEL = intPreferencesKey("dark_level")
    val CACHE_SIZE = longPreferencesKey("cache_size")
    val CACHE_LAST_SCAN = stringPreferencesKey("cache_last_scan")
    val NAVBAR_AUTO_HIDE = booleanPreferencesKey("navbar_auto_hide")
    val METEOR_ANIMATION = booleanPreferencesKey("meteor_animation")
    val AUTO_REMIND_ENABLED = booleanPreferencesKey("auto_remind_enabled")
    val LAST_UPDATE_CHECK = stringPreferencesKey("last_update_check")
}

@Singleton
class SettingsDataStore @Inject constructor(@ApplicationContext private val context: Context) {
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

    val meteorAnimationFlow = context.settingsDataStore.data.map { prefs ->
        prefs[SettingsKeys.METEOR_ANIMATION] ?: true
    }

    val autoRemindEnabled = context.settingsDataStore.data.map { prefs ->
        prefs[SettingsKeys.AUTO_REMIND_ENABLED] ?: true
    }

    val lastUpdateCheckFlow = context.settingsDataStore.data.map { prefs ->
        prefs[SettingsKeys.LAST_UPDATE_CHECK] ?: "Never"
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

    suspend fun setAutoRemindEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.AUTO_REMIND_ENABLED] = enabled
        }
    }

    suspend fun setLastUpdateCheck(timestamp: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.LAST_UPDATE_CHECK] = timestamp
        }
    }

}
