/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.api

import com.oxyzenq.kconvert.data.model.ExchangeRatesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit API service for ExchangeRate-API
 */
interface ExchangeRateApiService {
    
    @GET("{apiKey}/latest/{baseCurrency}")
    suspend fun getExchangeRates(
        @Path("apiKey") apiKey: String,
        @Path("baseCurrency") baseCurrency: String
    ): Response<ExchangeApiResponse>
    
    @GET("{apiKey}/codes")
    suspend fun getSupportedCurrencies(
        @Path("apiKey") apiKey: String
    ): Response<CurrencyCodesResponse>
}

/**
 * API response models for ExchangeRate-API
 */
data class ExchangeApiResponse(
    val result: String,
    val documentation: String,
    val terms_of_use: String,
    val time_last_update_unix: Long,
    val time_last_update_utc: String,
    val time_next_update_unix: Long,
    val time_next_update_utc: String,
    val base_code: String,
    val conversion_rates: Map<String, Double>
)

data class CurrencyCodesResponse(
    val result: String,
    val documentation: String,
    val terms_of_use: String,
    val supported_codes: List<List<String>>
)
