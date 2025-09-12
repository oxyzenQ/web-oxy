/*
 * Simple Report Floating Window
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

@Composable
fun ReportFloatingWindow(
    isVisible: Boolean,
    reportMessage: String,
    reportIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.CheckCircle,
    iconColor: Color = Color(0xFF10B981),
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300)) + scaleIn(
            initialScale = 0.8f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ),
        exit = fadeOut(animationSpec = tween(200)) + scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(200)
        )
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = modifier
                    .fillMaxWidth(0.85f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = Color.Transparent,
                elevation = 0.dp
            ) {
                Box {
                    // Background with glassmorphism effect
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF1E1B4B).copy(alpha = 0.95f),
                                        Color(0xFF312E81).copy(alpha = 0.90f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.3f),
                                        Color.White.copy(alpha = 0.1f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clip(RoundedCornerShape(16.dp))
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Success icon
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            iconColor.copy(alpha = 0.3f),
                                            iconColor.copy(alpha = 0.1f)
                                        ),
                                        radius = 30.dp.value
                                    ),
                                    shape = RoundedCornerShape(30.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = reportIcon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Report message
                        Text(
                            text = reportMessage,
                            style = MaterialTheme.typography.body1.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                fontSize = 16.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Close button
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF8B5CF6)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            elevation = ButtonDefaults.elevation(0.dp)
                        ) {
                            Text(
                                text = "Close",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
