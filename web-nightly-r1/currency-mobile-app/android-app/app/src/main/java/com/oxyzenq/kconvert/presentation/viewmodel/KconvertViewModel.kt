/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oxyzenq.kconvert.data.preferences.AppPreferences
import com.oxyzenq.kconvert.data.repository.CurrencyRepository
import com.oxyzenq.kconvert.data.repository.UpdateRepository
import android.content.Context
import com.oxyzenq.kconvert.data.repository.ConversionResult
import com.oxyzenq.kconvert.data.model.Currency
import com.oxyzenq.kconvert.AppVersion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main ViewModel for Kconvert app
 * Handles all business logic for currency conversion and app settings
 */
@HiltViewModel
class KconvertViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository,
    private val appPreferences: AppPreferences,
    private val updateRepository: UpdateRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(KconvertUiState())
    val uiState: StateFlow<KconvertUiState> = _uiState.asStateFlow()

    // Currencies from database
    val currencies: StateFlow<List<Currency>> = currencyRepository.getAllCurrencies()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    // Haptics enabled setting
    val hapticsEnabled: StateFlow<Boolean> = appPreferences.hapticsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    init {
        // Initial setup - context will be provided via initializeApp()
    }

    /**
     * Load initial data and check auto update setting
     */
    fun initializeApp(context: Context) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Get currency count to check if data exists
                val currencyCount = currencyRepository.getCurrencyCount()
                val lastUpdate = currencyRepository.getLastUpdateTimestamp()
                
                _uiState.value = _uiState.value.copy(
                    dataIndicator = if (currencyCount > 0) {
                        "this data has been updated last seen $lastUpdate"
                    } else {
                        "null, no data please click button 'refresh data of price' to update/fetching"
                    },
                    isLoading = false
                )

                // Check auto update setting and sync data if needed
                val autoUpdate = appPreferences.getAutoUpdateOnLaunch()
                if (autoUpdate) {
                    if (currencyCount == 0) {
                        // First launch - fetch data
                        refreshData(context)
                    } else {
                        // Check if data needs refresh (smart sync)
                        val syncResult = currencyRepository.syncDataIfNeeded(context)
                        syncResult.fold(
                            onSuccess = { message ->
                                if (message != "Data is up to date") {
                                    val newLastUpdate = currencyRepository.getLastUpdateTimestamp()
                                    _uiState.value = _uiState.value.copy(
                                        dataIndicator = "this data has been updated last seen $newLastUpdate"
                                    )
                                    showNotification("Auto-sync completed", NotificationType.SUCCESS)
                                }
                            },
                            onFailure = { 
                                // Silent fail for auto-sync, continue with cached data
                            }
                        )
                    }
                    
                }
                
                // Check for version updates and show welcome dialog
                checkForVersionUpdate()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load initial data: ${e.message}"
                )
            }
        }
    }

    /**
     * Check for app updates automatically on launch
     */
    private fun checkForUpdatesAutomatically() {
        viewModelScope.launch {
            try {
                val result = updateRepository.getLatestRelease()
                result.fold(
                    onSuccess = { release ->
                        val latestVersion = release.tag_name
                        val currentVersion = AppVersion.VERSION_NAME
                        
                        val comparison = updateRepository.compareVersions(latestVersion, currentVersion)
                        val updateMsg = updateRepository.generateUpdateMessage(latestVersion, currentVersion, comparison)
                        
                        // Show update dialog if there's an update available or version mismatch
                        if (comparison == com.oxyzenq.kconvert.data.repository.VersionComparison.NEWER_AVAILABLE || 
                            updateMsg.isWarning) {
                            _uiState.value = _uiState.value.copy(
                                updateDialog = UpdateDialogState(
                                    isVisible = true,
                                    isOutdated = comparison == com.oxyzenq.kconvert.data.repository.VersionComparison.NEWER_AVAILABLE,
                                    updateError = false,
                                    latestVersion = latestVersion,
                                    updateMessage = updateMsg.message,
                                    updateTitle = updateMsg.title,
                                    isWarning = updateMsg.isWarning,
                                    showGitHubLink = updateMsg.showGitHubLink,
                                    uiState = updateMsg.uiState
                                )
                            )
                        }
                    },
                    onFailure = { 
                        // Silent fail for automatic update check - don't show error dialog on launch
                        // User can still manually check for updates if needed
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to check for updates: ${e.message}"
                )
            }
        }
    }

    /**
     * Check for version updates and show welcome dialog
     */
    private fun checkForVersionUpdate() {
        viewModelScope.launch {
            try {
                val currentVersion = AppVersion.VERSION_NAME
                val lastKnownVersion = appPreferences.getLastKnownVersion()
                
                when {
                    lastKnownVersion == null -> {
                        // First install - show welcome dialog for new users
                        _uiState.value = _uiState.value.copy(
                            welcomeDialog = WelcomeDialogState(
                                isVisible = true,
                                currentVersion = currentVersion,
                                previousVersion = "",
                                isFirstInstall = true
                            )
                        )
                        // Save current version
                        appPreferences.setLastKnownVersion(currentVersion)
                    }
                    lastKnownVersion != currentVersion -> {
                        // App was updated - show congratulations dialog
                        _uiState.value = _uiState.value.copy(
                            welcomeDialog = WelcomeDialogState(
                                isVisible = true,
                                currentVersion = currentVersion,
                                previousVersion = lastKnownVersion,
                                isFirstInstall = false
                            )
                        )
                        // Save new version
                        appPreferences.setLastKnownVersion(currentVersion)
                    }
                    // else: same version, no dialog needed
                }
            } catch (e: Exception) {
                // Silent fail for version check
            }
        }
    }

    /**
     * Dismiss update dialog
     */
    fun dismissUpdateDialog() {
        _uiState.value = _uiState.value.copy(
            updateDialog = UpdateDialogState()
        )
    }

    /**
     * Dismiss welcome dialog
     */
    fun dismissWelcomeDialog() {
        _uiState.value = _uiState.value.copy(
            welcomeDialog = WelcomeDialogState()
        )
    }

    /**
     * Set haptics enabled preference
     */
    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                appPreferences.setHapticsEnabled(enabled)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update haptics setting: ${e.message}"
                )
            }
        }
    }

    /**
     * Update amount input
     */
    fun updateAmount(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }

    /**
     * Select source currency
     */
    fun selectSourceCurrency(currency: Currency) {
        _uiState.value = _uiState.value.copy(sourceCurrency = currency)
    }

    /**
     * Select target currency
     */
    fun selectTargetCurrency(currency: Currency) {
        _uiState.value = _uiState.value.copy(targetCurrency = currency)
    }

    /**
     * Perform currency conversion
     */
    fun convertCurrency() {
        val currentState = _uiState.value
        val amount = currentState.amount.toDoubleOrNull()
        val sourceCurrency = currentState.sourceCurrency
        val targetCurrency = currentState.targetCurrency

        if (amount == null || amount <= 0) {
            showNotification("Please enter a valid amount", NotificationType.WARNING)
            return
        }

        if (sourceCurrency == null || targetCurrency == null) {
            showNotification("Please select both currencies", NotificationType.WARNING)
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isConverting = true)
                
                val result = currencyRepository.convertCurrency(
                    sourceCurrency.code,
                    targetCurrency.code,
                    amount
                )

                result.fold(
                    onSuccess = { conversionResult ->
                        _uiState.value = _uiState.value.copy(
                            conversionResult = conversionResult,
                            isConverting = false,
                            error = null
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isConverting = false,
                            error = "Conversion failed: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isConverting = false,
                    error = "Conversion error: ${e.message}"
                )
            }
        }
    }

    /**
     * Refresh currency data from API
     */
    fun refreshData(context: Context) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isRefreshing = true)
                
                val result = currencyRepository.fetchAndSaveCurrencyData(context)
                
                if (result.isSuccess) {
                    // Force reload currencies and update UI state
                    loadCurrencies()
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        dataIndicator = "Data refreshed successfully at ${System.currentTimeMillis()}",
                        error = null
                    )
                    showNotification("Ultra-secure data refreshed successfully", NotificationType.SUCCESS)
                } else {
                    showNotification("Failed to refresh data: ${result.exceptionOrNull()?.message}", NotificationType.ERROR)
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        error = "Failed to refresh data: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = "Refresh error: ${e.message}"
                )
            }
        }
    }

    /**
     * Load currencies from repository
     */
    private fun loadCurrencies() {
        viewModelScope.launch {
            try {
                currencyRepository.getAllCurrencies()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = "Failed to load currencies: ${e.message}"
                )
            }
        }
    }

    /**
     * Delete all currency data
     */
    fun deleteAllData() {
        viewModelScope.launch {
            try {
                val result = currencyRepository.deleteAllCurrencyData()
                
                result.fold(
                    onSuccess = { _ ->
                        _uiState.value = _uiState.value.copy(
                            dataIndicator = "null, no data please click button 'refresh data of price' to update/fetching",
                            conversionResult = null,
                            error = null
                        )
                        showNotification("Successfully delete old data", NotificationType.SUCCESS)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to delete data: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Delete error: ${e.message}"
                )
            }
        }
    }

    /**
     * Refresh app (reset inputs and scroll to top)
     */
    fun refreshApp() {
        _uiState.value = _uiState.value.copy(
            amount = "0",
            sourceCurrency = null,
            targetCurrency = null,
            conversionResult = null,
            error = null,
            shouldScrollToTop = true
        )
        showNotification("Successfully refresh the app", NotificationType.SUCCESS)
    }

    /**
     * Mark scroll to top as handled
     */
    fun onScrollToTopHandled() {
        _uiState.value = _uiState.value.copy(shouldScrollToTop = false)
    }

    /**
     * Show confirmation dialog
     */
    fun showConfirmationDialog(type: ConfirmationType) {
        val (title, _) = when (type) {
            ConfirmationType.REFRESH_DATA -> "Refresh Data" to "Are you sure you want to refresh all currency data? This will fetch the latest exchange rates."
            ConfirmationType.DELETE_DATA -> "Delete All Data" to "Are you sure you want to delete all stored currency data? This action cannot be undone."
            ConfirmationType.EXIT_APP -> "Exit App Kconvert?" to "Are you sure you want to exit the application?"
        }
        _uiState.value = _uiState.value.copy(
            confirmationDialog = ConfirmationDialogState(
                isVisible = true,
                type = type,
                title = title
            )
        )
    }

    /**
     * Confirmation dialog state
     */
    fun hideConfirmationDialog() {
        _uiState.value = _uiState.value.copy(
            confirmationDialog = _uiState.value.confirmationDialog.copy(isVisible = false)
        )
    }

    /**
     * Handle confirmation dialog result
     */
    fun onConfirmationResult(confirmed: Boolean, context: Context) {
        val currentDialog = _uiState.value.confirmationDialog
        hideConfirmationDialog()
        
        if (confirmed) {
            when (currentDialog.type) {
                ConfirmationType.REFRESH_DATA -> refreshData(context)
                ConfirmationType.DELETE_DATA -> deleteAllData()
                ConfirmationType.EXIT_APP -> {
                    // Exit the app
                    if (context is android.app.Activity) {
                        context.finishAffinity()
                    }
                }
            }
        }
    }

    /**
     * Show notification
     */
    private fun showNotification(message: String, type: NotificationType) {
        _uiState.value = _uiState.value.copy(
            notification = NotificationState(
                isVisible = true,
                message = message,
                type = type
            )
        )
    }

    /**
     * Hide notification
     */
    fun hideNotification() {
        _uiState.value = _uiState.value.copy(
            notification = _uiState.value.notification.copy(isVisible = false)
        )
    }

    /**
     * Clear error
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Reset app to default state (fresh/clean state)
     */
    fun resetAppToDefault() {
        _uiState.value = KconvertUiState(
            dataIndicator = _uiState.value.dataIndicator, // Keep data indicator
            shouldScrollToTop = true
        )
        showNotification("App refresh successfully", NotificationType.SUCCESS)
    }
}

