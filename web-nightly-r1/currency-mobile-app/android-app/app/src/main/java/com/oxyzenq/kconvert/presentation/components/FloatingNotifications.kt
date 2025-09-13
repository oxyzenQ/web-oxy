/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.oxyzenq.kconvert.presentation.viewmodel.ConfirmationType
import com.oxyzenq.kconvert.presentation.viewmodel.NotificationType
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
    onDismiss: () -> Unit,
    strict: Boolean = true,
    hapticsEnabled: Boolean = true
) {
    if (!isVisible) return

    val confirmColor = when (type) {
        ConfirmationType.DELETE_DATA, ConfirmationType.EXIT_APP -> Color(0xFFDC2626)
        ConfirmationType.REFRESH_DATA -> Color(0xFF059669)
    }

    FloatingModal(
        visible = isVisible,
        onDismiss = onDismiss,
        strictModal = strict,
        width = 280.dp,
        header = {
            FloatingModalHeader(
                title = title,
                subtitle = null,
                icon = Icons.Default.Warning,
                iconTint = Color(0xFFF59E0B)
            )
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onDismiss,
                modifier = Modifier.weight(1f).height(44.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6B7280), contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) { Text(text = "No") }

            Button(
                onClick = { onConfirm(); onDismiss() },
                modifier = Modifier.weight(1f).height(44.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = confirmColor, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) { Text(text = "Yes") }
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
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .widthIn(min = 260.dp, max = 300.dp)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                // Outer glow
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.25f))
                        .blur(20.dp)
                )
                // Main background with navbar style
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0B1530).copy(alpha = 0.8f),
                                    Color(0xFF0F1F3F).copy(alpha = 0.9f)
                                )
                            )
                        )
                        .border(
                            0.5.dp,
                            Color.White.copy(alpha = 0.08f),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
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
}
