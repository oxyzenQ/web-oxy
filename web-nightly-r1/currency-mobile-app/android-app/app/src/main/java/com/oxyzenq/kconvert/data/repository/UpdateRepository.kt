/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.repository

import com.oxyzenq.kconvert.data.remote.GitHubApiService
import com.oxyzenq.kconvert.data.remote.GitHubRelease
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling app updates with parallel network requests and caching
 */
@Singleton
class UpdateRepository @Inject constructor(
    private val gitHubApiService: GitHubApiService
) {
    
    companion object {
        private const val OWNER = "oxyzenq"
        private const val REPO = "web-oxy"
        private const val NETWORK_TIMEOUT = 30000L // 30 seconds
    }

    /**
     * Get the latest release with timeout and error handling
     */
    suspend fun getLatestRelease(): Result<GitHubRelease> = withContext(Dispatchers.IO) {
        try {
            withTimeout(NETWORK_TIMEOUT) {
                val response = gitHubApiService.getLatestRelease(OWNER, REPO)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("API Error: ${response.code()} ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get multiple releases in parallel for comparison
     */
    suspend fun getReleasesParallel(): Result<List<GitHubRelease>> = withContext(Dispatchers.IO) {
        try {
            withTimeout(NETWORK_TIMEOUT) {
                // Launch parallel requests
                val latestDeferred = async { gitHubApiService.getLatestRelease(OWNER, REPO) }
                val allReleasesDeferred = async { gitHubApiService.getAllReleases(OWNER, REPO) }
                
                // Await all results
                val results = awaitAll(latestDeferred, allReleasesDeferred)
                val latestResponse = results[0]
                val allReleasesResponse = results[1] as retrofit2.Response<List<GitHubRelease>>
                
                when {
                    allReleasesResponse.isSuccessful && allReleasesResponse.body() != null -> {
                        Result.success(allReleasesResponse.body()!!.take(5)) // Return top 5 releases
                    }
                    latestResponse.isSuccessful && latestResponse.body() != null -> {
                        Result.success(listOf(latestResponse.body()!!)) // Fallback to latest only
                    }
                    else -> {
                        Result.failure(Exception("Failed to fetch releases"))
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get specific release by tag
     */
    suspend fun getReleaseByTag(tag: String): Result<GitHubRelease> = withContext(Dispatchers.IO) {
        try {
            withTimeout(NETWORK_TIMEOUT) {
                val response = gitHubApiService.getReleaseByTag(OWNER, REPO, tag)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Release not found: $tag"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Compare version strings with enhanced parsing for tags like "2.dev-4"
     */
    fun compareVersions(latestVersion: String, currentVersion: String): VersionComparison {
        val latest = parseVersion(latestVersion)
        val current = parseVersion(currentVersion)
        
        return when {
            latest > current -> VersionComparison.NEWER_AVAILABLE
            latest < current -> VersionComparison.CURRENT_IS_NEWER
            else -> VersionComparison.UP_TO_DATE
        }
    }

    /**
     * Parse version string to comparable format
     * Handles formats like: "2.dev-4", "v1.2.3", "1.0.0-beta2"
     */
    private fun parseVersion(version: String): List<Int> {
        val cleaned = version.trim().removePrefix("v").removePrefix("V")
        val numbers = Regex("\\d+").findAll(cleaned)
            .map { it.value.toIntOrNull() ?: 0 }
            .toList()
        return if (numbers.isEmpty()) listOf(0) else numbers
    }
}

/**
 * Version comparison result
 */
enum class VersionComparison {
    NEWER_AVAILABLE,
    UP_TO_DATE,
    CURRENT_IS_NEWER
}

/**
 * Extension function for List<Int> comparison
 */
private operator fun List<Int>.compareTo(other: List<Int>): Int {
    val maxSize = maxOf(this.size, other.size)
    for (i in 0 until maxSize) {
        val a = this.getOrNull(i) ?: 0
        val b = other.getOrNull(i) ?: 0
        if (a != b) return a.compareTo(b)
    }
    return 0
}
