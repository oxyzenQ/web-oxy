/*
 * Enhanced Haptic Feedback Helper
 * Provides robust vibration with fallback mechanisms
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Enhanced haptic feedback helper with multiple fallback mechanisms
 * Ensures vibration works on all Android devices
 */
object HapticHelper {
    
    /**
     * Perform haptic feedback with multiple fallback mechanisms
     * @param context Android context
     * @param type Haptic feedback type (SUCCESS, ERROR, WARNING)
     * @param enabled Whether haptic feedback is enabled by user
     */
    fun performHaptic(
        context: Context, 
        type: HapticType = HapticType.SUCCESS,
        enabled: Boolean = true
    ) {
        if (!enabled) return
        
        try {
            // Get vibrator service
            val vibrator = getVibrator(context)
            
            // Perform vibration based on type
            when (type) {
                HapticType.SUCCESS -> performSuccessVibration(vibrator)
                HapticType.ERROR -> performErrorVibration(vibrator)
                HapticType.WARNING -> performWarningVibration(vibrator)
                HapticType.LIGHT -> performLightVibration(vibrator)
                HapticType.MEDIUM -> performMediumVibration(vibrator)
                HapticType.HEAVY -> performHeavyVibration(vibrator)
            }
        } catch (e: Exception) {
            // Silent fail - don't crash app if vibration fails
        }
    }
    
    /**
     * Get vibrator service with API level compatibility
     */
    private fun getVibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    /**
     * Success vibration - single smooth pulse
     */
    private fun performSuccessVibration(vibrator: Vibrator) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }
    
    /**
     * Error vibration - double pulse pattern
     */
    private fun performErrorVibration(vibrator: Vibrator) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 40, 80, 40)
            val amplitudes = intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE)
            val effect = VibrationEffect.createWaveform(pattern, amplitudes, -1)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            val pattern = longArrayOf(0, 40, 80, 40)
            vibrator.vibrate(pattern, -1)
        }
    }
    
    /**
     * Warning vibration - medium pulse
     */
    private fun performWarningVibration(vibrator: Vibrator) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(70)
        }
    }
    
    /**
     * Light vibration - very gentle
     */
    private fun performLightVibration(vibrator: Vibrator) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(30, 100)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(30)
        }
    }
    
    /**
     * Medium vibration - standard feedback
     */
    private fun performMediumVibration(vibrator: Vibrator) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(60)
        }
    }
    
    /**
     * Heavy vibration - strong feedback
     */
    private fun performHeavyVibration(vibrator: Vibrator) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(100, 255)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }
    
    /**
     * Compose-friendly haptic feedback
     */
    @Composable
    fun rememberHapticPerformer(enabled: Boolean = true): (HapticType) -> Unit {
        val context = LocalContext.current
        return remember(enabled) { { type ->
            performHaptic(context, type, enabled)
        }}
    }
}

/**
 * Haptic feedback types with different vibration patterns
 */
enum class HapticType {
    SUCCESS,    // Single smooth pulse (50ms)
    ERROR,      // Double pulse pattern (40ms-80ms-40ms)
    WARNING,    // Medium pulse (70ms)
    LIGHT,      // Very gentle (30ms)
    MEDIUM,     // Standard feedback (60ms)
    HEAVY       // Strong feedback (100ms)
}
