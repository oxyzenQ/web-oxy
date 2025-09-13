/*
 * High-performance API service for 100+ currencies including cryptocurrency
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.api

import retrofit2.Response
import retrofit2.http.*
import com.oxyzenq.kconvert.data.model.ExchangeRateResponse
import com.oxyzenq.kconvert.data.model.CryptoCurrencyResponse
import com.oxyzenq.kconvert.data.model.ExtendedCurrencyListResponse

interface CryptoApiService {
    
    /**
     * Get all supported currencies (100+ including crypto)
     * Ultra-fast endpoint with minimal data transfer
     */
    @Headers(
        "Accept: application/json",
        "Cache-Control: max-age=3600" // Cache for 1 hour (currencies don't change often)
    )
    @GET("v1/currencies")
    suspend fun getAllCurrencies(): Response<ExtendedCurrencyListResponse>
    
    /**
     * Get real-time exchange rates for multiple currencies
     * Optimized for batch requests
     */
    @Headers(
        "Accept: application/json",
        "Cache-Control: max-age=60" // Cache for 1 minute (rates change frequently)
    )
    @GET("v1/latest")
    suspend fun getExchangeRates(
        @Query("base") baseCurrency: String = "USD",
        @Query("symbols") targetCurrencies: String? = null // Comma-separated list
    ): Response<ExchangeRateResponse>
    
    /**
     * Get cryptocurrency prices from CoinGecko API
     * Supports 100+ cryptocurrencies
     */
    @Headers(
        "Accept: application/json",
        "Cache-Control: max-age=30" // Cache for 30 seconds (crypto is volatile)
    )
    @GET("https://api.coingecko.com/api/v3/simple/price")
    suspend fun getCryptoPrices(
        @Query("ids") cryptoIds: String, // Comma-separated crypto IDs
        @Query("vs_currencies") vsCurrencies: String = "usd,eur,btc"
    ): Response<CryptoCurrencyResponse>
    
    /**
     * Get top 100 cryptocurrencies by market cap
     * For crypto currency picker
     */
    @Headers(
        "Accept: application/json",
        "Cache-Control: max-age=300" // Cache for 5 minutes
    )
    @GET("https://api.coingecko.com/api/v3/coins/markets")
    suspend fun getTopCryptocurrencies(
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = false
    ): Response<List<CryptoCurrencyResponse>>
    
    /**
     * Batch request for multiple exchange rates
     * Ultra-optimized for performance
     */
    @Headers(
        "Accept: application/json",
        "Cache-Control: max-age=60"
    )
    @POST("v1/batch")
    suspend fun getBatchExchangeRates(
        @Body request: BatchExchangeRequest
    ): Response<BatchExchangeResponse>
}

/**
 * Request models for batch operations
 */
data class BatchExchangeRequest(
    val baseCurrencies: List<String>,
    val targetCurrencies: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)

data class BatchExchangeResponse(
    val rates: Map<String, Map<String, Double>>,
    val timestamp: Long,
    val success: Boolean
)
