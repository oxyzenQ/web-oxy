/*
 * Optimized Room Entity for user settings
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.oxyzenq.kconvert.AppVersion
import androidx.room.Index
import androidx.room.ColumnInfo

@Entity(
    tableName = "user_settings",
    indices = [Index(value = ["id"], unique = true)]
)
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = 1, // Single row for all settings
    
    // Currency preferences
    val sourceCurrency: String = "USD",
    val targetCurrency: String = "EUR",
    
    // UI preferences
    val themeMode: String = "SYSTEM", // LIGHT, DARK, SYSTEM
    val hapticsEnabled: Boolean = true,
    
    // Data preferences
    val autoRefresh: Boolean = true,
    val refreshInterval: Int = 30, // minutes
    
    // Cache settings
    val cacheEnabled: Boolean = true,
    val cacheSize: Long = 10 * 1024 * 1024, // 10MB
    
    // Performance settings
    val animationsEnabled: Boolean = true,
    val meteorAnimationEnabled: Boolean = true,
    val lowLatencyMode: Boolean = false,
    
    // Metadata
    val lastUpdated: Long = System.currentTimeMillis(),
    val version: String = AppVersion.VERSION_NAME
)
