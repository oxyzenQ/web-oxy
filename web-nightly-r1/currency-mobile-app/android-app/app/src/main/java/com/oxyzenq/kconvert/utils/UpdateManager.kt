/*
 * Auto-Update Manager for GitHub Release Checking
 * Handles version checking, floating dialogs, and reminder system
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.content.SharedPreferences
import com.oxyzenq.kconvert.BuildConfig
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import androidx.appcompat.app.AlertDialog
import android.util.Log

/**
 * Manages automatic update checking and user notifications
 */
class UpdateManager(private val context: Context) {
    
    companion object {
        private const val TAG = "UpdateManager"
        private const val GITHUB_API_URL = "https://api.github.com/repos/oxyzenq/web-oxy/releases/latest"
        private const val GITHUB_RELEASES_URL = "https://github.com/oxyzenq/web-oxy/releases/latest"
        private const val REMINDER_INTERVAL_MS = 60_000L // 1 minute
        private const val PREF_REMINDER_ENABLED = "automatic_reminder_enabled"
        private const val PREF_LAST_DISMISSED_VERSION = "last_dismissed_version"
    }
    
    private val prefs = context.getSharedPreferences("kconvert_prefs", Context.MODE_PRIVATE)
    private var reminderHandler: Handler? = null
    private var updateJob: Job? = null
    
    /**
     * Check for updates asynchronously with proper error handling
     */
    fun checkForUpdates(onResult: (Boolean, String?, String?) -> Unit) {
        updateJob?.cancel()
        updateJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(GITHUB_API_URL)
                val connection = url.openConnection() as HttpURLConnection
                
                connection.apply {
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                    setRequestProperty("User-Agent", "Kconvert-Android-App")
                    connectTimeout = 10000
                    readTimeout = 10000
                }
                
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)
                    
                    val latestVersion = json.getString("tag_name").removePrefix("v")
                    val releaseNotes = json.optString("body", "")
                    val currentVersion = BuildConfig.VERSION_NAME
                    
                    Log.d(TAG, "Current: $currentVersion, Latest: $latestVersion")
                    
