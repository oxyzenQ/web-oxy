/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.repository

import com.oxyzenq.kconvert.data.api.CurrencyApi
import com.oxyzenq.kconvert.data.local.dao.CurrencyDao
import com.oxyzenq.kconvert.data.local.entity.CurrencyEntity
import com.oxyzenq.kconvert.data.local.entity.ExchangeRateEntity
import com.oxyzenq.kconvert.data.model.Currency
import com.oxyzenq.kconvert.data.model.ConversionResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HybridCurrencyRepository @Inject constructor(
    private val currencyApi: CurrencyApi,
    private val currencyDao: CurrencyDao
) {
    
    companion object {
        private const val DATA_EXPIRY_TIME = 24 * 60 * 60 * 1000L // 24 hours in milliseconds
    }
    
    // Get currencies with offline-first approach
    fun getCurrencies(): Flow<List<Currency>> {
        return currencyDao.getAllCurrencies().map { entities ->
            entities.map { entity ->
                Currency(
                    code = entity.code,
                    name = entity.name,
                    flag = entity.flag
                )
            }
        }
    }
    
    // Fetch and save currency rates from API
    suspend fun fetchAndSaveCurrencyRates(baseCurrency: String = "USD"): Result<Unit> {
        return try {
            val response = currencyApi.getExchangeRates(baseCurrency)
            if (response.isSuccessful) {
                val exchangeRateResponse = response.body()!!
                val currentTime = System.currentTimeMillis()
                
                // Convert API response to entities
                val rateEntities = exchangeRateResponse.rates.map { (currency, rate) ->
                    ExchangeRateEntity(
                        id = "${baseCurrency}_$currency",
                        fromCurrency = baseCurrency,
                        toCurrency = currency,
                        rate = rate,
                        lastUpdated = currentTime
                    )
                }
                
                // Save to database
                currencyDao.insertExchangeRates(rateEntities)
                
                // Clean old data
                deleteOldData()
                
                Result.success(Unit)
            } else {
                Result.failure(Exception("API request failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get offline currency rate
    suspend fun getOfflineCurrencyRate(from: String, to: String): ExchangeRateEntity? {
        return currencyDao.getExchangeRate(from, to)
    }
    
    // Get all rates for a base currency (offline) - simplified for new structure
    suspend fun getOfflineRatesForBase(baseCurrency: String): List<ExchangeRateEntity> {
        // Note: This method needs to be implemented in CurrencyDao if needed
        return emptyList() // Placeholder
    }
    
    // Get rates as Flow for reactive UI - simplified for new structure
    fun getOfflineRatesForBaseFlow(baseCurrency: String): Flow<List<ExchangeRateEntity>> {
        // Note: This method needs to be implemented in CurrencyDao if needed
        return kotlinx.coroutines.flow.flowOf(emptyList()) // Placeholder
    }
    
    // Force refresh currency rates (real-time)
    suspend fun refreshCurrencyRates(baseCurrency: String = "USD"): Result<Unit> {
        return fetchAndSaveCurrencyRates(baseCurrency)
    }
    
    // Convert currency using offline data first, fallback to API
    suspend fun convertCurrency(
        amount: Double,
        fromCurrency: String,
        toCurrency: String
    ): Result<ConversionResponse> {
        return try {
            // Try offline first
            val offlineRate = getOfflineCurrencyRate(fromCurrency, toCurrency)
            
            if (offlineRate != null && !isDataExpired(offlineRate.lastUpdated)) {
                // Use offline data
                val convertedAmount = amount * offlineRate.rate
                val response = ConversionResponse(
                    success = true,
                    fromCurrency = fromCurrency,
                    toCurrency = toCurrency,
                    amount = amount,
                    convertedAmount = String.format("%.2f", convertedAmount).toDouble(),
                    exchangeRate = offlineRate.rate,
                    timestamp = offlineRate.lastUpdated.toString(),
                    error = null
                )
                Result.success(response)
            } else {
                // Fetch fresh data
                fetchAndSaveCurrencyRates(fromCurrency).fold(
                    onSuccess = {
                        val freshRate = getOfflineCurrencyRate(fromCurrency, toCurrency)
                        if (freshRate != null) {
                            val convertedAmount = amount * freshRate.rate
                            val response = ConversionResponse(
                                success = true,
                                fromCurrency = fromCurrency,
                                toCurrency = toCurrency,
                                amount = amount,
                                convertedAmount = String.format("%.2f", convertedAmount).toDouble(),
                                exchangeRate = freshRate.rate,
                                timestamp = freshRate.lastUpdated.toString(),
                                error = null
                            )
                            Result.success(response)
                        } else {
                            Result.failure(Exception("Currency pair not found"))
                        }
                    },
                    onFailure = { error ->
                        Result.failure(error)
                    }
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Check if data is expired
    private fun isDataExpired(timestamp: Long): Boolean {
        return System.currentTimeMillis() - timestamp > DATA_EXPIRY_TIME
    }
    
    // Check if we have recent data - simplified for new structure
    suspend fun hasRecentData(baseCurrency: String): Boolean {
        // Note: This method needs to be implemented in CurrencyDao if needed
        return false // Placeholder
    }
    
    // Initialize currencies if not present
    suspend fun initializeCurrencies() {
        try {
            val response = currencyApi.getSupportedCurrencies()
            if (response.isSuccessful) {
                val currencyList = response.body()!!
                val currentTime = System.currentTimeMillis()
                
                val currencyEntities = currencyList.currencies.map { (code, name) ->
                    CurrencyEntity(
                        code = code,
                        name = name,
                        flag = getFlagUrl(code)
                    )
                }
                
                currencyDao.insertCurrencies(currencyEntities)
            }
        } catch (e: Exception) {
            // Fallback to default currencies if API fails
            initializeDefaultCurrencies()
        }
    }
    
    // Fallback currencies
    private suspend fun initializeDefaultCurrencies() {
        val defaultCurrencies = listOf(
            "USD" to "US Dollar",
            "EUR" to "Euro",
            "GBP" to "British Pound",
            "JPY" to "Japanese Yen",
            "AUD" to "Australian Dollar",
            "CAD" to "Canadian Dollar",
            "CHF" to "Swiss Franc",
            "CNY" to "Chinese Yuan",
            "INR" to "Indian Rupee",
            "IDR" to "Indonesian Rupiah"
        )
        
        val currentTime = System.currentTimeMillis()
        val entities = defaultCurrencies.map { (code, name) ->
            CurrencyEntity(
                code = code,
                name = name,
                flag = getFlagUrl(code)
            )
        }
        
        currencyDao.insertCurrencies(entities)
    }
    
    // Delete old data - simplified for new structure
    private suspend fun deleteOldData() {
        // Note: Implement cleanup logic if needed
        // For now, we rely on the database size management
    }
    
    // Get flag URL for currency
    private fun getFlagUrl(currencyCode: String): String {
        val countryMap = mapOf(
            "USD" to "us", "EUR" to "eu", "GBP" to "gb", "JPY" to "jp",
            "AUD" to "au", "CAD" to "ca", "CHF" to "ch", "CNY" to "cn",
            "INR" to "in", "IDR" to "id", "KRW" to "kr", "SGD" to "sg",
            "THB" to "th", "MYR" to "my", "PHP" to "ph", "VND" to "vn",
            "BRL" to "br", "MXN" to "mx", "ARS" to "ar", "CLP" to "cl",
            "COP" to "co", "PEN" to "pe", "UYU" to "uy", "BOB" to "bo",
            "RUB" to "ru", "TRY" to "tr", "ZAR" to "za", "EGP" to "eg",
            "NGN" to "ng", "KES" to "ke", "GHS" to "gh", "MAD" to "ma",
            "DZD" to "dz", "TND" to "tn", "LYD" to "ly", "ETB" to "et",
            "UGX" to "ug", "TZS" to "tz", "RWF" to "rw", "MWK" to "mw",
            "ZMW" to "zm", "BWP" to "bw", "NAD" to "na", "SZL" to "sz",
            "LSL" to "ls", "MZN" to "mz", "AOA" to "ao", "XAF" to "cm",
            "XOF" to "sn", "CVE" to "cv", "GMD" to "gm", "GNF" to "gn",
            "LRD" to "lr", "SLL" to "sl", "NZD" to "nz", "FJD" to "fj",
            "PGK" to "pg", "SBD" to "sb", "VUV" to "vu", "WST" to "ws",
            "TOP" to "to", "NOK" to "no", "SEK" to "se", "DKK" to "dk",
            "ISK" to "is", "PLN" to "pl", "CZK" to "cz", "HUF" to "hu",
            "RON" to "ro", "BGN" to "bg", "HRK" to "hr", "RSD" to "rs",
            "BAM" to "ba", "MKD" to "mk", "ALL" to "al", "MDL" to "md",
            "UAH" to "ua", "BYN" to "by", "LTL" to "lt", "LVL" to "lv",
            "EEK" to "ee", "GEL" to "ge", "AMD" to "am", "AZN" to "az",
            "KZT" to "kz", "UZS" to "uz", "TJS" to "tj", "KGS" to "kg",
            "TMT" to "tm", "AFN" to "af", "PKR" to "pk", "BDT" to "bd",
            "LKR" to "lk", "MVR" to "mv", "NPR" to "np", "BTN" to "bt",
            "MMK" to "mm", "KHR" to "kh", "LAK" to "la", "MNT" to "mn",
            "HKD" to "hk", "MOP" to "mo", "TWD" to "tw", "ILS" to "il",
            "JOD" to "jo", "LBP" to "lb", "SYP" to "sy", "IQD" to "iq",
            "SAR" to "sa", "QAR" to "qa", "AED" to "ae", "OMR" to "om",
            "YER" to "ye", "KWD" to "kw", "BHD" to "bh", "IRR" to "ir"
        )
        val countryCode = countryMap[currencyCode] ?: "xx"
        return "https://flagsapi.com/$countryCode/flat/64.png"
    }
    
    // Clear all data (for testing/reset)
    suspend fun clearAllData() {
        currencyDao.deleteAllExchangeRates()
        currencyDao.deleteAllCurrencies()
    }
}
