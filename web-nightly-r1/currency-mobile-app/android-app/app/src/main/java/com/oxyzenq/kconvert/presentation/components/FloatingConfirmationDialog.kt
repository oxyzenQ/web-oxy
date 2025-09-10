/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex

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
    iconColor: Color = Color(0xFFFF9500), // Orange warning color
    confirmText: String = "Yes",
    dismissText: String = "No",
    confirmButtonColor: Color = Color(0xFFEF4444), // Red for destructive actions
    dismissButtonColor: Color = Color(0xFF6B7280), // Gray for cancel
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    hapticsEnabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    
    // Animation states
    val animatedScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
    )
    
    val backgroundBlur by animateFloatAsState(
        targetValue = if (isVisible) 8f else 0f,
        animationSpec = tween(200, easing = LinearOutSlowInEasing)
    )

    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10f),
                contentAlignment = Alignment.Center
            ) {
                // Background blur overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f * animatedAlpha))
                        .blur(backgroundBlur.dp)
                )
                
                // Main dialog card
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .wrapContentHeight()
                        .scale(animatedScale)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    // Outer glow effect
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                            .blur(24.dp)
                    )
                    
                    // Main glassmorphism background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF1E293B).copy(alpha = 0.95f),
                                        Color(0xFF0F172A).copy(alpha = 0.98f)
                                    )
                                )
                            )
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Warning icon with glow effect
                            Box(
                                modifier = Modifier.size(64.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Icon glow
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            iconColor.copy(alpha = 0.3f),
                                            RoundedCornerShape(24.dp)
                                        )
                                        .blur(12.dp)
                                )
                                
                                // Main icon
                                Icon(
                                    imageVector = icon,
                                    contentDescription = "Warning",
                                    tint = iconColor,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            
                            // Title text
                            Text(
                                text = title,
                                style = MaterialTheme.typography.h6.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 18.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                            
                            // Optional message text
                            message?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.body2.copy(
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                            
                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Dismiss button (No)
                                Button(
                                    onClick = {
                                        if (hapticsEnabled) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                        onDismiss()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = dismissButtonColor.copy(alpha = 0.9f),
                                        contentColor = Color.White
                                    ),
                                    elevation = ButtonDefaults.elevation(
                                        defaultElevation = 0.dp,
                                        pressedElevation = 2.dp
                                    )
                                ) {
                                    Text(
                                        text = dismissText,
                                        style = MaterialTheme.typography.button.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp
                                        )
                                    )
                                }
                                
                                // Confirm button (Yes)
                                Button(
                                    onClick = {
                                        if (hapticsEnabled) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                        onConfirm()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = confirmButtonColor.copy(alpha = 0.9f),
                                        contentColor = Color.White
                                    ),
                                    elevation = ButtonDefaults.elevation(
                                        defaultElevation = 0.dp,
                                        pressedElevation = 2.dp
                                    )
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
                }
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
        iconColor = Color(0xFFF59E0B), // Amber warning
        confirmText = "Clear",
        dismissText = "Cancel",
        confirmButtonColor = Color(0xFFF59E0B), // Amber
        dismissButtonColor = Color(0xFF6B7280), // Gray
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        hapticsEnabled = hapticsEnabled
    )
}
