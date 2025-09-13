/*
 * Centralized App Version Management
 * Edit this file ONLY to update app version across entire codebase
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert

/**
 * Single source of truth for app version information.
 * Change VERSION_NAME here and it propagates everywhere automatically.
 */
object AppVersion {
    
    // ===== EDIT THIS LINE ONLY TO UPDATE APP VERSION =====
    const val VERSION_NAME = "Stellar-1.3"
    // =====================================================
    
    // Auto-generated version components (DO NOT EDIT)
    // Parse Stellar-X.Y or Stellar-X.Y.dev format
    private val stellarRegex = Regex("^Stellar-(\\d+)\\.(\\d+)(\\.dev)?$")
    private val stellarMatch = stellarRegex.find(VERSION_NAME)
    
    val MAJOR_VERSION = stellarMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1
    val MINOR_VERSION = stellarMatch?.groupValues?.get(2)?.toIntOrNull() ?: 4
    val IS_DEV = stellarMatch?.groupValues?.get(3) == ".dev"
    
    // Version code for Play Store (auto-incremented based on version)
    // Add 1000 if dev version to distinguish from stable
    val VERSION_CODE = (MAJOR_VERSION * 10000) + (MINOR_VERSION * 100) + if (IS_DEV) 1000 else 0
    
    // Display strings
    val VERSION_DISPLAY = "Kconvert v$VERSION_NAME"
    val VERSION_FULL = "Kconvert $VERSION_NAME (Build $VERSION_CODE)"
    
    // API User-Agent
    val USER_AGENT = "Kconvert/$VERSION_NAME (Android)"
    
    // GitHub release tag format
    val RELEASE_TAG = "v$VERSION_NAME"
    
    // Debug info
    fun getVersionInfo(): String = """
        App Version: $VERSION_NAME
        Version Code: $VERSION_CODE
        Major: $MAJOR_VERSION, Minor: $MINOR_VERSION
        Is Dev: $IS_DEV
        Release Tag: $RELEASE_TAG
        User Agent: $USER_AGENT
    """.trimIndent()
}
