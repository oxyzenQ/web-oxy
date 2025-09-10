/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing user preferences and settings
 */
@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey
    val key: String,
    val value: String,
    val type: String, // "string", "boolean", "int", "float"
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Room entity for storing conversion history
 */
@Entity(tableName = "conversion_history")
data class ConversionHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fromCurrency: String,
    val toCurrency: String,
    val fromAmount: Double,
    val toAmount: Double,
    val exchangeRate: Double,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Room entity for storing favorite currency pairs
 */
@Entity(tableName = "favorite_pairs")
data class FavoritePairEntity(
    @PrimaryKey
    val id: String, // Format: "USD_IDR"
    val fromCurrency: String,
    val toCurrency: String,
    val displayName: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Room entity for storing cached API responses
 */
@Entity(tableName = "api_cache")
data class ApiCacheEntity(
    @PrimaryKey
    val cacheKey: String,
    val data: String, // JSON string
    val expiresAt: Long,
    val createdAt: Long = System.currentTimeMillis()
)
