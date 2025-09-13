/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.repository

import com.oxyzenq.kconvert.data.remote.GitHubApiService
import com.oxyzenq.kconvert.data.remote.GitHubRelease
import com.oxyzenq.kconvert.AppVersion
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
        private const val OWNER = "oxyzenQ"
        private const val REPO = "web-oxy"
        private const val NETWORK_TIMEOUT = 30000L // 30 seconds
    }

    /**
     * Get the latest release with enhanced error handling for offline mode
     */
    suspend fun getLatestRelease(): Result<GitHubRelease> = withContext(Dispatchers.IO) {
        try {
            withTimeout(NETWORK_TIMEOUT) {
                val response = gitHubApiService.getLatestRelease(OWNER, REPO)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorMessage = when (response.code()) {
                        404 -> "Repository not found. Please check the repository URL."
                        403 -> "GitHub API rate limit exceeded. Please try again later."
                        401 -> "Authentication required for this repository."
                        else -> "API Error: ${response.code()} ${response.message()}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            }
        } catch (e: java.net.UnknownHostException) {
            Result.failure(Exception("Cannot check updates. No internet connection."))
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(Exception("Connection timeout. Please check your internet connection."))
        } catch (e: java.io.IOException) {
            Result.failure(Exception("Network error. Please check your internet connection."))
        } catch (e: Exception) {
            Result.failure(Exception("Cannot check updates. No internet connection."))
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
                val latestResponse = latestDeferred.await()
                val allReleasesResponse = allReleasesDeferred.await()
                
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
     * Compare versions with Stellar naming convention support
     * Handles: Stellar-1.4, Stellar-1.4.dev, and legacy formats
     */
    fun compareVersions(latestVersion: String, currentVersion: String): VersionComparison {
        val latestType = detectVersionType(latestVersion)
        val currentType = detectVersionType(currentVersion)
        
        // Extract base version numbers for comparison
        val latestBase = extractBaseVersion(latestVersion)
        val currentBase = extractBaseVersion(currentVersion)
        
        // If versions are from different naming schemes, it's a mismatch
        if ((latestType == VersionType.UNKNOWN) != (currentType == VersionType.UNKNOWN)) {
            return VersionComparison.VERSION_MISMATCH
        }
        
        val baseComparison = compareBaseVersions(latestBase, currentBase)
        
        return when {
            baseComparison > 0 -> VersionComparison.NEWER_AVAILABLE
            baseComparison < 0 -> VersionComparison.CURRENT_IS_NEWER
            baseComparison == 0 -> {
                // Same base version, check dev vs stable
                when {
                    latestType == currentType -> VersionComparison.UP_TO_DATE
                    latestType == VersionType.DEVELOPMENT && currentType == VersionType.STABLE_RELEASE -> VersionComparison.NEWER_AVAILABLE
                    latestType == VersionType.STABLE_RELEASE && currentType == VersionType.DEVELOPMENT -> VersionComparison.CURRENT_IS_NEWER
                    else -> VersionComparison.UP_TO_DATE
                }
            }
            else -> VersionComparison.UP_TO_DATE
        }
    }
    
    /**
     * Extract base version number from Stellar format
     * Stellar-1.4 -> [1, 4]
     * Stellar-1.4.dev -> [1, 4]
     */
    private fun extractBaseVersion(version: String): List<Int> {
        val stellarMatch = Regex("^Stellar-(\\d+)\\.(\\d+)(\\.dev)?$").find(version.trim())
        return if (stellarMatch != null) {
            listOf(
                stellarMatch.groupValues[1].toInt(),
                stellarMatch.groupValues[2].toInt()
            )
        } else {
            // Fallback to legacy parsing
            parseVersion(version)
        }
    }
    
    /**
     * Compare base version numbers
     */
    private fun compareBaseVersions(version1: List<Int>, version2: List<Int>): Int {
        val maxSize = maxOf(version1.size, version2.size)
        for (i in 0 until maxSize) {
            val v1 = version1.getOrNull(i) ?: 0
            val v2 = version2.getOrNull(i) ?: 0
            if (v1 != v2) return v1.compareTo(v2)
        }
        return 0
    }

    /**
     * Detect version type based on Stellar naming convention
     * Stellar-1.4 = STABLE_RELEASE
     * Stellar-1.4.dev = DEVELOPMENT
     */
    fun detectVersionType(version: String): VersionType {
        val cleanVersion = version.trim()
        return when {
            cleanVersion.matches(Regex("^Stellar-\\d+\\.\\d+\\.dev$")) -> VersionType.DEVELOPMENT
            cleanVersion.matches(Regex("^Stellar-\\d+\\.\\d+$")) -> VersionType.STABLE_RELEASE
            cleanVersion.contains("beta") || cleanVersion.contains("alpha") || cleanVersion.contains("rc") -> VersionType.PRE_RELEASE
            else -> VersionType.UNKNOWN
        }
    }

    /**
     * Generate appropriate update message based on Stellar version comparison
     */
    fun generateUpdateMessage(
        latestVersion: String,
        currentVersion: String,
        comparison: VersionComparison
    ): UpdateMessage {
        val currentType = detectVersionType(currentVersion)
        val latestType = detectVersionType(latestVersion)
        
        return when (comparison) {
            VersionComparison.NEWER_AVAILABLE -> {
                when (currentType) {
                    VersionType.DEVELOPMENT -> UpdateMessage(
                        title = "Update Available",
                        message = "Update available for development version → New: $latestVersion vs Current: $currentVersion",
                        showGitHubLink = true,
                        isWarning = false,
                        uiState = UpdateUIState.UPDATE_AVAILABLE
                    )
                    VersionType.STABLE_RELEASE -> UpdateMessage(
                        title = "Update Available",
                        message = "Update available → New: $latestVersion vs Current: $currentVersion",
                        showGitHubLink = true,
                        isWarning = false,
                        uiState = UpdateUIState.UPDATE_AVAILABLE
                    )
                    else -> UpdateMessage(
                        title = "Update Available",
                        message = "Update available → New: $latestVersion vs Current: $currentVersion",
                        showGitHubLink = true,
                        isWarning = false,
                        uiState = UpdateUIState.UPDATE_AVAILABLE
                    )
                }
            }
            VersionComparison.UP_TO_DATE -> {
                when (currentType) {
                    VersionType.DEVELOPMENT -> UpdateMessage(
                        title = "Latest Development",
                        message = "Using latest development version ($currentVersion)",
                        showGitHubLink = false,
                        isWarning = false,
                        uiState = UpdateUIState.UP_TO_DATE
                    )
                    VersionType.STABLE_RELEASE -> UpdateMessage(
                        title = "Latest Release",
                        message = "Using latest stable release ($currentVersion)",
                        showGitHubLink = false,
                        isWarning = false,
                        uiState = UpdateUIState.UP_TO_DATE
                    )
                    else -> UpdateMessage(
                        title = "Up to Date",
                        message = "Using latest version ($currentVersion)",
                        showGitHubLink = false,
                        isWarning = false,
                        uiState = UpdateUIState.UP_TO_DATE
                    )
                }
            }
            VersionComparison.VERSION_MISMATCH -> UpdateMessage(
                title = "Version Mismatch",
                message = "This app version differs from the original repository. Please visit official GitHub releases for safe updates.",
                showGitHubLink = true,
                isWarning = true,
                uiState = UpdateUIState.MISMATCH_WARNING
            )
            VersionComparison.CURRENT_IS_NEWER -> {
                when (currentType) {
                    VersionType.DEVELOPMENT -> UpdateMessage(
                        title = "Development Version",
                        message = "Using development version $currentVersion. Latest stable: $latestVersion",
                        showGitHubLink = true,
                        isWarning = false,
                        uiState = UpdateUIState.UP_TO_DATE
                    )
                    else -> UpdateMessage(
                        title = "Version Mismatch",
                        message = "This app version differs from the original repository. Please visit official GitHub releases for safe updates.",
                        showGitHubLink = true,
                        isWarning = true,
                        uiState = UpdateUIState.MISMATCH_WARNING
                    )
                }
            }
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
    CURRENT_IS_NEWER,
    VERSION_MISMATCH
}

/**
 * Version type classification
 */
enum class VersionType {
    DEVELOPMENT,
    PRE_RELEASE,
    STABLE_RELEASE,
    UNKNOWN
}

/**
 * UI state for update checker with color coding
 */
enum class UpdateUIState {
    UP_TO_DATE,        // Green highlight
    UPDATE_AVAILABLE,  // Yellow/Orange highlight
    MISMATCH_WARNING   // Red highlight
}

/**
 * Update message data class
 */
data class UpdateMessage(
    val title: String,
    val message: String,
    val showGitHubLink: Boolean,
    val isWarning: Boolean,
    val uiState: UpdateUIState
)

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
