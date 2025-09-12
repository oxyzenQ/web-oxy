/*
 * Notification ViewModel for Premium Update System
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oxyzenq.kconvert.data.local.dao.NotifyDao
import com.oxyzenq.kconvert.data.local.entity.NotifyMessage
import com.oxyzenq.kconvert.domain.engine.UpdateEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notifyDao: NotifyDao,
    private val updateEngine: UpdateEngine
) : ViewModel() {

    // Observe all notifications
    val notifications = notifyDao.observeAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Observe notification count for badge
    val notificationCount = notifyDao.getUnreadCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // UI state for bottom sheet
    private val _isBottomSheetVisible = MutableStateFlow(false)
    val isBottomSheetVisible = _isBottomSheetVisible.asStateFlow()

    // UI state for changelog dialog
    private val _selectedMessage = MutableStateFlow<NotifyMessage?>(null)
    val selectedMessage = _selectedMessage.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    /**
     * Show notification bottom sheet
     */
    fun showBottomSheet() {
        _isBottomSheetVisible.value = true
    }

    /**
     * Hide notification bottom sheet
     */
    fun hideBottomSheet() {
        _isBottomSheetVisible.value = false
    }

    /**
     * Show changelog dialog for specific message
     */
    fun showChangelogDialog(message: NotifyMessage) {
        _selectedMessage.value = message
        markAsRead(message.id)
    }

    /**
     * Hide changelog dialog
     */
    fun hideChangelogDialog() {
        _selectedMessage.value = null
    }


    /**
     * Clear all notifications
     */
    fun clearAllNotifications() {
        viewModelScope.launch {
            try {
                notifyDao.deleteAll()
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    /**
     * Perform manual update check
     */
    fun performManualCheck() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                updateEngine.performManualCheck()
            } catch (e: Exception) {
                // Error handling is done in UpdateEngine
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Get auto-remind setting as Flow
     */
    val autoRemindEnabled = flow {
        emit(updateEngine.isAutoRemindEnabled())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    /**
     * Set auto-remind setting
     */
    fun setAutoRemindEnabled(enabled: Boolean) {
        updateEngine.setAutoRemindEnabled(enabled)
    }

    /**
     * Mark all notifications as read
     */
    fun markAllAsRead() {
        viewModelScope.launch {
            notifyDao.markAllAsRead()
        }
    }

    /**
     * Mark specific notification as read
     */
    private fun markAsRead(messageId: Long) {
        viewModelScope.launch {
            notifyDao.markAsRead(messageId)
        }
    }

    /**
     * Delete notification message
     */
    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            notifyDao.deleteById(messageId)
        }
    }

    /**
     * Handle "Don't ask again" action
     */
    fun handleDontAskAgain() {
        setAutoRemindEnabled(false)
        hideChangelogDialog()
    }

    /**
     * Trigger update check on main screen entry
     */
    fun checkForUpdatesOnMainScreen() {
        viewModelScope.launch {
            try {
                updateEngine.checkOnceAndNotifyIfNeeded()
            } catch (e: Exception) {
                // Error handling is done in UpdateEngine
            }
        }
    }
}
