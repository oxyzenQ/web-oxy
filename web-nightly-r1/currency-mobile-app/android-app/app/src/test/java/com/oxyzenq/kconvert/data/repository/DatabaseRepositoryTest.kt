/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oxyzenq.kconvert.data.local.database.KconvertDatabase
import com.oxyzenq.kconvert.data.local.entity.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Comprehensive tests for Room database implementation
 */
@RunWith(AndroidJUnit4::class)
class DatabaseRepositoryTest {

    private lateinit var database: KconvertDatabase
    private lateinit var repository: DatabaseRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            KconvertDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = DatabaseRepository(
            database.currencyDao(),
            database.userPreferencesDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveCurrency() = runTest {
        // Given
        val currency = CurrencyEntity(
            code = "USD",
            name = "US Dollar",
            flag = "🇺🇸",
            rate = 1.0
        )

        // When
        repository.insertCurrency(currency)
        val retrieved = repository.getCurrencyByCode("USD")

        // Then
        assertNotNull(retrieved)
        assertEquals("USD", retrieved?.code)
        assertEquals("US Dollar", retrieved?.name)
        assertEquals("🇺🇸", retrieved?.flag)
        assertEquals(1.0, retrieved?.rate, 0.001)
    }

    @Test
    fun insertMultipleCurrenciesAndCount() = runTest {
        // Given
        val currencies = listOf(
            CurrencyEntity("USD", "US Dollar", "🇺🇸", 1.0),
            CurrencyEntity("EUR", "Euro", "🇪🇺", 0.85),
            CurrencyEntity("IDR", "Indonesian Rupiah", "🇮🇩", 15000.0)
        )

        // When
        repository.insertCurrencies(currencies)
        val count = repository.getCurrencyCount()
        val allCurrencies = repository.getAllCurrencies().first()

        // Then
        assertEquals(3, count)
        assertEquals(3, allCurrencies.size)
        assertTrue(allCurrencies.any { it.code == "USD" })
        assertTrue(allCurrencies.any { it.code == "EUR" })
        assertTrue(allCurrencies.any { it.code == "IDR" })
    }

    @Test
    fun exchangeRateOperations() = runTest {
        // Given
        val rate = ExchangeRateEntity(
            id = "USD_IDR",
            fromCurrency = "USD",
            toCurrency = "IDR",
            rate = 15000.0
        )

        // When
        repository.insertExchangeRate(rate)
        val retrieved = repository.getExchangeRate("USD", "IDR")

        // Then
        assertNotNull(retrieved)
        assertEquals("USD_IDR", retrieved?.id)
        assertEquals("USD", retrieved?.fromCurrency)
        assertEquals("IDR", retrieved?.toCurrency)
        assertEquals(15000.0, retrieved?.rate, 0.001)
    }

    @Test
    fun userPreferencesOperations() = runTest {
        // Test string preference
        repository.setPreference("theme", "dark")
        assertEquals("dark", repository.getPreference("theme"))

        // Test boolean preference
        repository.setBooleanPreference("haptics_enabled", true)
        assertTrue(repository.getBooleanPreference("haptics_enabled"))

        // Test int preference
        repository.setIntPreference("refresh_interval", 300)
        assertEquals(300, repository.getIntPreference("refresh_interval"))

        // Test float preference
        repository.setFloatPreference("animation_speed", 1.5f)
        assertEquals(1.5f, repository.getFloatPreference("animation_speed"), 0.001f)

        // Test default values
        assertFalse(repository.getBooleanPreference("non_existent", false))
        assertEquals(0, repository.getIntPreference("non_existent", 0))
        assertEquals(0f, repository.getFloatPreference("non_existent", 0f), 0.001f)
    }

    @Test
    fun conversionHistoryOperations() = runTest {
        // Insert conversion history
        repository.insertConversion("USD", "IDR", 100.0, 1500000.0, 15000.0)
        repository.insertConversion("EUR", "USD", 50.0, 58.82, 1.1764)

        // Retrieve all history
        val allHistory = repository.getConversionHistory().first()
        assertEquals(2, allHistory.size)

        // Retrieve history for specific pair
        val usdIdrHistory = repository.getConversionHistoryForPair("USD", "IDR").first()
        assertEquals(1, usdIdrHistory.size)
        assertEquals("USD", usdIdrHistory[0].fromCurrency)
        assertEquals("IDR", usdIdrHistory[0].toCurrency)
        assertEquals(100.0, usdIdrHistory[0].fromAmount, 0.001)
        assertEquals(1500000.0, usdIdrHistory[0].toAmount, 0.001)

        // Clear history
        repository.clearConversionHistory()
        val clearedHistory = repository.getConversionHistory().first()
        assertTrue(clearedHistory.isEmpty())
    }

    @Test
    fun favoritePairsOperations() = runTest {
        // Add favorite pairs
        repository.addFavoritePair("USD", "IDR", "Dollar to Rupiah")
        repository.addFavoritePair("EUR", "USD")

        // Check if pairs are favorites
        assertTrue(repository.isFavoritePair("USD", "IDR"))
        assertTrue(repository.isFavoritePair("EUR", "USD"))
        assertFalse(repository.isFavoritePair("GBP", "JPY"))

        // Get all favorite pairs
        val favorites = repository.getFavoritePairs().first()
        assertEquals(2, favorites.size)

        // Remove favorite pair
        repository.removeFavoritePair("USD", "IDR")
        assertFalse(repository.isFavoritePair("USD", "IDR"))
        assertTrue(repository.isFavoritePair("EUR", "USD"))
    }

    @Test
    fun apiCacheOperations() = runTest {
        // Set cached data
        val testData = """{"rates":{"USD":1.0,"EUR":0.85}}"""
        repository.setCachedData("exchange_rates", testData, 60)

        // Retrieve cached data
        val cachedData = repository.getCachedData("exchange_rates")
        assertEquals(testData, cachedData)

        // Test non-existent cache
        assertNull(repository.getCachedData("non_existent"))

        // Clear cache
        repository.clearAllCache()
        assertNull(repository.getCachedData("exchange_rates"))
    }

    @Test
    fun databaseStatsOperations() = runTest {
        // Insert test data
        repository.insertCurrencies(listOf(
            CurrencyEntity("USD", "US Dollar", "🇺🇸", 1.0),
            CurrencyEntity("EUR", "Euro", "🇪🇺", 0.85)
        ))
        repository.insertConversion("USD", "EUR", 100.0, 85.0, 0.85)
        repository.addFavoritePair("USD", "EUR")

        // Get stats
        val stats = repository.getDatabaseStats()
        assertEquals(2, stats.currencyCount)
        assertEquals(1, stats.conversionCount)
        assertEquals(1, stats.favoritePairCount)
    }

    @Test
    fun clearAllDataOperations() = runTest {
        // Insert test data
        repository.insertCurrency(CurrencyEntity("USD", "US Dollar", "🇺🇸", 1.0))
        repository.setPreference("test", "value")
        repository.insertConversion("USD", "EUR", 100.0, 85.0, 0.85)
        repository.addFavoritePair("USD", "EUR")

        // Verify data exists
        assertEquals(1, repository.getCurrencyCount())
        assertEquals("value", repository.getPreference("test"))

        // Clear all data
        repository.clearAllData()

        // Verify data is cleared
        assertEquals(0, repository.getCurrencyCount())
        assertNull(repository.getPreference("test"))
        assertTrue(repository.getConversionHistory().first().isEmpty())
        assertTrue(repository.getFavoritePairs().first().isEmpty())
    }
}
