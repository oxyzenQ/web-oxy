/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.di

import android.content.Context
import androidx.room.Room
import com.oxyzenq.kconvert.data.local.database.KconvertDatabase
import com.oxyzenq.kconvert.data.local.dao.CurrencyDao
import com.oxyzenq.kconvert.data.local.dao.UserPreferencesDao
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
        return KconvertDatabase.getDatabase(context)
    }

    @Provides
    fun provideCurrencyDao(database: KconvertDatabase): CurrencyDao {
        return database.currencyDao()
    }

    @Provides
    fun provideUserPreferencesDao(database: KconvertDatabase): UserPreferencesDao {
        return database.userPreferencesDao()
    }
}
