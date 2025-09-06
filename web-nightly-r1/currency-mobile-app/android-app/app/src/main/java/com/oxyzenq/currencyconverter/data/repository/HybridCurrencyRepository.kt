package com.oxyzenq.currencyconverter.data.repository

import com.oxyzenq.currencyconverter.data.api.CurrencyApi
import com.oxyzenq.currencyconverter.data.database.dao.CurrencyDao
import com.oxyzenq.currencyconverter.data.database.entities.CurrencyEntity
import com.oxyzenq.currencyconverter.data.database.entities.CurrencyRateEntity
import com.oxyzenq.currencyconverter.data.model.Currency
import com.oxyzenq.currencyconverter.data.model.ConversionResponse
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
                    CurrencyRateEntity(
                        currencyPair = "${baseCurrency}_$currency",
                        baseCurrency = baseCurrency,
                        targetCurrency = currency,
                        exchangeRate = rate,
                        timestamp = currentTime,
                        lastUpdated = exchangeRateResponse.date
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
    suspend fun getOfflineCurrencyRate(currencyPair: String): CurrencyRateEntity? {
        return currencyDao.getExchangeRate(currencyPair)
    }
    
    // Get all rates for a base currency (offline)
    suspend fun getOfflineRatesForBase(baseCurrency: String): List<CurrencyRateEntity> {
        return currencyDao.getExchangeRatesForBase(baseCurrency)
    }
    
    // Get rates as Flow for reactive UI
    fun getOfflineRatesForBaseFlow(baseCurrency: String): Flow<List<CurrencyRateEntity>> {
        return currencyDao.getExchangeRatesForBaseFlow(baseCurrency)
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
            val offlineRate = getOfflineCurrencyRate("${fromCurrency}_$toCurrency")
            
            if (offlineRate != null && !isDataExpired(offlineRate.timestamp)) {
                // Use offline data
                val convertedAmount = amount * offlineRate.exchangeRate
                val response = ConversionResponse(
                    success = true,
                    fromCurrency = fromCurrency,
                    toCurrency = toCurrency,
                    amount = amount,
                    convertedAmount = String.format("%.2f", convertedAmount).toDouble(),
                    exchangeRate = offlineRate.exchangeRate,
                    timestamp = offlineRate.lastUpdated,
                    error = null
                )
                Result.success(response)
            } else {
                // Fetch fresh data
                fetchAndSaveCurrencyRates(fromCurrency).fold(
                    onSuccess = {
                        val freshRate = getOfflineCurrencyRate("${fromCurrency}_$toCurrency")
                        if (freshRate != null) {
                            val convertedAmount = amount * freshRate.exchangeRate
                            val response = ConversionResponse(
                                success = true,
                                fromCurrency = fromCurrency,
                                toCurrency = toCurrency,
                                amount = amount,
                                convertedAmount = String.format("%.2f", convertedAmount).toDouble(),
                                exchangeRate = freshRate.exchangeRate,
                                timestamp = freshRate.lastUpdated,
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
    
    // Check if we have recent data
    suspend fun hasRecentData(baseCurrency: String): Boolean {
        val latestTimestamp = currencyDao.getLatestTimestamp(baseCurrency)
        return latestTimestamp != null && !isDataExpired(latestTimestamp)
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
                        flag = getFlagUrl(code),
                        isActive = true,
                        timestamp = currentTime
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
                flag = getFlagUrl(code),
                isActive = true,
                timestamp = currentTime
            )
        }
        
        currencyDao.insertCurrencies(entities)
    }
    
    // Delete old data
    private suspend fun deleteOldData() {
        val cutoffTime = System.currentTimeMillis() - DATA_EXPIRY_TIME
        currencyDao.deleteOldExchangeRates(cutoffTime)
        currencyDao.deleteOldCurrencies(cutoffTime)
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
        currencyDao.clearAllRates()
        currencyDao.clearAllCurrencies()
    }
}
