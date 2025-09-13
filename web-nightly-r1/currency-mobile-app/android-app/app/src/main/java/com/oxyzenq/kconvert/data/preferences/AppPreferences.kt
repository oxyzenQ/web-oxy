/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

// Extension property to create DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kconvert_preferences")

/**
 * DataStore preferences manager for Kconvert app settings
 */
@Singleton
class AppPreferences @Inject constructor(
    private val context: Context
) {
    companion object {
        private val AUTO_UPDATE_ON_LAUNCH = booleanPreferencesKey("auto_update_on_launch")
        private val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        private val LAST_KNOWN_VERSION = androidx.datastore.preferences.core.stringPreferencesKey("last_known_version")
    }
    
    /**
     * Get auto update on launch setting as Flow
     */
    val autoUpdateOnLaunch: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_UPDATE_ON_LAUNCH] ?: false // Default is OFF
    }

    /**
     * Get haptics enabled setting as Flow
     */
    val hapticsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HAPTICS_ENABLED] ?: true // Default is ON
    }
    
    /**
     * Set auto update on launch setting
     */
    suspend fun setAutoUpdateOnLaunch(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_UPDATE_ON_LAUNCH] = enabled
        }
    }

    /**
     * Set haptics enabled setting
     */
    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAPTICS_ENABLED] = enabled
        }
    }
    
    /**
     * Get current auto update setting (suspend function for one-time read)
     */
    suspend fun getAutoUpdateOnLaunch(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[AUTO_UPDATE_ON_LAUNCH] ?: false
        }.first()
    }

    /**
     * One-time read of current haptics setting
     */
    suspend fun getHapticsEnabled(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[HAPTICS_ENABLED] ?: true
        }.first()
    }

    /**
     * Get last known app version
     */
    suspend fun getLastKnownVersion(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[LAST_KNOWN_VERSION]
        }.first()
    }

    /**
     * Set last known app version
     */
    suspend fun setLastKnownVersion(version: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_KNOWN_VERSION] = version
        }
    }
}
