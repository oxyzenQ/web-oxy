/*
 * Global Floating Window components
 * Centralizes layout, bones, and styles for all floating windows.
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.oxyzenq.kconvert.data.model.Currency

/**
 * Basic wrappers built on FloatingModal for common message types
 */
@Composable
fun InfoWindow(
    visible: Boolean,
    title: String,
    subtitle: String? = null,
    onDismiss: () -> Unit,
    strict: Boolean = true,
    hapticsEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    FloatingModal(
        visible = visible,
        onDismiss = onDismiss,
        strictModal = strict,
        width = 280.dp,
        header = {
            FloatingModalHeader(
                title = title,
                subtitle = subtitle,
                icon = Icons.Default.CheckCircle,
                iconTint = Color(0xFF10B981)
            )
        }
    ) { content() }
}

/**
 * Currency picker window using global floating modal style.
 */
@Composable
fun CurrencyPickerWindow(
    visible: Boolean,
    title: String = "Select Currency",
    currencies: List<Currency>,
    onCurrencySelected: (Currency) -> Unit,
    onDismiss: () -> Unit,
    strict: Boolean = true,
    hapticsEnabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    FloatingModal(
        visible = visible,
        onDismiss = onDismiss,
        strictModal = strict,
        width = 320.dp,
        header = {
            FloatingModalHeader(
                title = title,
                subtitle = null,
                icon = null,
                iconTint = Color.White
            )
        }
    ) {
        // Scrollable list with height cap to avoid tall modal
        Box(modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp, max = 360.dp)) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(currencies) { currency ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCurrencySelected(currency)
                                onDismiss()
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = currency.code,
                                style = MaterialTheme.typography.subtitle1.copy(
                                    color = Color.White
                                )
                            )
                            Text(
                                text = currency.name,
                                style = MaterialTheme.typography.caption.copy(
                                    color = Color(0xFF94A3B8)
                                )
                            )
                        }
                    }
                }
            }
        }
        // Close button
        Button(
            onClick = {
                if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF374151).copy(alpha = 0.7f),
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.elevation(0.dp)
        ) {
            Text(text = "Close", style = MaterialTheme.typography.button)
        }
    }
}

@Composable
fun ErrorWindow(
    visible: Boolean,
    title: String,
    subtitle: String? = null,
    onDismiss: () -> Unit,
    strict: Boolean = true,
    hapticsEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    FloatingModal(
        visible = visible,
        onDismiss = onDismiss,
        strictModal = strict,
        width = 280.dp,
        header = {
            FloatingModalHeader(
                title = title,
                subtitle = subtitle,
                icon = Icons.Default.Warning,
                iconTint = Color(0xFFF59E0B)
            )
        }
    ) { content() }
}

@Composable
fun SuccessWindow(
    visible: Boolean,
    title: String,
    subtitle: String? = null,
    onDismiss: () -> Unit,
    strict: Boolean = true,
    hapticsEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    FloatingModal(
        visible = visible,
        onDismiss = onDismiss,
        strictModal = strict,
        width = 280.dp,
        header = {
            FloatingModalHeader(
                title = title,
                subtitle = subtitle,
                icon = Icons.Default.CheckCircle,
                iconTint = Color(0xFF10B981)
            )
        }
    ) { content() }
}

/**
 * Update result window used by Maintenance section.
 */
