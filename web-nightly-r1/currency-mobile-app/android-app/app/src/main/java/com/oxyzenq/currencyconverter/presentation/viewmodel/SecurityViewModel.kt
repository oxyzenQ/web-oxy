/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oxyzenq.kconvert.security.RASPSecurityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Security state data class
 */
data class SecurityState(
    val isLoading: Boolean = false,
    val isPassed: Boolean = false,
    val threatDetails: List<String> = emptyList(),
    val lastCheckTime: Long = 0L,
    val errorMessage: String? = null
)

/**
 * ViewModel for managing security checks and status
 */
@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val raspSecurityManager: RASPSecurityManager
) : ViewModel() {
    
    private val _securityState = MutableStateFlow(SecurityState())
    val securityState: StateFlow<SecurityState> = _securityState.asStateFlow()
    
    /**
     * Perform comprehensive security check
     */
    fun performSecurityCheck(context: Context) {
        viewModelScope.launch {
            try {
                _securityState.value = _securityState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )
                
                // Perform security assessment
                val securityStatus = raspSecurityManager.performSecurityAssessment(context)
                
                _securityState.value = SecurityState(
                    isLoading = false,
                    isPassed = securityStatus == RASPSecurityManager.SecurityStatus.SECURE,
                    threatDetails = emptyList(), // Will be populated from detected threats if needed
                    lastCheckTime = System.currentTimeMillis(),
                    errorMessage = null
                )
                
            } catch (e: Exception) {
                _securityState.value = SecurityState(
                    isLoading = false,
                    isPassed = false,
                    threatDetails = emptyList(),
                    lastCheckTime = System.currentTimeMillis(),
                    errorMessage = e.message ?: "Security check failed"
                )
            }
        }
    }
    
    /**
     * Get security status summary
     */
    fun getSecurityStatusSummary(): String {
        val state = _securityState.value
        return when {
            state.isLoading -> "Checking security..."
            state.isPassed -> "✅ Security Check: Passed"
            state.threatDetails.isNotEmpty() -> "❌ Security Check: Failed (${state.threatDetails.size} threats)"
            else -> "⚠️ Security Check: Unknown"
        }
    }
    
    /**
     * Check if security check is needed (every 5 minutes)
     */
    fun isSecurityCheckNeeded(): Boolean {
        val lastCheck = _securityState.value.lastCheckTime
        val fiveMinutes = 5 * 60 * 1000L
        return System.currentTimeMillis() - lastCheck > fiveMinutes
    }
    
    /**
     * Clear security state
     */
    fun clearSecurityState() {
        _securityState.value = SecurityState()
    }
}