                    withContext(Dispatchers.Main) {
                        if (isNewerVersion(currentVersion, latestVersion)) {
                            onResult(true, latestVersion, releaseNotes)
                        } else {
                            onResult(false, null, null)
                        }
                    }
                } else {
                    Log.w(TAG, "GitHub API returned ${connection.responseCode}")
                    withContext(Dispatchers.Main) {
                        onResult(false, null, null)
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error checking for updates", e)
                withContext(Dispatchers.Main) {
                    onResult(false, null, null)
                }
            }
        }
    }
    
    /**
     * Start automatic reminder system after main screen loads
     */
    fun startAutomaticReminder(activity: Activity) {
        val reminderEnabled = prefs.getBoolean(PREF_REMINDER_ENABLED, true)
        if (!reminderEnabled) {
            Log.d(TAG, "Automatic reminder disabled by user")
            return
        }
        
        Log.d(TAG, "Starting automatic reminder system - will check in 1 minute")
        scheduleReminderCheck(activity)
    }
    
    /**
     * Schedule the first reminder check after 1 minute
     */
    private fun scheduleReminderCheck(activity: Activity) {
        stopReminders() // Clear any existing reminders
        
        reminderHandler = Handler(Looper.getMainLooper())
        reminderHandler?.postDelayed({
            checkAndShowReminder(activity)
        }, REMINDER_INTERVAL_MS)
    }
    
    /**
     * Check for updates and show reminder if needed
     */
    private fun checkAndShowReminder(activity: Activity) {
        val reminderEnabled = prefs.getBoolean(PREF_REMINDER_ENABLED, true)
        if (!reminderEnabled) {
            Log.d(TAG, "Reminder disabled, stopping checks")
            return
        }
        
        checkForUpdates { updateAvailable, version, releaseNotes ->
            if (updateAvailable && version != null) {
                val lastDismissedVersion = prefs.getString(PREF_LAST_DISMISSED_VERSION, "")
                
                // Don't show if user already dismissed this version
                if (version != lastDismissedVersion) {
                    showUpdateBottomSheet(activity, version, releaseNotes ?: "")
                } else {
                    // Schedule next check even if dismissed
                    scheduleNextReminder(activity)
                }
            } else {
                // No update available, schedule next check
                scheduleNextReminder(activity)
            }
        }
    }
    
    /**
     * Schedule next reminder check
     */
    private fun scheduleNextReminder(activity: Activity) {
        reminderHandler?.postDelayed({
            checkAndShowReminder(activity)
        }, REMINDER_INTERVAL_MS)
    }
    
    /**
     * Show modern update dialog using Compose
     */
    private fun showUpdateBottomSheet(activity: Activity, version: String, releaseNotes: String) {
        try {
            val message = buildString {
                append("🚀 New version $version is available!\n\n")
                if (releaseNotes.isNotEmpty()) {
                    append("What's new:\n")
                    append(releaseNotes.take(200))
                    if (releaseNotes.length > 200) append("...")
                }
            }
            
            // Use AlertDialog with modern styling for simplicity and stability
            AlertDialog.Builder(activity)
                .setTitle("✨ Kconvert Update Available")
                .setMessage(message)
                .setPositiveButton("🔄 Update Now") { _, _ ->
                    openGitHubReleases(activity)
                    stopReminders()
                }
                .setNegativeButton("⏰ Remind in 1 Min") { _, _ ->
                    // Schedule next reminder
                    scheduleNextReminder(activity)
                }
                .setNeutralButton("❌ Don't Ask Again") { _, _ ->
                    // Disable automatic reminder toggle
                    setReminderEnabled(false)
                    dismissVersionPermanently(version)
                    stopReminders()
                }
                .setCancelable(false)
                .show()
                
        } catch (e: Exception) {
            Log.e(TAG, "Error showing update dialog", e)
            // Fallback to simple notification
            Log.i(TAG, "Update available: $version")
        }
    }
    
    
    /**
     * Stop all reminder timers
     */
    fun stopReminders() {
        reminderHandler?.removeCallbacksAndMessages(null)
        reminderHandler = null
    }
    
    /**
     * Open GitHub releases page
     */
    private fun openGitHubReleases(activity: Activity) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_RELEASES_URL))
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening GitHub releases", e)
        }
    }
    
    /**
     * Mark version as permanently dismissed
     */
    private fun dismissVersionPermanently(version: String) {
        prefs.edit()
            .putString(PREF_LAST_DISMISSED_VERSION, version)
            .apply()
        Log.d(TAG, "Version $version dismissed permanently")
    }
    
    /**
     * Compare version strings (simple semantic versioning)
     */
    private fun isNewerVersion(current: String, latest: String): Boolean {
        return try {
            val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
            val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
            
            val maxLength = maxOf(currentParts.size, latestParts.size)
            
            for (i in 0 until maxLength) {
                val currentPart = currentParts.getOrNull(i) ?: 0
                val latestPart = latestParts.getOrNull(i) ?: 0
                
                when {
                    latestPart > currentPart -> return true
                    latestPart < currentPart -> return false
                }
            }
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error comparing versions: $current vs $latest", e)
            false
        }
    }
    
    /**
     * Get automatic reminder preference
     */
    fun isReminderEnabled(): Boolean {
        return prefs.getBoolean(PREF_REMINDER_ENABLED, true)
    }
    
    /**
     * Set automatic reminder preference
     */
    fun setReminderEnabled(enabled: Boolean) {
        prefs.edit()
            .putBoolean(PREF_REMINDER_ENABLED, enabled)
            .apply()
        
        if (!enabled) {
            stopReminders()
        }
        
        Log.d(TAG, "Automatic reminder ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        updateJob?.cancel()
        stopReminders()
    }
}
