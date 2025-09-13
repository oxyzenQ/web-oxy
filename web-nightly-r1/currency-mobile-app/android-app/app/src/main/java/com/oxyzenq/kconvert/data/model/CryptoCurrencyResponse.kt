/*
 * Response models for cryptocurrency and extended currency data
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.model

import com.google.gson.annotations.SerializedName

/**
 * Response for cryptocurrency price data from CoinGecko
 */
data class CryptoCurrencyResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("symbol")
    val symbol: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("current_price")
    val currentPrice: Double,
    
    @SerializedName("market_cap")
    val marketCap: Long?,
    
    @SerializedName("market_cap_rank")
    val marketCapRank: Int?,
    
    @SerializedName("price_change_percentage_24h")
    val priceChangePercentage24h: Double?,
    
    @SerializedName("image")
    val image: String?
)

/**
 * Response for extended currency list (traditional + crypto)
 */
data class ExtendedCurrencyListResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("currencies")
    val currencies: Map<String, ExtendedCurrencyInfo>
)

data class ExtendedCurrencyInfo(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("symbol")
    val symbol: String,
    
    @SerializedName("type")
    val type: String, // "fiat", "crypto"
    
    @SerializedName("country")
    val country: String? = null,
    
    @SerializedName("icon")
    val icon: String? = null
)

/**
 * Extended exchange rate response supporting crypto
 */
data class ExchangeRateResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("timestamp")
    val timestamp: Long,
    
    @SerializedName("base")
    val base: String,
    
    @SerializedName("date")
    val date: String,
    
    @SerializedName("rates")
    val rates: Map<String, Double>
)
