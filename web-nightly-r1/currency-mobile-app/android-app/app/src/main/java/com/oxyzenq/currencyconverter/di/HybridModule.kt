/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.di

import android.content.Context
import com.oxyzenq.kconvert.data.api.CurrencyApi
import com.oxyzenq.kconvert.data.database.CurrencyDatabase
import com.oxyzenq.kconvert.data.database.dao.CurrencyDao
import com.oxyzenq.kconvert.data.repository.HybridCurrencyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HybridModule {

    // Removed duplicate OkHttpClient and Retrofit providers - using NetworkModule instead

    @Provides
    @Singleton
    fun provideCurrencyApi(retrofit: Retrofit): CurrencyApi {
        return retrofit.create(CurrencyApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCurrencyDatabase(@ApplicationContext context: Context): CurrencyDatabase {
        return CurrencyDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideCurrencyDao(database: CurrencyDatabase): CurrencyDao {
        return database.currencyDao()
    }

    @Provides
    @Singleton
    fun provideHybridCurrencyRepository(
        currencyApi: CurrencyApi,
        currencyDao: CurrencyDao
    ): HybridCurrencyRepository {
        return HybridCurrencyRepository(currencyApi, currencyDao)
    }
}
