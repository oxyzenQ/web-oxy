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
import androidx.fragment.app.FragmentActivity
import com.oxyzenq.kconvert.BuildConfig
import com.oxyzenq.kconvert.presentation.dialogs.PremiumUpdateDialogFragment
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.oxyzenq.kconvert.presentation.components.FloatingModal
import com.oxyzenq.kconvert.presentation.components.FloatingModalHeader
import com.oxyzenq.kconvert.ui.theme.CurrencyConverterTheme

/**
 * Manages automatic update checking and user notifications
 */
class UpdateManager(private val context: Context) {
    
    companion object {
        private const val TAG = "UpdateManager"
        private const val PREF_AUTOMATIC_REMINDER_ENABLED = "automatic_reminder_enabled"
        private const val PREF_LAST_DISMISSED_VERSION = "last_dismissed_version"
        private const val REMINDER_INTERVAL_MS = 10000L // 10 seconds for testing
        private const val GITHUB_API_URL = "https://api.github.com/repos/oxyzenq/web-oxy/releases/latest"
        private const val GITHUB_RELEASES_URL = "https://github.com/oxyzenq/web-oxy/releases"
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
     * Start automatic reminder system after main screen is shown
     */
    fun startAutomaticReminders(activity: Activity) {
        val reminderEnabled = prefs.getBoolean(PREF_AUTOMATIC_REMINDER_ENABLED, true) // Default ON
        Log.d(TAG, "startAutomaticReminders called - reminderEnabled: $reminderEnabled")
        
        if (!reminderEnabled) {
            Log.d(TAG, "Automatic reminder disabled by user")
            return
        }
        
        Log.d(TAG, "Starting automatic reminders with 10-second interval")
        scheduleNextReminder(activity)
    }
    
    /**
     * Schedule next reminder check after 10 seconds
     */
    private fun scheduleNextReminder(activity: Activity) {
        // Stop any existing reminders first
        stopReminders()
        
        reminderHandler = Handler(Looper.getMainLooper())
        Log.d(TAG, "scheduleNextReminder: Setting timer for 10000ms (10 seconds)")
        reminderHandler?.postDelayed({
            Log.d(TAG, "Timer triggered - calling checkAndShowReminderIfNeeded")
            checkAndShowReminderIfNeeded(activity)
        }, REMINDER_INTERVAL_MS)
    }
    
    /**
     * Check for updates and show reminder if needed
     */
    private fun checkAndShowReminderIfNeeded(activity: Activity) {
        // Check if activity is still valid
        if (activity.isFinishing || activity.isDestroyed) {
            Log.d(TAG, "Activity is finishing/destroyed, stopping reminders")
            stopReminders()
            return
        }
        
        val reminderEnabled = prefs.getBoolean(PREF_AUTOMATIC_REMINDER_ENABLED, true) // Default ON
        Log.d(TAG, "checkAndShowReminderIfNeeded: reminderEnabled=$reminderEnabled")
        
        if (!reminderEnabled) {
            Log.d(TAG, "Automatic reminder disabled by user")
            return
        }
        
        Log.d(TAG, "Starting GitHub API check for updates...")
        checkForUpdates { updateAvailable, version, releaseNotes ->
            // Double-check activity is still valid when callback returns
            if (activity.isFinishing || activity.isDestroyed) {
                Log.d(TAG, "Activity finished during API call, stopping reminders")
                stopReminders()
                return@checkForUpdates
            }
            
            Log.d(TAG, "GitHub API response: updateAvailable=$updateAvailable, version=$version")
            if (updateAvailable && version != null) {
                Log.d(TAG, "Update available! Showing dialog for version: $version")
                showFloatingUpdateDialog(activity, version, releaseNotes ?: "")
            } else {
                Log.d(TAG, "No update available, scheduling next check in 10 seconds")
                scheduleNextReminder(activity)
            }
        }
    }
    
    /**
     * Show premium glassmorphism floating update dialog
     */
    private fun showFloatingUpdateDialog(activity: Activity, version: String, releaseNotes: String) {
        try {
            // Use DialogFragment with XML layout for stable glassmorphism style
            if (activity is androidx.fragment.app.FragmentActivity) {
                val dialogFragment = PremiumUpdateDialogFragment.newInstance(version, releaseNotes)
                dialogFragment.setUpdateListener(object : PremiumUpdateDialogFragment.UpdateListener {
                    override fun onUpdateNow() {
                        openGitHubReleases(activity)
                        stopReminders()
                    }
                    
                    override fun onRejectUpdate() {
                        // Reject only for this cycle, show again after 10s
                        scheduleNextReminder(activity)
                    }
                    
                    override fun onDontAskAgain() {
                        // Turn OFF toggle and stop reminders
                        setAutomaticReminderEnabled(false)
                        stopReminders()
                    }
                })
                dialogFragment.show(activity.supportFragmentManager, "PremiumUpdateDialog")
            } else {
                // Fallback for non-FragmentActivity
                showSimpleUpdateDialog(activity, version, releaseNotes)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing glassmorphism dialog, falling back", e)
            showSimpleUpdateDialog(activity, version, releaseNotes)
        }
    }
    
    /**
     * Show stable glassmorphism dialog with delayed initialization
     */
    private fun showStableGlassmorphismDialog(activity: Activity, version: String, releaseNotes: String) {
        try {
            // Use Handler to delay dialog creation and avoid lifecycle issues
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    val composeView = ComposeView(activity)
                    var isVisible by mutableStateOf(true)
                    
                    composeView.setContent {
                        CurrencyConverterTheme {
                            if (isVisible) {
                                PremiumUpdateDialog(
                                    version = version,
                                    releaseNotes = releaseNotes,
                                    onUpdateNow = {
                                        isVisible = false
                                        openGitHubReleases(activity)
                                        stopReminders()
                                    },
                                    onRemindLater = {
                                        isVisible = false
                                        scheduleNextReminder(activity)
                                    },
                                    onDontAskAgain = {
                                        isVisible = false
                                        dismissVersionPermanently(version)
                                        setAutomaticReminderEnabled(false)
                                        stopReminders()
                                    }
                                )
                            }
                        }
                    }
                    
                    val dialog = android.app.Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar)
                    dialog.setContentView(composeView)
                    dialog.setCancelable(false)
                    dialog.show()
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error in delayed glassmorphism dialog", e)
                    showSimpleUpdateDialog(activity, version, releaseNotes)
                }
            }, 500) // 500ms delay to ensure activity is fully initialized
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing stable glassmorphism dialog", e)
            showSimpleUpdateDialog(activity, version, releaseNotes)
        }
    }
    
    /**
     * Fallback simple dialog if Compose fails
     */
    private fun showSimpleUpdateDialog(activity: Activity, version: String, releaseNotes: String) {
        try {
            val builder = android.app.AlertDialog.Builder(activity)
            builder.setTitle("🚀 Kconvert Update Available")
            builder.setMessage("Version $version is ready to install!\n\n${releaseNotes.take(100)}")
            builder.setCancelable(false)
            
            builder.setPositiveButton("Update Now") { dialog, _ ->
                dialog.dismiss()
                openGitHubReleases(activity)
                stopReminders()
            }
            
            builder.setNeutralButton("Remind Later") { dialog, _ ->
                dialog.dismiss()
                scheduleNextReminder(activity)
            }
            
            builder.setNegativeButton("Don't Ask Again") { dialog, _ ->
                dialog.dismiss()
                dismissVersionPermanently(version)
                setAutomaticReminderEnabled(false)
                stopReminders()
            }
            
            val dialog = builder.create()
            dialog.show()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing fallback dialog", e)
        }
    }
    
    /**
     * Legacy method - keeping for reference but using simple dialog instead
     */
    private fun showFloatingUpdateDialogCompose(activity: Activity, version: String, releaseNotes: String) {
        try {
            val composeView = ComposeView(activity)
            var isVisible by mutableStateOf(true)
            
            composeView.setContent {
                CurrencyConverterTheme {
                    if (isVisible) {
                        PremiumUpdateDialog(
                            version = version,
                            releaseNotes = releaseNotes,
                            onUpdateNow = {
                                isVisible = false
                                openGitHubReleases(activity)
                                stopReminders()
                            },
                            onRemindLater = {
                                isVisible = false
                                scheduleNextReminder(activity)
                            },
                            onDontAskAgain = {
                                isVisible = false
                                dismissVersionPermanently(version)
                                setAutomaticReminderEnabled(false)
                                stopReminders()
                            }
                        )
                    }
                }
            }
            
            val dialog = android.app.Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar)
            dialog.setContentView(composeView)
            dialog.setCancelable(false)
            dialog.show()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing update dialog", e)
        }
    }
    
    /**
     * Premium glassmorphism update dialog component with dynamic height
     */
    @Composable
    private fun PremiumUpdateDialog(
        version: String,
        releaseNotes: String,
        onUpdateNow: () -> Unit,
        onRemindLater: () -> Unit,
        onDontAskAgain: () -> Unit
    ) {
        // Calculate dynamic width and height based on content
        val dynamicWidth = if (releaseNotes.length > 100) 360.dp else 320.dp
        val hasReleaseNotes = releaseNotes.isNotEmpty()
        
        FloatingModal(
            visible = true,
            onDismiss = onRemindLater,
            width = dynamicWidth,
            strictModal = true,
            header = {
                FloatingModalHeader(
                    title = "Kconvert Update Available",
                    subtitle = "Version $version is ready to install",
                    icon = Icons.Default.SystemUpdate,
                    iconTint = Color(0xFF3B82F6)
                )
            }
        ) {
            // Release notes section with dynamic content
            if (hasReleaseNotes) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "What's New:",
                        style = MaterialTheme.typography.subtitle2.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    
                    // Dynamic text display based on content length
                    val displayText = when {
                        releaseNotes.length <= 100 -> releaseNotes
                        releaseNotes.length <= 200 -> releaseNotes.take(180) + "..."
                        else -> releaseNotes.take(250) + "..."
                    }
                    
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.body2.copy(
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        ),
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 40.dp, max = 120.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Action buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Update Now button (primary)
                Button(
                    onClick = onUpdateNow,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF3B82F6),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.elevation(0.dp)
                ) {
                    Text(
                        text = "🔄 Update Now",
                        style = MaterialTheme.typography.button.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                
                // Remind Later button (secondary)
                Button(
                    onClick = onRemindLater,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF374151).copy(alpha = 0.7f),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.elevation(0.dp)
                ) {
                    Text(
                        text = "⏰ Remind Later",
                        style = MaterialTheme.typography.button
                    )
                }
                
                // Don't Ask Again button (tertiary)
                TextButton(
                    onClick = onDontAskAgain,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "❌ Don't Ask Again",
                        style = MaterialTheme.typography.button.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                    )
                }
            }
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
     * Check if automatic reminders are enabled
     */
    fun isAutomaticReminderEnabled(): Boolean {
        return prefs.getBoolean(PREF_AUTOMATIC_REMINDER_ENABLED, true) // Default ON as requested
    }
    
    /**
     * Set automatic reminder preference
     */
    fun setAutomaticReminderEnabled(enabled: Boolean, activity: Activity? = null) {
        Log.d(TAG, "setAutomaticReminderEnabled: $enabled")
        prefs.edit()
            .putBoolean(PREF_AUTOMATIC_REMINDER_ENABLED, enabled)
            .apply()
        
        if (!enabled) {
            stopReminders()
            Log.d(TAG, "Automatic reminders disabled and stopped")
        } else {
            // When toggle is turned ON, reset dismissals (ignore previous "Don't Ask Again")
            Log.d(TAG, "Automatic reminders enabled - resetting dismissals for fresh start")
            
            if (activity != null) {
                // Immediately start reminders when enabled with activity context
                Log.d(TAG, "Starting automatic reminders immediately")
                startAutomaticReminders(activity)
            }
        }
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        updateJob?.cancel()
        stopReminders()
    }
}
