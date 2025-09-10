/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.local.dao

import androidx.room.*
import com.oxyzenq.kconvert.data.local.entity.UserPreferencesEntity
import com.oxyzenq.kconvert.data.local.entity.ConversionHistoryEntity
import com.oxyzenq.kconvert.data.local.entity.FavoritePairEntity
import com.oxyzenq.kconvert.data.local.entity.ApiCacheEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for User Preferences and App Data
 */
@Dao
interface UserPreferencesDao {
    
    // User preferences operations
    @Query("SELECT * FROM user_preferences WHERE key = :key")
    suspend fun getPreference(key: String): UserPreferencesEntity?
    
    @Query("SELECT * FROM user_preferences")
    fun getAllPreferences(): Flow<List<UserPreferencesEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreference(preference: UserPreferencesEntity)
    
    @Query("DELETE FROM user_preferences WHERE key = :key")
    suspend fun deletePreference(key: String)
    
    @Query("DELETE FROM user_preferences")
    suspend fun deleteAllPreferences()
    
    // Conversion history operations
    @Query("SELECT * FROM conversion_history ORDER BY timestamp DESC LIMIT :limit")
    fun getConversionHistory(limit: Int = 50): Flow<List<ConversionHistoryEntity>>
    
    @Query("SELECT * FROM conversion_history WHERE fromCurrency = :from AND toCurrency = :to ORDER BY timestamp DESC LIMIT :limit")
    fun getConversionHistoryForPair(from: String, to: String, limit: Int = 10): Flow<List<ConversionHistoryEntity>>
    
    @Insert
    suspend fun insertConversion(conversion: ConversionHistoryEntity)
    
    @Query("DELETE FROM conversion_history WHERE id = :id")
    suspend fun deleteConversion(id: Long)
    
    @Query("DELETE FROM conversion_history")
    suspend fun deleteAllConversions()
    
    @Query("DELETE FROM conversion_history WHERE timestamp < :timestamp")
    suspend fun deleteOldConversions(timestamp: Long)
    
    // Favorite pairs operations
    @Query("SELECT * FROM favorite_pairs ORDER BY createdAt DESC")
    fun getFavoritePairs(): Flow<List<FavoritePairEntity>>
    
    @Query("SELECT * FROM favorite_pairs WHERE id = :id")
    suspend fun getFavoritePair(id: String): FavoritePairEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoritePair(pair: FavoritePairEntity)
    
    @Query("DELETE FROM favorite_pairs WHERE id = :id")
    suspend fun deleteFavoritePair(id: String)
    
    @Query("DELETE FROM favorite_pairs")
    suspend fun deleteAllFavoritePairs()
    
    // API cache operations
    @Query("SELECT * FROM api_cache WHERE cacheKey = :key AND expiresAt > :currentTime")
    suspend fun getCachedData(key: String, currentTime: Long = System.currentTimeMillis()): ApiCacheEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedData(cache: ApiCacheEntity)
    
    @Query("DELETE FROM api_cache WHERE cacheKey = :key")
    suspend fun deleteCachedData(key: String)
    
    @Query("DELETE FROM api_cache WHERE expiresAt < :currentTime")
    suspend fun deleteExpiredCache(currentTime: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM api_cache")
    suspend fun deleteAllCache()
    
    // Utility operations
    @Query("SELECT COUNT(*) FROM conversion_history")
    suspend fun getConversionCount(): Int
    
    @Query("SELECT COUNT(*) FROM favorite_pairs")
    suspend fun getFavoritePairCount(): Int
    
    @Transaction
    suspend fun clearAllUserData() {
        deleteAllPreferences()
        deleteAllConversions()
        deleteAllFavoritePairs()
        deleteAllCache()
    }
}
