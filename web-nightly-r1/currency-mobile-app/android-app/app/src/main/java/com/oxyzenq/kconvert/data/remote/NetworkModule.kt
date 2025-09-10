/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.remote

import android.content.Context
import com.oxyzenq.kconvert.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.tls.HandshakeCertificates
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.X509TrustManager

/**
 * Network module for Retrofit configuration with security, caching, and performance optimizations
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val GITHUB_BASE_URL = "https://api.github.com/"
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
     * Configure SSL/TLS security with certificate pinning for GitHub API
     */
    private fun configureSslSecurity(builder: OkHttpClient.Builder) {
        try {
            // Certificate pinning for GitHub API
            val certificatePinner = CertificatePinner.Builder()
                .add("api.github.com", "sha256/WoiWRyIOVNa9ihaBciRSC7XHjliYS9VwUGOIud4PB18=")
                .add("api.github.com", "sha256/RMuXhqHncJBQcuMiNiOhcCTyQwNdep6QTMNcAFjZuqU=")
                .build()

            builder.certificatePinner(certificatePinner)

            // Enhanced TLS configuration
            val handshakeCertificates = HandshakeCertificates.Builder()
                .addPlatformTrustedCertificates()
                .build()

            builder.sslSocketFactory(
                handshakeCertificates.sslSocketFactory(),
                handshakeCertificates.trustManager
            )

        } catch (e: Exception) {
            // Fallback to default SSL configuration if certificate pinning fails
            // This ensures the app still works even if certificates change
        }
    }

    /**
     * Provides Retrofit instance configured for GitHub API
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(GITHUB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provides GitHub API service
     */
    @Provides
    @Singleton
    fun provideGitHubApiService(retrofit: Retrofit): GitHubApiService {
        return retrofit.create(GitHubApiService::class.java)
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
