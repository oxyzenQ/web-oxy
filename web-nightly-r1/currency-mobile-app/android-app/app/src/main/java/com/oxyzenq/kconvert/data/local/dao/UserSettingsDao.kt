/*
 * Optimized Room DAO for ultra-fast user settings
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.oxyzenq.kconvert.data.local.entity.UserSettingsEntity

@Dao
interface UserSettingsDao {
    
    /**
     * Ultra-fast settings retrieval with Flow for reactive updates
     */
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<UserSettingsEntity?>
    
    /**
     * Synchronous settings for immediate access (cached)
     */
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): UserSettingsEntity?
    
    /**
     * Batch upsert for maximum performance
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: UserSettingsEntity)
    
    /**
     * Individual setting updates for minimal I/O
     */
    @Query("UPDATE user_settings SET source_currency = :currency WHERE id = 1")
    suspend fun updateSourceCurrency(currency: String)
    
    @Query("UPDATE user_settings SET target_currency = :currency WHERE id = 1")
    suspend fun updateTargetCurrency(currency: String)
    
    @Query("UPDATE user_settings SET theme_mode = :theme WHERE id = 1")
    suspend fun updateTheme(theme: String)
    
    @Query("UPDATE user_settings SET haptics_enabled = :enabled WHERE id = 1")
    suspend fun updateHaptics(enabled: Boolean)
    
    @Query("UPDATE user_settings SET auto_refresh = :enabled WHERE id = 1")
    suspend fun updateAutoRefresh(enabled: Boolean)
    
    @Query("UPDATE user_settings SET refresh_interval = :minutes WHERE id = 1")
    suspend fun updateRefreshInterval(minutes: Int)
    
    @Query("UPDATE user_settings SET last_updated = :timestamp WHERE id = 1")
    suspend fun updateLastUpdated(timestamp: Long)
    
    /**
     * Bulk currency preferences update
     */
    @Query("UPDATE user_settings SET source_currency = :source, target_currency = :target WHERE id = 1")
    suspend fun updateCurrencyPair(source: String, target: String)
    
    /**
     * Performance: Get specific settings without full entity
     */
    @Query("SELECT source_currency FROM user_settings WHERE id = 1")
    suspend fun getSourceCurrency(): String?
    
    @Query("SELECT target_currency FROM user_settings WHERE id = 1")
    suspend fun getTargetCurrency(): String?
    
    @Query("SELECT haptics_enabled FROM user_settings WHERE id = 1")
    suspend fun getHapticsEnabled(): Boolean?
    
    /**
     * Initialize default settings if none exist
     */
    @Query("INSERT OR IGNORE INTO user_settings (id, source_currency, target_currency, theme_mode, haptics_enabled, auto_refresh, refresh_interval, last_updated) VALUES (1, 'USD', 'EUR', 'SYSTEM', 1, 1, 30, :timestamp)")
    suspend fun initializeDefaults(timestamp: Long = System.currentTimeMillis())
    
    /**
     * Clear all settings (for reset functionality)
     */
    @Query("DELETE FROM user_settings")
    suspend fun clearAllSettings()
}
