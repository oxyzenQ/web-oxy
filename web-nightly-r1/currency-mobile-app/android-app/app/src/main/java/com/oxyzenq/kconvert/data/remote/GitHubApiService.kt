/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

/**
 * GitHub API service interface for fetching release information
 * Optimized for parallel requests with caching support
 */
interface GitHubApiService {
    
    /**
     * Get the latest release from GitHub repository
     * Uses HTTP caching for performance optimization
     */
    @Headers(
        "Accept: application/vnd.github+json",
        "Cache-Control: no-cache",
        "Pragma: no-cache"
    )
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Response<GitHubRelease>
    
    /**
     * Get all releases from GitHub repository
     * For parallel fetching of multiple release data
     */
    @Headers(
        "Accept: application/vnd.github+json",
        "Cache-Control: no-cache",
        "Pragma: no-cache"
    )
    @GET("repos/{owner}/{repo}/releases")
    suspend fun getAllReleases(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Response<List<GitHubRelease>>
    
    /**
     * Get specific release by tag
     * For targeted release information
     */
    @Headers(
        "Accept: application/vnd.github+json",
        "Cache-Control: no-cache",
        "Pragma: no-cache"
    )
    @GET("repos/{owner}/{repo}/releases/tags/{tag}")
    suspend fun getReleaseByTag(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("tag") tag: String
    ): Response<GitHubRelease>
}

/**
 * GitHub Release data model
 * Optimized for JSON parsing with Gson
 */
data class GitHubRelease(
    val id: Long,
    val tag_name: String,
    val name: String?,
    val body: String?,
    val draft: Boolean,
    val prerelease: Boolean,
    val created_at: String,
    val published_at: String?,
    val html_url: String,
    val assets: List<GitHubAsset>? = null
)

/**
 * GitHub Asset data model
 * For release assets information
 */
data class GitHubAsset(
    val id: Long,
    val name: String,
    val size: Long,
    val download_count: Int,
    val browser_download_url: String
)
