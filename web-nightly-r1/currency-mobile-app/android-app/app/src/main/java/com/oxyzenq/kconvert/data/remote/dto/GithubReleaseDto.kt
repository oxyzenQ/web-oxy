/*
 * GitHub Release API Response DTO
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GithubReleaseDto(
    @Json(name = "tag_name")
    val tagName: String,
    
    @Json(name = "name")
    val name: String?,
    
    @Json(name = "body")
    val body: String?,
    
    @Json(name = "html_url")
    val htmlUrl: String?,
    
    @Json(name = "published_at")
    val publishedAt: String?,
    
    @Json(name = "prerelease")
    val prerelease: Boolean = false,
    
    @Json(name = "draft")
    val draft: Boolean = false
)