@Composable
fun UpdateResultWindow(
    visible: Boolean,
    updateError: Boolean,
    isOutdated: Boolean,
    latestVersion: String,
    updateMessage: String,
    currentVersion: String,
    onDismiss: () -> Unit,
    onOpenLatest: () -> Unit,
    onOpenReleases: () -> Unit,
    isWarning: Boolean = false,
    showGitHubLink: Boolean = true,
    updateTitle: String = "",
    uiState: com.oxyzenq.kconvert.data.repository.UpdateUIState = com.oxyzenq.kconvert.data.repository.UpdateUIState.UP_TO_DATE,
    strict: Boolean = true,
    hapticsEnabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val (icon, tint, backgroundColor) = when {
        updateError -> Triple(Icons.Default.Warning, Color(0xFFF59E0B), Color(0xFFF59E0B).copy(alpha = 0.12f))
        uiState == com.oxyzenq.kconvert.data.repository.UpdateUIState.MISMATCH_WARNING -> Triple(
            Icons.Default.Warning, 
            Color(0xFFEF4444), 
            Color(0xFFEF4444).copy(alpha = 0.12f)
        )
        uiState == com.oxyzenq.kconvert.data.repository.UpdateUIState.UPDATE_AVAILABLE -> Triple(
            Icons.AutoMirrored.Filled.OpenInNew, 
            Color(0xFFF59E0B), 
            Color(0xFFF59E0B).copy(alpha = 0.12f)
        )
        uiState == com.oxyzenq.kconvert.data.repository.UpdateUIState.UP_TO_DATE -> Triple(
            Icons.Default.CheckCircle, 
            Color(0xFF10B981), 
            Color(0xFF10B981).copy(alpha = 0.12f)
        )
        else -> Triple(Icons.Default.CheckCircle, Color(0xFF10B981), Color(0xFF10B981).copy(alpha = 0.12f))
    }

    FloatingModal(
        visible = visible,
        onDismiss = onDismiss,
        strictModal = strict,
        width = 320.dp,
        cornerRadius = 20.dp,
        header = {
            // iOS-like header with proper centering and rounded corners
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = backgroundColor,
                        shape = RoundedCornerShape(
                            topStart = 20.dp, 
                            topEnd = 20.dp,
                            bottomStart = 12.dp,
                            bottomEnd = 12.dp
                        )
                    )
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Centered icon with proper sizing
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    // Title with iOS-like typography
                    Text(
                        text = if (updateTitle.isNotEmpty()) updateTitle else updateMessage,
                        style = MaterialTheme.typography.h6.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 18.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    
                    // Subtitle with proper spacing
                    if (updateTitle.isNotEmpty()) {
                        Text(
                            text = updateMessage,
                            style = MaterialTheme.typography.body2.copy(
                                color = Color(0xFFCBD5E1),
                                fontSize = 14.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "Current version: $currentVersion",
                            style = MaterialTheme.typography.body2.copy(
                                color = Color(0xFFCBD5E1),
                                fontSize = 14.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    ) {
        // iOS-like button layout with proper spacing
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Show GitHub link button for updates, warnings, or errors
            if (showGitHubLink && (isOutdated || isWarning || updateError || 
                uiState == com.oxyzenq.kconvert.data.repository.UpdateUIState.UPDATE_AVAILABLE ||
                uiState == com.oxyzenq.kconvert.data.repository.UpdateUIState.MISMATCH_WARNING)) {
                
                val buttonColor = when (uiState) {
                    com.oxyzenq.kconvert.data.repository.UpdateUIState.MISMATCH_WARNING -> Color(0xFFEF4444)
                    com.oxyzenq.kconvert.data.repository.UpdateUIState.UPDATE_AVAILABLE -> Color(0xFF007AFF) // iOS blue
                    else -> Color(0xFF007AFF)
                }
                
                Button(
                    onClick = {
                        if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (isOutdated || uiState == com.oxyzenq.kconvert.data.repository.UpdateUIState.UPDATE_AVAILABLE) onOpenLatest() else onOpenReleases()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = buttonColor,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.elevation(0.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center, 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Launch, 
                            contentDescription = "Visit GitHub", 
                            tint = Color.White, 
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (uiState) {
                                com.oxyzenq.kconvert.data.repository.UpdateUIState.UPDATE_AVAILABLE -> "Get Update on GitHub"
                                com.oxyzenq.kconvert.data.repository.UpdateUIState.MISMATCH_WARNING -> "Visit Repository"
                                else -> "Visit Original Repository"
                            },
                            style = MaterialTheme.typography.button.copy(
                                fontSize = 16.sp, 
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            // Close button with iOS styling
            Button(
                onClick = {
                    if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF48525F).copy(alpha = 0.8f),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.elevation(0.dp)
            ) {
                Text(
                    text = "Close", 
                    style = MaterialTheme.typography.button.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

/**
 * Welcome dialog for new app updates and first installs
 */
@Composable
fun WelcomeWindow(
    visible: Boolean,
    currentVersion: String,
    previousVersion: String,
    isFirstInstall: Boolean,
    onDismiss: () -> Unit,
    onOpenGitHub: () -> Unit,
    strict: Boolean = true,
    hapticsEnabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val (title, message, icon) = if (isFirstInstall) {
        Triple(
            "Welcome to Kconvert!",
            "Thank you for installing Kconvert $currentVersion. Enjoy fast, secure currency conversion with premium features and real-time updates.",
            Icons.Default.Star
        )
    } else {
        Triple(
            "Successfully Updated!",
            "Congratulations! You've successfully updated from $previousVersion to $currentVersion. Enjoy the latest features and improvements.",
            Icons.Default.CheckCircle
        )
    }
    
    val iconTint = if (isFirstInstall) Color(0xFF10B981) else Color(0xFF3B82F6)

    FloatingModal(
        visible = visible,
        onDismiss = onDismiss,
        strictModal = strict,
        width = 320.dp,
        cornerRadius = 20.dp,
        header = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = iconTint.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                        )
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = iconTint
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.h6.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.body2.copy(
                    color = Color(0xFFE5E7EB),
                    lineHeight = 20.sp
                ),
                textAlign = TextAlign.Center
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF6B7280),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Continue")
                }
                
                Button(
                    onClick = {
                        if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOpenGitHub()
                    },
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = iconTint,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("GitHub")
                    }
                }
            }
        }
    }
}

/**
 * Cache management window used by Maintenance section.
 * Host screen should handle the actual scan/clear work via callbacks.
 */
@Composable
fun CacheManagementWindow(
    visible: Boolean,
    cacheSizeText: String,
    isScanning: Boolean,
    isClearing: Boolean,
    onScan: () -> Unit,
    onRequestClear: () -> Unit,
    onDismiss: () -> Unit,
    strict: Boolean = true,
    hapticsEnabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    FloatingModal(
        visible = visible,
        onDismiss = onDismiss,
        strictModal = strict,
        width = 280.dp,
        header = {
            FloatingModalHeader(
                title = "Cache Management",
                subtitle = "Current size: $cacheSizeText",
                icon = Icons.Default.Storage,
                iconTint = Color(0xFF10B981)
            )
        }
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onScan()
                },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF3B82F6).copy(alpha = 0.85f),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.elevation(0.dp),
                enabled = !isScanning && !isClearing
            ) {
                Text(text = if (isScanning) "Scanning..." else "Scan", style = MaterialTheme.typography.button)
            }
            Button(
                onClick = {
                    if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onRequestClear()
                },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFFEF4444).copy(alpha = 0.85f),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.elevation(0.dp),
                enabled = !isScanning && !isClearing
            ) {
                Text(text = if (isClearing) "Clearing..." else "Clear", style = MaterialTheme.typography.button)
            }
        }
        Button(
            onClick = {
                if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF374151).copy(alpha = 0.7f),
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.elevation(0.dp)
        ) {
            Text(text = "Cancel", style = MaterialTheme.typography.button)
        }
    }
}
