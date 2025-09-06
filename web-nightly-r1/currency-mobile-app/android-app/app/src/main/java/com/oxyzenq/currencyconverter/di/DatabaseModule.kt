/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.currencyconverter.di

import android.content.Context
import androidx.room.Room
import com.oxyzenq.currencyconverter.data.local.database.KconvertDatabase
import com.oxyzenq.currencyconverter.data.local.dao.CurrencyDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideKconvertDatabase(@ApplicationContext context: Context): KconvertDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            KconvertDatabase::class.java,
            "kconvert_database"
        ).build()
    }

    @Provides
    fun provideCurrencyDao(database: KconvertDatabase): CurrencyDao {
        return database.currencyDao()
    }
}
