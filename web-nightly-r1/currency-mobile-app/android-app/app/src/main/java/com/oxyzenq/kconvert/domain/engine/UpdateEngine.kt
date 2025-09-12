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
import com.oxyzenq.kconvert.data.remote.service.GithubService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateEngine @Inject constructor(
    private val githubService: GithubService,
    private val notifyDao: NotifyDao,
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
    suspend fun checkOnceAndNotifyIfNeeded() {
        if (!prefs.getBoolean(KEY_AUTO_REMIND, true)) return
        
        // Prevent multiple concurrent checks
        if (!isChecking.compareAndSet(false, true)) return
        
        try {
            // Check if already ran this session
            val lastCheckSession = prefs.getLong(KEY_LAST_CHECK_SESSION, 0)
            if (lastCheckSession == currentSessionId) return
            
            withContext(Dispatchers.IO) {
                performUpdateCheck()
                // Mark this session as checked
                prefs.edit().putLong(KEY_LAST_CHECK_SESSION, currentSessionId).apply()
            }
        } finally {
            isChecking.set(false)
        }
    }

    private suspend fun performUpdateCheck() {
        try {
            val response = githubService.getLatestRelease()
            
            if (!response.isSuccessful) {
                insertErrorMessage("Cannot check update, please fix internet!")
                return
            }
            
            val release = response.body() ?: run {
                insertErrorMessage("Cannot check update, unexpected response")
                return
            }
            
            val latestVersion = release.tagName
            val currentVersion = BuildConfig.VERSION_NAME
            
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
            } else {
                // Optional: insert "up to date" message
                // val message = NotifyMessage(
                //     body = "Using the latest version. Great!",
                //     messageType = "INFO"
                // )
                // notifyDao.insert(message)
            }
        } catch (e: Exception) {
            insertErrorMessage("Cannot check update, please fix internet!")
        }
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
    suspend fun performManualCheck() {
        if (!isChecking.compareAndSet(false, true)) return
        
        try {
            withContext(Dispatchers.IO) {
                performUpdateCheck()
            }
        } finally {
            isChecking.set(false)
        }
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
