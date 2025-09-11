/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.di

import android.content.Context
import com.oxyzenq.kconvert.data.api.ExchangeRateApiService
import com.oxyzenq.kconvert.data.preferences.AppPreferences
import com.oxyzenq.kconvert.data.remote.GitHubApiService
import com.oxyzenq.kconvert.security.UltraSecureApiKeyManager
import com.oxyzenq.kconvert.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import javax.inject.Named
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.tls.HandshakeCertificates
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module for network dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val GITHUB_BASE_URL = "https://api.github.com/"
    private const val EXCHANGE_RATE_BASE_URL = "https://v6.exchangerate-api.com/v6/"
    private const val CACHE_SIZE = 10 * 1024 * 1024L // 10MB cache
    private const val CONNECT_TIMEOUT = 15L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L

    /**
     * Provides HTTP cache for network requests
     */
    @Provides
    @Singleton
    fun provideHttpCache(@ApplicationContext context: Context): Cache {
        val cacheDir = File(context.cacheDir, "http_cache")
        return Cache(cacheDir, CACHE_SIZE)
    }

    /**
     * Provides secure OkHttpClient with SSL/TLS, caching, and performance optimizations
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        cache: Cache
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .followSslRedirects(true)

        // Add logging interceptor for debug builds
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            builder.addInterceptor(loggingInterceptor)
        }

        // Add cache control interceptor
        builder.addInterceptor(CacheControlInterceptor())

        // Add network cache interceptor for offline support
        builder.addNetworkInterceptor(NetworkCacheInterceptor())

        // Add User-Agent interceptor
        builder.addInterceptor(UserAgentInterceptor())

        // SSL/TLS Security Configuration
        configureSslSecurity(builder)

        return builder.build()
    }

    /**
     * Configure SSL/TLS security - removed strict certificate pinning for GitHub API compatibility
     */
    private fun configureSslSecurity(builder: OkHttpClient.Builder) {
        try {
            // Use platform trusted certificates without strict pinning
            // This allows GitHub API to work with certificate updates
            val handshakeCertificates = HandshakeCertificates.Builder()
                .addPlatformTrustedCertificates()
                .build()

            builder.sslSocketFactory(
                handshakeCertificates.sslSocketFactory(),
                handshakeCertificates.trustManager
            )

        } catch (e: Exception) {
            // Fallback to default SSL configuration
            // This ensures the app still works with standard HTTPS
        }
    }

    @Provides
    @Singleton
    @Named("exchangeRate")
    fun provideExchangeRateRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(EXCHANGE_RATE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("github")
    fun provideGitHubRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(GITHUB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideExchangeRateApiService(@Named("exchangeRate") retrofit: Retrofit): ExchangeRateApiService {
        return retrofit.create(ExchangeRateApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGitHubApiService(@Named("github") retrofit: Retrofit): GitHubApiService {
        return retrofit.create(GitHubApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAppPreferences(@ApplicationContext context: Context): AppPreferences {
        return AppPreferences(context)
    }
}

/**
 * Cache control interceptor for optimizing network requests
 */
private class CacheControlInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // Cache successful responses for 5 minutes
        return if (response.isSuccessful) {
            response.newBuilder()
                .header("Cache-Control", "public, max-age=300")
                .build()
        } else {
            response
        }
    }
}

/**
 * Network cache interceptor for offline support
 */
private class NetworkCacheInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        
        // Cache successful responses for offline access (1 day)
        return response.newBuilder()
            .header("Cache-Control", "public, max-age=86400")
            .build()
    }
}

/**
 * User-Agent interceptor for API identification
 */
private class UserAgentInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("User-Agent", "Kconvert/${BuildConfig.VERSION_NAME} (Android)")
            .build()
        return chain.proceed(request)
    }
}
