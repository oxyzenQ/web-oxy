/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.currencyconverter.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface CurrencyApi {
    
    @GET("latest/{baseCurrency}")
    suspend fun getExchangeRates(
        @Path("baseCurrency") baseCurrency: String = "USD"
    ): Response<ExchangeRateResponse>
    
    @GET("currencies")
    suspend fun getSupportedCurrencies(): Response<CurrencyListResponse>
}

data class ExchangeRateResponse(
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)

data class CurrencyListResponse(
    val currencies: Map<String, String>
)
