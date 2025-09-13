/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing currency data locally
 */
@Entity(tableName = "currencies")
data class CurrencyEntity(
    @PrimaryKey
    val code: String,
    val name: String,
    val flag: String,
    val rate: Double = 1.0, // Exchange rate relative to base currency (USD)
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Room entity for storing exchange rate data
 */
@Entity(tableName = "exchange_rates")
data class ExchangeRateEntity(
    @PrimaryKey
    val id: String, // Format: "USD_IDR"
    val fromCurrency: String,
    val toCurrency: String,
    val rate: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Room entity for storing app metadata
 */
@Entity(tableName = "app_metadata")
data class AppMetadataEntity(
    @PrimaryKey
    val key: String,
    val value: String,
    val lastUpdated: Long = System.currentTimeMillis()
)
