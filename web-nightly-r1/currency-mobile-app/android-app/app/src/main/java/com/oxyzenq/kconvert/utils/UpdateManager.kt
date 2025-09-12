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
        private const val REMINDER_INTERVAL_MS = 10_000L // 10 seconds per requirement
        private const val PREF_AUTO_UPDATE_ENABLED = "auto_update_enabled" // single toggle, default ON
        private const val PREF_LAST_DISMISSED_VERSION = "last_dismissed_version"
    }
    
    private val prefs = context.getSharedPreferences("kconvert_prefs", Context.MODE_PRIVATE)
    private var reminderHandler: Handler? = null
    private var updateJob: Job? = null
    private var isDialogShowing: Boolean = false
    private var engineRunnable: Runnable? = null
    
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
     * Main entry point - check and show update reminder if needed
     */
    fun maybeShowUpdateReminder(activity: Activity) {
        // Backwards compatibility: now delegates to engine start
        startEngine(activity)
    }
    
    /**
     * Show premium floating update dialog
     */
    private fun showFloatingUpdateDialog(activity: Activity, version: String, releaseNotes: String) {
        try {
            if (isDialogShowing) return
            val message = buildString {
                append("🚀 New version $version is available!\n\n")
                if (releaseNotes.isNotEmpty()) {
                    append("What's new:\n")
                    append(releaseNotes.take(200))
                    if (releaseNotes.length > 200) append("...")
                }
            }
            
            isDialogShowing = true
            AlertDialog.Builder(activity)
                .setTitle("✨ Kconvert Update Available")
                .setMessage(message)
                .setPositiveButton("🔄 Update Now") { _, _ ->
                    openGitHubReleases(activity)
                    // Keep engine running; user may come back if not updated
                    isDialogShowing = false
                }
                .setNegativeButton("⏰ Reject update") { _, _ ->
                    // Dismiss now; engine will check again in 10s and show if still available
                    isDialogShowing = false
                }
                .setNeutralButton("❌ Don't ask again") { _, _ ->
                    // Turn OFF the automatic reminder toggle and stop engine
                    setAutoUpdateEnabled(false)
                    stopEngine()
                    isDialogShowing = false
                }
                .setCancelable(false)
                .show()
                
        } catch (e: Exception) {
            Log.e(TAG, "Error showing update dialog", e)
        }
    }
    
    /**
     * Start the 10s background reminder engine. Safe to call multiple times.
     */
    fun startEngine(activity: Activity) {
        val enabled = isAutoUpdateEnabled()
        if (!enabled) {
            Log.d(TAG, "Update engine not started (disabled)")
            return
        }
        if (reminderHandler == null) {
            reminderHandler = Handler(Looper.getMainLooper())
        }
        // Cancel existing runnable to avoid duplicates
        engineRunnable?.let { reminderHandler?.removeCallbacks(it) }
        engineRunnable = object : Runnable {
            override fun run() {
                if (!isAutoUpdateEnabled()) {
                    // Stop if user disabled
                    stopEngine()
                    return
                }
                checkForUpdates { updateAvailable, version, releaseNotes ->
                    if (updateAvailable && version != null) {
                        showFloatingUpdateDialog(activity, version, releaseNotes ?: "")
                    }
                    // Schedule next tick regardless of result
                    reminderHandler?.postDelayed(this, REMINDER_INTERVAL_MS)
                }
            }
        }
        // Kick off immediately after UI is shown
        reminderHandler?.postDelayed(engineRunnable!!, REMINDER_INTERVAL_MS)
        Log.d(TAG, "Update engine started (every ${REMINDER_INTERVAL_MS}ms)")
    }
    
    /**
     * Stop all reminder timers
     */
    fun stopEngine() {
        engineRunnable?.let { reminderHandler?.removeCallbacks(it) }
        engineRunnable = null
        isDialogShowing = false
        Log.d(TAG, "Update engine stopped")
    }

    fun restartEngine(activity: Activity) {
        stopEngine()
        startEngine(activity)
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
        // No longer used: behavior changed to flexible "don't ask again" by disabling toggle
        Log.d(TAG, "dismissVersionPermanently() deprecated. Version=$version")
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
     * Get auto-update preference
     */
    fun isAutoUpdateEnabled(): Boolean {
        return prefs.getBoolean(PREF_AUTO_UPDATE_ENABLED, true)
    }
    
    /**
     * Set auto-update preference
     */
    fun setAutoUpdateEnabled(enabled: Boolean) {
        prefs.edit()
            .putBoolean(PREF_AUTO_UPDATE_ENABLED, enabled)
            .apply()
        
        // Engine lifecycle managed by caller via start/stop/restart
        
        Log.d(TAG, "Auto-update ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        updateJob?.cancel()
        stopEngine()
    }
}
