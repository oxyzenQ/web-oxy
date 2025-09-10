/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.oxyzenq.kconvert.data.local.dao.CurrencyDao
import com.oxyzenq.kconvert.data.local.dao.UserPreferencesDao
import com.oxyzenq.kconvert.data.local.entity.CurrencyEntity
import com.oxyzenq.kconvert.data.local.entity.ExchangeRateEntity
import com.oxyzenq.kconvert.data.local.entity.AppMetadataEntity
import com.oxyzenq.kconvert.data.local.entity.UserPreferencesEntity
import com.oxyzenq.kconvert.data.local.entity.ConversionHistoryEntity
import com.oxyzenq.kconvert.data.local.entity.FavoritePairEntity
import com.oxyzenq.kconvert.data.local.entity.ApiCacheEntity
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room database for Kconvert app
 */
@Database(
    entities = [
        CurrencyEntity::class,
        ExchangeRateEntity::class,
        AppMetadataEntity::class,
        UserPreferencesEntity::class,
        ConversionHistoryEntity::class,
        FavoritePairEntity::class,
        ApiCacheEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class KconvertDatabase : RoomDatabase() {
    
    abstract fun currencyDao(): CurrencyDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    
    companion object {
        @Volatile
        private var INSTANCE: KconvertDatabase? = null
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create new tables for version 2
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_preferences (
                        key TEXT NOT NULL PRIMARY KEY,
                        value TEXT NOT NULL,
                        type TEXT NOT NULL,
                        lastUpdated INTEGER NOT NULL
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS conversion_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        fromCurrency TEXT NOT NULL,
                        toCurrency TEXT NOT NULL,
                        fromAmount REAL NOT NULL,
                        toAmount REAL NOT NULL,
                        exchangeRate REAL NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS favorite_pairs (
                        id TEXT NOT NULL PRIMARY KEY,
                        fromCurrency TEXT NOT NULL,
                        toCurrency TEXT NOT NULL,
                        displayName TEXT,
                        createdAt INTEGER NOT NULL
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS api_cache (
                        cacheKey TEXT NOT NULL PRIMARY KEY,
                        data TEXT NOT NULL,
                        expiresAt INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """)
            }
        }
        
        fun getDatabase(context: Context): KconvertDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KconvertDatabase::class.java,
                    "kconvert_database"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
