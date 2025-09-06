/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.currencyconverter.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currency_rates")
data class CurrencyRateEntity(
    @PrimaryKey
    val currencyPair: String, // e.g., "USD_EUR", "USD_IDR"
    val baseCurrency: String,
    val targetCurrency: String,
    val exchangeRate: Double,
    val timestamp: Long,
    val lastUpdated: String // ISO date string
)

@Entity(tableName = "currencies")
data class CurrencyEntity(
    @PrimaryKey
    val code: String, // e.g., "USD", "EUR"
    val name: String, // e.g., "US Dollar", "Euro"
    val flag: String, // Flag URL
    val isActive: Boolean = true,
    val timestamp: Long
)
