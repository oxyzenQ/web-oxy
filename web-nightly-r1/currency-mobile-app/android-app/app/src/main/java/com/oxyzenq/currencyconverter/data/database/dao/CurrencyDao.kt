package com.oxyzenq.currencyconverter.data.database.dao

import androidx.room.*
import com.oxyzenq.currencyconverter.data.database.entities.CurrencyEntity
import com.oxyzenq.currencyconverter.data.database.entities.CurrencyRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {
    
    // Currency operations
    @Query("SELECT * FROM currencies WHERE isActive = 1 ORDER BY name ASC")
    fun getAllCurrencies(): Flow<List<CurrencyEntity>>
    
    @Query("SELECT * FROM currencies WHERE code = :currencyCode")
    suspend fun getCurrencyByCode(currencyCode: String): CurrencyEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrencies(currencies: List<CurrencyEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrency(currency: CurrencyEntity)
    
    @Query("DELETE FROM currencies WHERE timestamp < :cutoffTime")
    suspend fun deleteOldCurrencies(cutoffTime: Long)
    
    // Exchange rate operations
    @Query("SELECT * FROM currency_rates WHERE currencyPair = :currencyPair")
    suspend fun getExchangeRate(currencyPair: String): CurrencyRateEntity?
    
    @Query("SELECT * FROM currency_rates WHERE baseCurrency = :baseCurrency")
    suspend fun getExchangeRatesForBase(baseCurrency: String): List<CurrencyRateEntity>
    
    @Query("SELECT * FROM currency_rates WHERE baseCurrency = :baseCurrency")
    fun getExchangeRatesForBaseFlow(baseCurrency: String): Flow<List<CurrencyRateEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExchangeRates(rates: List<CurrencyRateEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExchangeRate(rate: CurrencyRateEntity)
    
    @Query("DELETE FROM currency_rates WHERE timestamp < :cutoffTime")
    suspend fun deleteOldExchangeRates(cutoffTime: Long)
    
    @Query("SELECT MAX(timestamp) FROM currency_rates WHERE baseCurrency = :baseCurrency")
    suspend fun getLatestTimestamp(baseCurrency: String): Long?
    
    @Query("DELETE FROM currency_rates")
    suspend fun clearAllRates()
    
    @Query("DELETE FROM currencies")
    suspend fun clearAllCurrencies()
}
