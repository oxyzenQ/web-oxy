/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.components

import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Beautiful floating confirmation dialog with glassmorphism design
 * Perfect for critical actions like delete confirmations
 */
@Composable
fun FloatingConfirmationDialog(
    isVisible: Boolean,
    title: String,
    message: String? = null,
    icon: ImageVector = Icons.Default.Warning,
    iconColor: Color = Color(0xFFFF9500),
    confirmText: String = "Yes",
    dismissText: String = "No",
    confirmButtonColor: Color = Color(0xFFEF4444),
    dismissButtonColor: Color = Color(0xFF6B7280),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    hapticsEnabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current

    FloatingModal(
        visible = isVisible,
        onDismiss = onDismiss,
        width = 280.dp,
        header = {
            FloatingModalHeader(
                title = title,
                subtitle = message,
                icon = icon,
                iconTint = iconColor
            )
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDismiss()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = androidx.compose.material.ButtonDefaults.buttonColors(
                    backgroundColor = dismissButtonColor.copy(alpha = 0.9f),
                    contentColor = Color.White
                ),
                elevation = androidx.compose.material.ButtonDefaults.elevation(0.dp)
            ) {
                Text(
                    text = dismissText,
                    style = MaterialTheme.typography.button.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                )
            }

            Button(
                onClick = {
                    if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onConfirm()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = androidx.compose.material.ButtonDefaults.buttonColors(
                    backgroundColor = confirmButtonColor.copy(alpha = 0.9f),
                    contentColor = Color.White
                ),
                elevation = androidx.compose.material.ButtonDefaults.elevation(0.dp)
            ) {
                Text(
                    text = confirmText,
                    style = MaterialTheme.typography.button.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}

/**
 * Predefined confirmation dialog for delete actions
 */
@Composable
fun DeleteConfirmationDialog(
    isVisible: Boolean,
    itemName: String = "All Data",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    hapticsEnabled: Boolean = true
) {
    FloatingConfirmationDialog(
        isVisible = isVisible,
        title = "Delete $itemName",
        message = "This action cannot be undone. Are you sure you want to proceed?",
        icon = Icons.Default.Warning,
        iconColor = Color(0xFFFF9500), // Orange warning
        confirmText = "Yes",
        dismissText = "No",
        confirmButtonColor = Color(0xFFEF4444), // Red
        dismissButtonColor = Color(0xFF6B7280), // Gray
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        hapticsEnabled = hapticsEnabled
    )
}

/**
 * Predefined confirmation dialog for logout actions
 */
@Composable
fun LogoutConfirmationDialog(
    isVisible: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    hapticsEnabled: Boolean = true
) {
    FloatingConfirmationDialog(
        isVisible = isVisible,
        title = "Sign Out",
        message = "Are you sure you want to sign out of your account?",
        icon = Icons.Default.ExitToApp,
        iconColor = Color(0xFF3B82F6), // Blue
        confirmText = "Sign Out",
        dismissText = "Cancel",
        confirmButtonColor = Color(0xFF3B82F6), // Blue
        dismissButtonColor = Color(0xFF6B7280), // Gray
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        hapticsEnabled = hapticsEnabled
    )
}

/**
 * Predefined confirmation dialog for clear cache actions
 */
@Composable
fun ClearCacheConfirmationDialog(
    isVisible: Boolean,
    cacheSize: String = "",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    hapticsEnabled: Boolean = true
) {
    FloatingConfirmationDialog(
        isVisible = isVisible,
        title = "Clear Cache",
        message = if (cacheSize.isNotEmpty()) {
            "This will clear $cacheSize of cached data. Continue?"
        } else {
            "This will clear all cached data. Continue?"
        },
        icon = Icons.Default.CleaningServices,
        iconColor = Color(0xFFF59E0B),
        confirmText = "Clear",
        dismissText = "Cancel",
        confirmButtonColor = Color(0xFFF59E0B),
        dismissButtonColor = Color(0xFF6B7280),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        hapticsEnabled = hapticsEnabled
    )
}
