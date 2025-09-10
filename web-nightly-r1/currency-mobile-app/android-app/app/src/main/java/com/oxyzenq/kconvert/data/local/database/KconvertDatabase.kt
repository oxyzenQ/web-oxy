/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.oxyzenq.kconvert.data.local.dao.CurrencyDao
import com.oxyzenq.kconvert.data.local.entity.CurrencyEntity
import com.oxyzenq.kconvert.data.local.entity.ExchangeRateEntity
import com.oxyzenq.kconvert.data.local.entity.AppMetadataEntity

/**
 * Room database for Kconvert app
 */
@Database(
    entities = [
        CurrencyEntity::class,
        ExchangeRateEntity::class,
        AppMetadataEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KconvertDatabase : RoomDatabase() {
    
    abstract fun currencyDao(): CurrencyDao
    
    companion object {
        @Volatile
        private var INSTANCE: KconvertDatabase? = null
        
        fun getDatabase(context: Context): KconvertDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KconvertDatabase::class.java,
                    "kconvert_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
