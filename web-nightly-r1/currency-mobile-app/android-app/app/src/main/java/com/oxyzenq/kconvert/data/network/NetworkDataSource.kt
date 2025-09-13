/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.network

import com.oxyzenq.kconvert.data.api.ExchangeRateApiService
import com.oxyzenq.kconvert.data.local.entity.CurrencyEntity
import com.oxyzenq.kconvert.data.local.entity.ExchangeRateEntity
import com.oxyzenq.kconvert.security.UltraSecureApiKeyManager
import com.oxyzenq.kconvert.security.RASPSecurityManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Network data source for fetching real-time currency data
 * Handles API calls and data transformation
 */
@Singleton
class NetworkDataSource @Inject constructor(
    private val apiService: ExchangeRateApiService,
    private val ultraSecureApiKeyManager: UltraSecureApiKeyManager,
    private val raspSecurityManager: RASPSecurityManager
) {
    
    /**
     * Fetch supported currencies from API
     */
    suspend fun fetchSupportedCurrencies(context: android.content.Context? = null): Result<List<CurrencyEntity>> {
        return try {
            // Ultra-security validation (skip if context is null)
            if (context != null) {
                raspSecurityManager.performSecurityAssessment(context)
                if (!raspSecurityManager.isApiAccessAllowed()) {
                    return Result.failure(Exception("Security assessment failed - API access denied"))
                }
            }
            
            val apiKey = if (context != null) {
                ultraSecureApiKeyManager.getApiKey(context)
                    ?: return Result.failure(Exception("Ultra-secure API key not available"))
            } else {
                "demo_key" // Fallback for testing
            }
            
            val response = apiService.getSupportedCurrencies(apiKey)
            
            if (response.isSuccessful && response.body()?.result == "success") {
                val currencyData = response.body()!!.supported_codes
                val currencies = currencyData.map { (code, name) ->
                    CurrencyEntity(
                        code = code,
                        name = name,
                        flag = getFlagUrl(code),
                        rate = 1.0, // Will be updated with exchange rates
                        lastUpdated = System.currentTimeMillis()
                    )
                }
                Result.success(currencies)
            } else {
                Result.failure(Exception("API request failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Fetch exchange rates for a specific base currency
     */
    suspend fun fetchExchangeRates(context: android.content.Context, baseCurrency: String): Result<List<ExchangeRateEntity>> {
        return try {
            // Ultra-security validation
            raspSecurityManager.performSecurityAssessment(context)
            if (!raspSecurityManager.isApiAccessAllowed()) {
                return Result.failure(Exception("Security assessment failed - API access denied"))
            }
            
            val apiKey = ultraSecureApiKeyManager.getApiKey(context)
                ?: return Result.failure(Exception("Ultra-secure API key not available"))
            
            val response = apiService.getExchangeRates(apiKey, baseCurrency)
            
            if (response.isSuccessful && response.body()?.result == "success") {
                val rates = response.body()!!.conversion_rates
                val exchangeRates = rates.map { (currency, rate) ->
                    ExchangeRateEntity(
                        id = "${baseCurrency}_${currency}",
                        fromCurrency = baseCurrency,
                        toCurrency = currency,
                        rate = rate,
                        lastUpdated = System.currentTimeMillis()
                    )
                }
                Result.success(exchangeRates)
            } else {
                Result.failure(Exception("API request failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Fetch comprehensive currency data (currencies + rates)
     */
    suspend fun fetchAllCurrencyData(context: android.content.Context): Result<Pair<List<CurrencyEntity>, List<ExchangeRateEntity>>> {
        return try {
            // Fetch currencies and rates in parallel
            val currenciesResult = fetchSupportedCurrencies(context)
            val ratesResult = fetchExchangeRates(context, "USD")
            
            if (currenciesResult.isSuccess && ratesResult.isSuccess) {
                val currencies = currenciesResult.getOrThrow()
                val rates = ratesResult.getOrThrow()
                
                // Update currency entities with their rates
                val updatedCurrencies = currencies.map { currency ->
                    val rate = rates.find { it.toCurrency == currency.code }?.rate ?: 1.0
                    currency.copy(rate = rate)
                }
                
                Result.success(Pair(updatedCurrencies, rates))
            } else {
                val error = currenciesResult.exceptionOrNull() ?: ratesResult.exceptionOrNull()
                Result.failure(error ?: Exception("Unknown error occurred"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get flag URL for currency code
     */
    private fun getFlagUrl(currencyCode: String): String {
        val countryCode = when (currencyCode) {
            "USD" -> "us"
            "EUR" -> "eu"
            "GBP" -> "gb"
            "JPY" -> "jp"
            "AUD" -> "au"
            "CAD" -> "ca"
            "CHF" -> "ch"
            "CNY" -> "cn"
            "SEK" -> "se"
            "NZD" -> "nz"
            "MXN" -> "mx"
            "SGD" -> "sg"
            "HKD" -> "hk"
            "NOK" -> "no"
            "TRY" -> "tr"
            "RUB" -> "ru"
            "INR" -> "in"
            "BRL" -> "br"
            "ZAR" -> "za"
            "KRW" -> "kr"
            "IDR" -> "id"
            "MYR" -> "my"
            "THB" -> "th"
            "PHP" -> "ph"
            "VND" -> "vn"
            else -> currencyCode.take(2).lowercase()
        }
        return "https://flagcdn.com/w320/$countryCode.png"
    }
}
