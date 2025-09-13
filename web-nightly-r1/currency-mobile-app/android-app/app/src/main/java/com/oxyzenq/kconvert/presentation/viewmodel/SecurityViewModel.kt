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
     * Get detailed security log for export
     */
    fun getSecurityLog(): String {
        val state = _securityState.value
        val threats = raspSecurityManager.getDetectedThreats()
        val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date(state.lastCheckTime))
        
        return buildString {
            appendLine("=== KCONVERT SECURITY LOG ===")
            appendLine("Generated: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")
            appendLine("Last Check: $timestamp")
            appendLine("Status: ${if (state.isPassed) "PASSED" else "FAILED"}")
            appendLine("Threats Detected: ${threats.size}")
            appendLine()
            
            if (threats.isNotEmpty()) {
                appendLine("=== THREAT DETAILS ===")
                threats.forEachIndexed { index, threat ->
                    appendLine("${index + 1}. ${threat.type}")
                    appendLine("   Level: ${threat.level}")
                    appendLine("   Description: ${threat.description}")
                    appendLine("   Timestamp: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(threat.timestamp))}")
                    appendLine()
                }
            } else {
                appendLine("No threats detected.")
            }
            
            appendLine("=== SYSTEM INFO ===")
            appendLine("Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
            appendLine("Android: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
            appendLine("Build Type: ${android.os.Build.TYPE}")
            appendLine("Build Tags: ${android.os.Build.TAGS}")
            appendLine()
            appendLine("=== END LOG ===")
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
