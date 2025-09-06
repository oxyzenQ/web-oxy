package com.oxyzenq.currencyconverter.data.api

import com.oxyzenq.currencyconverter.data.model.ConversionRequest
import com.oxyzenq.currencyconverter.data.model.ConversionResponse
import com.oxyzenq.currencyconverter.data.model.CurrencyListResponse
import com.oxyzenq.currencyconverter.data.model.ExchangeRatesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CurrencyApiService {
    
    @GET("api/v1/currencies")
    suspend fun getCurrencies(): Response<CurrencyListResponse>
    
    @POST("api/v1/convert")
    suspend fun convertCurrency(@Body request: ConversionRequest): Response<ConversionResponse>
    
    @GET("api/v1/rates/{baseCurrency}")
    suspend fun getExchangeRates(@Path("baseCurrency") baseCurrency: String): Response<ExchangeRatesResponse>
    
    @GET("api/v1/rate/{from}/{to}")
    suspend fun getSingleRate(
        @Path("from") fromCurrency: String,
        @Path("to") toCurrency: String
    ): Response<Map<String, Any>>
    
    @GET("health")
    suspend fun healthCheck(): Response<Map<String, Any>>
}
