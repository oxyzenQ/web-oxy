/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.repository

import com.oxyzenq.kconvert.data.local.dao.CurrencyDao
import com.oxyzenq.kconvert.data.local.entity.CurrencyEntity
import com.oxyzenq.kconvert.data.local.entity.ExchangeRateEntity
import com.oxyzenq.kconvert.data.local.entity.AppMetadataEntity
import com.oxyzenq.kconvert.data.model.Currency
import com.oxyzenq.kconvert.data.network.NetworkDataSource
import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repository for currency data operations
 * Handles both local database and API operations
 */
@Singleton
class CurrencyRepository @Inject constructor(
    private val currencyDao: CurrencyDao,
    private val networkDataSource: NetworkDataSource
) {
    
    companion object {
        private const val LAST_UPDATE_KEY = "last_update_timestamp"
        private const val DATA_SOURCE_KEY = "data_source"
    }
    
    /**
     * Get all currencies from local database
     */
    fun getAllCurrencies(): Flow<List<Currency>> {
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
    
    /**
     * Get currency count from database
     */
    suspend fun getCurrencyCount(): Int {
        return currencyDao.getCurrencyCount()
    }
    
    /**
     * Get exchange rate between two currencies
     */
    suspend fun getExchangeRate(fromCurrency: String, toCurrency: String): Double? {
        val rate = currencyDao.getExchangeRate(fromCurrency, toCurrency)
        return rate?.rate
    }
    
    /**
     * Fetch and save currency data from API with fallback to sample data
     */
    suspend fun fetchAndSaveCurrencyData(context: Context): Result<String> {
        return try {
            val networkResult = networkDataSource.fetchAllCurrencyData(context)
            
            if (networkResult.isSuccess) {
                val (currencies, rates) = networkResult.getOrThrow()
                currencyDao.insertCurrencies(currencies)
                currencyDao.insertExchangeRates(rates)
                
                // Save metadata
                val timestamp = System.currentTimeMillis()
                val formattedTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
                
                currencyDao.insertMetadata(
                    AppMetadataEntity(LAST_UPDATE_KEY, formattedTime)
                )
                currencyDao.insertMetadata(
                    AppMetadataEntity(DATA_SOURCE_KEY, "Real-time API (Ultra-Secure)")
                )
                
                Result.success("Ultra-secure real-time data updated successfully")
            } else {
                // Fallback to sample data if API fails
                val sampleCurrencies = getSampleCurrencies()
                currencyDao.insertCurrencies(sampleCurrencies)
                
                val sampleRates = getSampleExchangeRates()
                currencyDao.insertExchangeRates(sampleRates)
                
                // Save metadata
                val timestamp = System.currentTimeMillis()
                val formattedTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
                
                currencyDao.insertMetadata(
                    AppMetadataEntity(LAST_UPDATE_KEY, formattedTime)
                )
                currencyDao.insertMetadata(
                    AppMetadataEntity(DATA_SOURCE_KEY, "Offline Sample Data")
                )
                
                Result.success("Sample data loaded (API unavailable)")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if data needs refresh (older than 1 hour)
     */
    suspend fun isDataStale(): Boolean {
        val lastUpdate = currencyDao.getMetadata(LAST_UPDATE_KEY)
        if (lastUpdate == null) return true
        
        return try {
            val lastUpdateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(lastUpdate.value)
            val currentTime = Date()
            val diffInHours = (currentTime.time - lastUpdateTime?.time!!) / (1000 * 60 * 60)
            diffInHours >= 1 // Refresh if older than 1 hour
        } catch (e: Exception) {
            true // Refresh if parsing fails
        }
    }
    
    /**
     * Sync data if needed (smart refresh)
     */
    suspend fun syncDataIfNeeded(context: Context): Result<String> {
        return if (isDataStale()) {
            fetchAndSaveCurrencyData(context)
        } else {
            Result.success("Data is up to date")
        }
    }
    
    /**
     * Delete all currency data from database
     */
    suspend fun deleteAllCurrencyData(): Result<String> {
        return try {
            currencyDao.deleteAllData()
            currencyDao.deleteMetadata(LAST_UPDATE_KEY)
            currencyDao.deleteMetadata(DATA_SOURCE_KEY)
            Result.success("All data deleted successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get last update timestamp
     */
    suspend fun getLastUpdateTimestamp(): String {
        val metadata = currencyDao.getMetadata(LAST_UPDATE_KEY)
        return metadata?.value ?: "null, no data please click button 'refresh data of price' to update/fetching"
    }
    
    /**
     * Convert currency amount
     */
    suspend fun convertCurrency(fromCurrency: String, toCurrency: String, amount: Double): Result<ConversionResult> {
        return try {
            if (fromCurrency == toCurrency) {
                return Result.success(ConversionResult(amount, 1.0, fromCurrency, toCurrency))
            }
            
            val rate = getExchangeRate(fromCurrency, toCurrency)
            if (rate != null) {
                val convertedAmount = amount * rate
                Result.success(ConversionResult(convertedAmount, rate, fromCurrency, toCurrency))
            } else {
                Result.failure(Exception("Exchange rate not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sample currencies for testing
     */
    private fun getSampleCurrencies(): List<CurrencyEntity> {
        return listOf(
            CurrencyEntity("USD", "US Dollar", "https://flagcdn.com/w320/us.png", 1.0),
            CurrencyEntity("EUR", "Euro", "https://flagcdn.com/w320/eu.png", 0.85),
            CurrencyEntity("IDR", "Indonesian Rupiah", "https://flagcdn.com/w320/id.png", 15000.0),
            CurrencyEntity("GBP", "British Pound", "https://flagcdn.com/w320/gb.png", 0.75),
            CurrencyEntity("JPY", "Japanese Yen", "https://flagcdn.com/w320/jp.png", 110.0),
            CurrencyEntity("AUD", "Australian Dollar", "https://flagcdn.com/w320/au.png", 1.35),
            CurrencyEntity("CAD", "Canadian Dollar", "https://flagcdn.com/w320/ca.png", 1.25),
            CurrencyEntity("CHF", "Swiss Franc", "https://flagcdn.com/w320/ch.png", 0.92),
            CurrencyEntity("CNY", "Chinese Yuan", "https://flagcdn.com/w320/cn.png", 6.45),
            CurrencyEntity("SGD", "Singapore Dollar", "https://flagcdn.com/w320/sg.png", 1.35)
        )
    }
    
    /**
     * Sample exchange rates for testing
     */
    private fun getSampleExchangeRates(): List<ExchangeRateEntity> {
        return listOf(
            ExchangeRateEntity("USD_EUR", "USD", "EUR", 0.85),
            ExchangeRateEntity("USD_IDR", "USD", "IDR", 15000.0),
            ExchangeRateEntity("USD_GBP", "USD", "GBP", 0.75),
            ExchangeRateEntity("USD_JPY", "USD", "JPY", 110.0),
            ExchangeRateEntity("EUR_USD", "EUR", "USD", 1.18),
            ExchangeRateEntity("EUR_IDR", "EUR", "IDR", 17647.0),
            ExchangeRateEntity("IDR_USD", "IDR", "USD", 0.000067),
            ExchangeRateEntity("IDR_EUR", "IDR", "EUR", 0.000057)
        )
    }
}

/**
 * Data class for conversion result
 */
data class ConversionResult(
    val convertedAmount: Double,
    val exchangeRate: Double,
    val fromCurrency: String,
    val toCurrency: String
)
