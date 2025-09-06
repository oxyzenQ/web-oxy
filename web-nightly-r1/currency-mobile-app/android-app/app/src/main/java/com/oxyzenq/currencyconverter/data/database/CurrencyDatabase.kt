package com.oxyzenq.currencyconverter.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.oxyzenq.currencyconverter.data.database.dao.CurrencyDao
import com.oxyzenq.currencyconverter.data.database.entities.CurrencyEntity
import com.oxyzenq.currencyconverter.data.database.entities.CurrencyRateEntity

@Database(
    entities = [CurrencyEntity::class, CurrencyRateEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CurrencyDatabase : RoomDatabase() {
    
    abstract fun currencyDao(): CurrencyDao
    
    companion object {
        @Volatile
        private var INSTANCE: CurrencyDatabase? = null
        
        fun getDatabase(context: Context): CurrencyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CurrencyDatabase::class.java,
                    "currency_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
