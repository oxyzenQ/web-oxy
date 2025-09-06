package com.oxyzenq.currencyconverter.data.local.dao

import androidx.room.*
import com.oxyzenq.currencyconverter.data.local.entity.CurrencyEntity
import com.oxyzenq.currencyconverter.data.local.entity.ExchangeRateEntity
import com.oxyzenq.currencyconverter.data.local.entity.AppMetadataEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Currency operations
 */
@Dao
interface CurrencyDao {
    
    // Currency operations
    @Query("SELECT * FROM currencies ORDER BY code ASC")
    fun getAllCurrencies(): Flow<List<CurrencyEntity>>
    
    @Query("SELECT * FROM currencies WHERE code = :code")
    suspend fun getCurrencyByCode(code: String): CurrencyEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrency(currency: CurrencyEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrencies(currencies: List<CurrencyEntity>)
    
    @Query("DELETE FROM currencies")
    suspend fun deleteAllCurrencies()
    
    @Query("SELECT COUNT(*) FROM currencies")
    suspend fun getCurrencyCount(): Int
    
    // Exchange rate operations
    @Query("SELECT * FROM exchange_rates WHERE fromCurrency = :from AND toCurrency = :to")
    suspend fun getExchangeRate(from: String, to: String): ExchangeRateEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExchangeRate(rate: ExchangeRateEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExchangeRates(rates: List<ExchangeRateEntity>)
    
    @Query("DELETE FROM exchange_rates")
    suspend fun deleteAllExchangeRates()
    
    // App metadata operations
    @Query("SELECT * FROM app_metadata WHERE key = :key")
    suspend fun getMetadata(key: String): AppMetadataEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: AppMetadataEntity)
    
    @Query("DELETE FROM app_metadata WHERE key = :key")
    suspend fun deleteMetadata(key: String)
    
    // Combined operations for complete data cleanup
    @Transaction
    suspend fun deleteAllData() {
        deleteAllCurrencies()
        deleteAllExchangeRates()
    }
}
