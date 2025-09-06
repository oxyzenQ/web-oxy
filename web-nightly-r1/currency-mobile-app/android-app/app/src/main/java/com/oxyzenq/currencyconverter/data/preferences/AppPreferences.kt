/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.currencyconverter.data.preferences

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
    }
    
    /**
     * Get auto update on launch setting as Flow
     */
    val autoUpdateOnLaunch: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_UPDATE_ON_LAUNCH] ?: false // Default is OFF
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
     * Get current auto update setting (suspend function for one-time read)
     */
    suspend fun getAutoUpdateOnLaunch(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[AUTO_UPDATE_ON_LAUNCH] ?: false
        }.first()
    }
}
