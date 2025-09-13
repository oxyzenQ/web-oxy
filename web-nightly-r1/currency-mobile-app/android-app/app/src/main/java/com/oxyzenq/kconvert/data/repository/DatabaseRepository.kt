/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.repository

import com.oxyzenq.kconvert.data.local.dao.CurrencyDao
import com.oxyzenq.kconvert.data.local.dao.UserPreferencesDao
import com.oxyzenq.kconvert.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for database operations with Room
 */
@Singleton
class DatabaseRepository @Inject constructor(
    private val currencyDao: CurrencyDao,
    private val userPreferencesDao: UserPreferencesDao
) {
    
    // Currency operations
    fun getAllCurrencies(): Flow<List<CurrencyEntity>> = currencyDao.getAllCurrencies()
    
    suspend fun getCurrencyByCode(code: String): CurrencyEntity? = 
        currencyDao.getCurrencyByCode(code)
    
    suspend fun insertCurrency(currency: CurrencyEntity) = 
        currencyDao.insertCurrency(currency)
    
    suspend fun insertCurrencies(currencies: List<CurrencyEntity>) = 
        currencyDao.insertCurrencies(currencies)
    
    suspend fun getCurrencyCount(): Int = currencyDao.getCurrencyCount()
    
    // Exchange rate operations
    suspend fun getExchangeRate(from: String, to: String): ExchangeRateEntity? = 
        currencyDao.getExchangeRate(from, to)
    
    suspend fun insertExchangeRate(rate: ExchangeRateEntity) = 
        currencyDao.insertExchangeRate(rate)
    
    suspend fun insertExchangeRates(rates: List<ExchangeRateEntity>) = 
        currencyDao.insertExchangeRates(rates)
    
    // User preferences operations
    suspend fun getPreference(key: String): String? = 
        userPreferencesDao.getPreference(key)?.value
    
    suspend fun getBooleanPreference(key: String, defaultValue: Boolean = false): Boolean = 
        userPreferencesDao.getPreference(key)?.value?.toBooleanStrictOrNull() ?: defaultValue
    
    suspend fun getIntPreference(key: String, defaultValue: Int = 0): Int = 
        userPreferencesDao.getPreference(key)?.value?.toIntOrNull() ?: defaultValue
    
    suspend fun getFloatPreference(key: String, defaultValue: Float = 0f): Float = 
        userPreferencesDao.getPreference(key)?.value?.toFloatOrNull() ?: defaultValue
    
    suspend fun setPreference(key: String, value: String) {
        userPreferencesDao.insertPreference(
            UserPreferencesEntity(key, value, "string")
        )
    }
    
    suspend fun setBooleanPreference(key: String, value: Boolean) {
        userPreferencesDao.insertPreference(
            UserPreferencesEntity(key, value.toString(), "boolean")
        )
    }
    
    suspend fun setIntPreference(key: String, value: Int) {
        userPreferencesDao.insertPreference(
            UserPreferencesEntity(key, value.toString(), "int")
        )
    }
    
    suspend fun setFloatPreference(key: String, value: Float) {
        userPreferencesDao.insertPreference(
            UserPreferencesEntity(key, value.toString(), "float")
        )
    }
    
    // Conversion history operations
    fun getConversionHistory(limit: Int = 50): Flow<List<ConversionHistoryEntity>> = 
        userPreferencesDao.getConversionHistory(limit)
    
    fun getConversionHistoryForPair(from: String, to: String, limit: Int = 10): Flow<List<ConversionHistoryEntity>> = 
        userPreferencesDao.getConversionHistoryForPair(from, to, limit)
    
    suspend fun insertConversion(
        fromCurrency: String,
        toCurrency: String,
        fromAmount: Double,
        toAmount: Double,
        exchangeRate: Double
    ) {
        userPreferencesDao.insertConversion(
            ConversionHistoryEntity(
                fromCurrency = fromCurrency,
                toCurrency = toCurrency,
                fromAmount = fromAmount,
                toAmount = toAmount,
                exchangeRate = exchangeRate
            )
        )
    }
    
    suspend fun deleteConversion(id: Long) = userPreferencesDao.deleteConversion(id)
    
    suspend fun clearConversionHistory() = userPreferencesDao.deleteAllConversions()
    
    // Favorite pairs operations
    fun getFavoritePairs(): Flow<List<FavoritePairEntity>> = 
        userPreferencesDao.getFavoritePairs()
    
    suspend fun addFavoritePair(fromCurrency: String, toCurrency: String, displayName: String? = null) {
        val id = "${fromCurrency}_${toCurrency}"
        userPreferencesDao.insertFavoritePair(
            FavoritePairEntity(
                id = id,
                fromCurrency = fromCurrency,
                toCurrency = toCurrency,
                displayName = displayName
            )
        )
    }
    
    suspend fun removeFavoritePair(fromCurrency: String, toCurrency: String) {
        val id = "${fromCurrency}_${toCurrency}"
        userPreferencesDao.deleteFavoritePair(id)
    }
    
    suspend fun isFavoritePair(fromCurrency: String, toCurrency: String): Boolean {
        val id = "${fromCurrency}_${toCurrency}"
        return userPreferencesDao.getFavoritePair(id) != null
    }
    
    // API cache operations
    suspend fun getCachedData(key: String): String? = 
        userPreferencesDao.getCachedData(key)?.data
    
    suspend fun setCachedData(key: String, data: String, expirationMinutes: Int = 60) {
        val expiresAt = System.currentTimeMillis() + (expirationMinutes * 60 * 1000)
        userPreferencesDao.insertCachedData(
            ApiCacheEntity(
                cacheKey = key,
                data = data,
                expiresAt = expiresAt
            )
        )
    }
    
    suspend fun clearExpiredCache() = userPreferencesDao.deleteExpiredCache()
    
    suspend fun clearAllCache() = userPreferencesDao.deleteAllCache()
    
    // Utility operations
    suspend fun clearAllData() {
        currencyDao.deleteAllData()
        userPreferencesDao.clearAllUserData()
    }
    
    suspend fun getDatabaseStats(): DatabaseStats {
        return DatabaseStats(
            currencyCount = currencyDao.getCurrencyCount(),
            conversionCount = userPreferencesDao.getConversionCount(),
            favoritePairCount = userPreferencesDao.getFavoritePairCount()
        )
    }
}

/**
 * Data class for database statistics
 */
data class DatabaseStats(
    val currencyCount: Int,
    val conversionCount: Int,
    val favoritePairCount: Int
)
