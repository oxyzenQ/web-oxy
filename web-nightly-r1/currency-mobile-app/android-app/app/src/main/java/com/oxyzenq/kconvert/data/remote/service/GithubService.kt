/*
 * GitHub API Service for Release Checking
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.remote.service

import com.oxyzenq.kconvert.data.remote.dto.GithubReleaseDto
import retrofit2.Response
import retrofit2.http.GET

interface GithubService {
    @GET("repos/oxyzenq/web-oxy/releases/latest")
    suspend fun getLatestRelease(): Response<GithubReleaseDto>
}