/**
 * UI State data class
 */
data class KconvertUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isConverting: Boolean = false,
    val amount: String = "0",
    val sourceCurrency: Currency? = null,
    val targetCurrency: Currency? = null,
    val conversionResult: ConversionResult? = null,
    val dataIndicator: String = "",
    val error: String? = null,
    val shouldScrollToTop: Boolean = false,
    val confirmationDialog: ConfirmationDialogState = ConfirmationDialogState(),
    val notification: NotificationState = NotificationState(),
    val updateDialog: UpdateDialogState = UpdateDialogState(),
    val welcomeDialog: WelcomeDialogState = WelcomeDialogState()
)

/**
 * Confirmation dialog state
 */
data class ConfirmationDialogState(
    val isVisible: Boolean = false,
    val type: ConfirmationType = ConfirmationType.REFRESH_DATA,
    val title: String = ""
)

/**
 * Notification state
 */
data class NotificationState(
    val isVisible: Boolean = false,
    val message: String = "",
    val type: NotificationType = NotificationType.SUCCESS
)

/**
 * Update dialog state
 */
data class UpdateDialogState(
    val isVisible: Boolean = false,
    val isOutdated: Boolean = false,
    val updateError: Boolean = false,
    val latestVersion: String = "",
    val updateMessage: String = "",
    val updateTitle: String = "",
    val isWarning: Boolean = false,
    val showGitHubLink: Boolean = true,
    val uiState: com.oxyzenq.kconvert.data.repository.UpdateUIState = com.oxyzenq.kconvert.data.repository.UpdateUIState.UP_TO_DATE
)

/**
 * Welcome dialog state for new app updates
 */
data class WelcomeDialogState(
    val isVisible: Boolean = false,
    val currentVersion: String = "",
    val previousVersion: String = "",
    val isFirstInstall: Boolean = false
)

/**
 * Confirmation dialog types
 */
enum class ConfirmationType {
    REFRESH_DATA,
    DELETE_DATA,
    EXIT_APP
}

/**
 * Notification types
 */
enum class NotificationType {
    SUCCESS,
    WARNING,
    ERROR
}
