/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.repository

import com.oxyzenq.kconvert.data.local.OfflineCurrencyService
import com.oxyzenq.kconvert.data.model.ConversionResponse
import com.oxyzenq.kconvert.data.model.Currency
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineCurrencyRepository @Inject constructor(
    private val offlineService: OfflineCurrencyService
) {
    
    suspend fun getCurrencies(): Result<List<Currency>> {
        return offlineService.getCurrencies()
    }
    
    suspend fun convertCurrency(
        amount: Double,
        fromCurrency: String,
        toCurrency: String
    ): Result<ConversionResponse> {
        return offlineService.convertCurrency(amount, fromCurrency, toCurrency)
    }
    
    suspend fun getExchangeRates(baseCurrency: String): Result<Map<String, Double>> {
        return offlineService.getExchangeRates(baseCurrency)
    }
}
