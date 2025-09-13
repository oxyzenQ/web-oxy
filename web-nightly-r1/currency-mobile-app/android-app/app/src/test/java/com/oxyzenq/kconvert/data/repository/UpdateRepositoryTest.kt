/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.repository

import com.oxyzenq.kconvert.data.remote.GitHubApiService
import com.oxyzenq.kconvert.data.remote.GitHubRelease
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import retrofit2.Response

/**
 * Unit tests for UpdateRepository with parallel network requests and caching
 */
class UpdateRepositoryTest {

    @Mock
    private lateinit var gitHubApiService: GitHubApiService

    private lateinit var updateRepository: UpdateRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        updateRepository = UpdateRepository(gitHubApiService)
    }

    @Test
    fun `getLatestRelease returns success when API call succeeds`() = runTest {
        // Given
        val mockRelease = GitHubRelease(
            tag_name = "v2.0.0",
            name = "Version 2.0.0",
            body = "New features and improvements",
            html_url = "https://github.com/oxyzenq/web-oxy/releases/tag/v2.0.0",
            published_at = "2025-01-01T00:00:00Z",
            assets = emptyList()
        )
        `when`(gitHubApiService.getLatestRelease("oxyzenq", "web-oxy"))
            .thenReturn(Response.success(mockRelease))

        // When
        val result = updateRepository.getLatestRelease()

        // Then
        assertTrue(result.isSuccess)
        assertEquals("v2.0.0", result.getOrNull()?.tag_name)
    }

    @Test
    fun `getLatestRelease returns failure when API call fails`() = runTest {
        // Given
        `when`(gitHubApiService.getLatestRelease("oxyzenq", "web-oxy"))
            .thenReturn(Response.error(404, "Not Found".toResponseBody()))

        // When
        val result = updateRepository.getLatestRelease()

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("404") == true)
    }

    @Test
    fun `compareVersions correctly identifies newer version`() {
        // Test various version formats
        assertEquals(
            VersionComparison.NEWER_AVAILABLE,
            updateRepository.compareVersions("v2.0.0", "v1.9.0")
        )
        
        assertEquals(
            VersionComparison.NEWER_AVAILABLE,
            updateRepository.compareVersions("2.dev-5", "2.dev-4")
        )
        
        assertEquals(
            VersionComparison.UP_TO_DATE,
            updateRepository.compareVersions("v1.0.0", "1.0.0")
        )
        
        assertEquals(
            VersionComparison.CURRENT_IS_NEWER,
            updateRepository.compareVersions("v1.0.0", "v2.0.0")
        )
    }

    @Test
    fun `getReleasesParallel handles multiple API calls efficiently`() = runTest {
        // Given
        val mockLatestRelease = GitHubRelease(
            tag_name = "v2.0.0",
            name = "Latest Release",
            body = "Latest version",
            html_url = "https://github.com/oxyzenq/web-oxy/releases/tag/v2.0.0",
            published_at = "2025-01-01T00:00:00Z",
            assets = emptyList()
        )
        
        val mockReleases = listOf(
            mockLatestRelease,
            GitHubRelease(
                tag_name = "v1.9.0",
                name = "Previous Release",
                body = "Previous version",
                html_url = "https://github.com/oxyzenq/web-oxy/releases/tag/v1.9.0",
                published_at = "2024-12-01T00:00:00Z",
                assets = emptyList()
            )
        )

        `when`(gitHubApiService.getLatestRelease("oxyzenq", "web-oxy"))
            .thenReturn(Response.success(mockLatestRelease))
        `when`(gitHubApiService.getAllReleases("oxyzenq", "web-oxy"))
            .thenReturn(Response.success(mockReleases))

        // When
        val result = updateRepository.getReleasesParallel()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        verify(gitHubApiService).getLatestRelease("oxyzenq", "web-oxy")
        verify(gitHubApiService).getAllReleases("oxyzenq", "web-oxy")
    }

    @Test
    fun `version parsing handles complex version strings`() {
        // Test the private parseVersion method through compareVersions
        val testCases = mapOf(
            "v2.0.0" to "1.9.0" to VersionComparison.NEWER_AVAILABLE,
            "2.dev-4" to "2.dev-3" to VersionComparison.NEWER_AVAILABLE,
            "1.0.0-beta2" to "1.0.0-beta1" to VersionComparison.NEWER_AVAILABLE,
            "3.1.4" to "3.1.4" to VersionComparison.UP_TO_DATE,
            "1.0" to "1.0.0" to VersionComparison.UP_TO_DATE
        )

        testCases.forEach { (versions, expected) ->
            val (latest, current) = versions
            assertEquals(
                "Failed for $latest vs $current",
                expected,
                updateRepository.compareVersions(latest, current)
            )
        }
    }
}
