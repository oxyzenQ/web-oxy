/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.currencyconverter.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oxyzenq.currencyconverter.data.preferences.AppPreferences
import com.oxyzenq.currencyconverter.data.repository.CurrencyRepository
import android.content.Context
import com.oxyzenq.currencyconverter.data.repository.ConversionResult
import com.oxyzenq.currencyconverter.data.model.Currency
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
    private val appPreferences: AppPreferences
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

    // Auto update setting
    val autoUpdateEnabled: StateFlow<Boolean> = appPreferences.autoUpdateOnLaunch
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
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
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load initial data: ${e.message}"
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
                    showNotification("Ultra-secure data refreshed successfully", NotificationType.SUCCESS)
                    loadCurrencies() // Reload currencies after refresh
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
                val currencies = currencyRepository.getAllCurrencies()
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
                    onSuccess = { message ->
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
     * Toggle auto update setting
     */
    fun toggleAutoUpdate() {
        viewModelScope.launch {
            try {
                val currentSetting = autoUpdateEnabled.value
                val newSetting = !currentSetting
                
                appPreferences.setAutoUpdateOnLaunch(newSetting)
                
                val message = if (newSetting) {
                    "enable auto update on launch is on sir!"
                } else {
                    "auto update on launch is off sir!"
                }
                
                showNotification(message, NotificationType.SUCCESS)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to toggle auto update: ${e.message}"
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
        _uiState.value = _uiState.value.copy(
            confirmationDialog = ConfirmationDialogState(
                isVisible = true,
                type = type,
                title = when (type) {
                    ConfirmationType.REFRESH_DATA -> "Are you sure to update sir?"
                    ConfirmationType.DELETE_DATA -> "Are you sure to delete the data of price sir? This can't be undone!"
                }
            )
        )
    }

    /**
     * Hide confirmation dialog
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
        val dialogType = _uiState.value.confirmationDialog.type
        hideConfirmationDialog()
        
        if (confirmed) {
            when (dialogType) {
                ConfirmationType.REFRESH_DATA -> refreshData(context)
                ConfirmationType.DELETE_DATA -> deleteAllData()
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
    val notification: NotificationState = NotificationState()
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
 * Confirmation dialog types
 */
enum class ConfirmationType {
    REFRESH_DATA,
    DELETE_DATA
}

/**
 * Notification types
 */
enum class NotificationType {
    SUCCESS,
    WARNING,
    ERROR
}
