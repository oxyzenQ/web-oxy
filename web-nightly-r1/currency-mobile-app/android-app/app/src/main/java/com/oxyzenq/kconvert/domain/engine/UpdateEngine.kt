/*
 * Update Engine - Single-check on MainScreen with Event-based Logic
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.domain.engine

import android.content.Context
import android.content.SharedPreferences
import com.oxyzenq.kconvert.AppVersion
import com.oxyzenq.kconvert.BuildConfig
import com.oxyzenq.kconvert.data.local.dao.NotifyDao
import com.oxyzenq.kconvert.data.local.entity.NotifyMessage
import com.oxyzenq.kconvert.data.local.SettingsDataStore
import com.oxyzenq.kconvert.data.remote.service.GithubService
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateEngine @Inject constructor(
    private val githubService: GithubService,
    private val notifyDao: NotifyDao,
    private val settingsDataStore: SettingsDataStore,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val KEY_AUTO_REMIND = "auto_remind_enabled"
        private const val KEY_LAST_CHECK_SESSION = "last_check_session"
    }

    private val isChecking = AtomicBoolean(false)
    private var currentSessionId = System.currentTimeMillis()
    private val prefs: SharedPreferences = context.getSharedPreferences("update_engine_prefs", Context.MODE_PRIVATE)

    /**
     * Single check on MainScreen entry - runs once per session
     */
    suspend fun checkOnceAndNotifyIfNeeded(): UpdateCheckResult {
        if (!prefs.getBoolean(KEY_AUTO_REMIND, true)) return UpdateCheckResult.DISABLED
        
        // Prevent multiple concurrent checks
        if (!isChecking.compareAndSet(false, true)) return UpdateCheckResult.ALREADY_CHECKING
        
        try {
            // Check if already ran this session
            val lastCheckSession = prefs.getLong(KEY_LAST_CHECK_SESSION, 0)
            if (lastCheckSession == currentSessionId) return UpdateCheckResult.ALREADY_CHECKED
            
            val result = withContext(Dispatchers.IO) {
                performUpdateCheck()
            }
            
            // Mark this session as checked
            prefs.edit().putLong(KEY_LAST_CHECK_SESSION, currentSessionId).apply()
            return result
        } finally {
            isChecking.set(false)
        }
    }

    private suspend fun performUpdateCheck(): UpdateCheckResult {
        try {
            val response = githubService.getLatestRelease()
            
            if (!response.isSuccessful) {
                insertErrorMessage("Cannot check update, please fix internet!")
                return UpdateCheckResult.ERROR
            }
            
            val release = response.body() ?: run {
                insertErrorMessage("Cannot check update, unexpected response")
                return UpdateCheckResult.ERROR
            }
            
            val latestVersion = release.tagName
            val currentVersion = BuildConfig.VERSION_NAME
            
            // Check for version mismatch (different naming schemes)
            val isVersionMismatch = detectVersionMismatch(latestVersion, currentVersion)
            
            if (isVersionMismatch) {
                val message = NotifyMessage(
                    title = "Version Mismatch Detected",
                    body = "Caution: Version mismatch detected. Current version differs from repository standard. Please verify your app source.",
                    releaseUrl = release.htmlUrl,
                    messageType = "WARNING"
                )
                notifyDao.insert(message)
                updateLastCheckTimestamp()
                return UpdateCheckResult.VERSION_MISMATCH
            }
            
            if (!latestVersion.equals(currentVersion, ignoreCase = true)) {
                val shortBody = truncateBody(release.body)
                val messageText = "Update available: $latestVersion > $currentVersion"
                
                val message = NotifyMessage(
                    title = messageText,
                    body = shortBody,
                    releaseUrl = release.htmlUrl,
                    messageType = "UPDATE"
                )
                notifyDao.insert(message)
                updateLastCheckTimestamp()
                return UpdateCheckResult.UPDATE_AVAILABLE
            } else {
                val message = NotifyMessage(
                    title = "Up to Date",
                    body = "You are using the latest version ($currentVersion). No updates available.",
                    messageType = "INFO"
                )
                notifyDao.insert(message)
                return UpdateCheckResult.UP_TO_DATE
            }
        } catch (e: Exception) {
            insertErrorMessage("Cannot check update, please fix internet!")
            return UpdateCheckResult.ERROR
        }
    }
    
    private fun detectVersionMismatch(latestVersion: String, currentVersion: String): Boolean {
        // Check if versions follow different naming patterns
        val latestPattern = when {
            latestVersion.contains("Stellar-") -> "stellar"
            latestVersion.matches(Regex("^\\d+\\.\\d+(\\.\\d+)?$")) -> "semantic"
            latestVersion.contains(".dev") -> "development"
            else -> "unknown"
        }
        
        val currentPattern = when {
            currentVersion.contains("Stellar-") -> "stellar"
            currentVersion.matches(Regex("^\\d+\\.\\d+(\\.\\d+)?$")) -> "semantic"
            currentVersion.contains(".dev") -> "development"
            else -> "unknown"
        }
        
        return latestPattern != currentPattern
    }

    private suspend fun insertErrorMessage(text: String) {
        val message = NotifyMessage(
            body = text,
            messageType = "ERROR"
        )
        notifyDao.insert(message)
    }

    private fun truncateBody(body: String?): String {
        if (body.isNullOrBlank()) return ""
        
        val words = body.trim().split("\\s+".toRegex())
        return if (words.size <= 100) {
            body
        } else {
            words.take(100).joinToString(" ") + "..."
        }
    }

    /**
     * Manual check triggered by user
     */
    suspend fun performManualCheck(): UpdateCheckResult {
        if (!isChecking.compareAndSet(false, true)) return UpdateCheckResult.ALREADY_CHECKING
        
        try {
            val result = withContext(Dispatchers.IO) {
                performUpdateCheck()
            }
            // Always update timestamp for manual checks
            updateLastCheckTimestamp()
            return result
        } finally {
            isChecking.set(false)
        }
    }

    /**
     * Update last check timestamp in DataStore
     */
    private suspend fun updateLastCheckTimestamp() {
        val formatter = SimpleDateFormat("HH:mm dd/MM/yy", Locale.getDefault())
        val timestamp = formatter.format(Date())
        settingsDataStore.setLastUpdateCheck(timestamp)
    }

    /**
     * Get auto-remind setting
     */
    fun isAutoRemindEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_REMIND, true)
    }

    /**
     * Set auto-remind setting
     */
    fun setAutoRemindEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_REMIND, enabled).apply()
        if (!enabled) {
            // Reset session to allow fresh check when re-enabled
            currentSessionId = System.currentTimeMillis()
        }
    }

    /**
     * Reset session for fresh check
     */
    fun resetSession() {
        currentSessionId = System.currentTimeMillis()
        prefs.edit().remove(KEY_LAST_CHECK_SESSION).apply()
    }
}

enum class UpdateCheckResult {
    UPDATE_AVAILABLE,
    UP_TO_DATE,
    VERSION_MISMATCH,
    ERROR,
    DISABLED,
    ALREADY_CHECKING,
    ALREADY_CHECKED
}
