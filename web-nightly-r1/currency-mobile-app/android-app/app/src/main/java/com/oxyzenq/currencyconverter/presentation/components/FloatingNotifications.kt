/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.currencyconverter.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.oxyzenq.currencyconverter.presentation.viewmodel.ConfirmationType
import com.oxyzenq.currencyconverter.presentation.viewmodel.NotificationType
import kotlinx.coroutines.delay

/**
 * Floating confirmation dialog with warning icon
 */
@Composable
fun ConfirmationDialog(
    isVisible: Boolean,
    title: String,
    type: ConfirmationType,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = Color(0xFF1E293B),
                elevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Warning icon
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Title
                    Text(
                        text = title,
                        style = MaterialTheme.typography.h6.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF6B7280)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "No",
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Button(
                            onClick = {
                                onConfirm()
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = when (type) {
                                    ConfirmationType.DELETE_DATA -> Color(0xFFDC2626)
                                    ConfirmationType.REFRESH_DATA -> Color(0xFF059669)
                                    ConfirmationType.EXIT_APP -> Color(0xFFDC2626)
                                }
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Yes",
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Floating notification that auto-dismisses after 3 seconds
 */
@Composable
fun FloatingNotification(
    isVisible: Boolean,
    message: String,
    type: NotificationType,
    onDismiss: () -> Unit
) {
    // Auto dismiss after 3 seconds
    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(3000)
            onDismiss()
        }
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = when (type) {
                    NotificationType.SUCCESS -> Color(0xFF059669)
                    NotificationType.WARNING -> Color(0xFFF59E0B)
                    NotificationType.ERROR -> Color(0xFFDC2626)
                },
                elevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = when (type) {
                            NotificationType.SUCCESS -> Icons.Default.Check
                            NotificationType.WARNING, NotificationType.ERROR -> Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = message,
                        style = MaterialTheme.typography.body1.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
