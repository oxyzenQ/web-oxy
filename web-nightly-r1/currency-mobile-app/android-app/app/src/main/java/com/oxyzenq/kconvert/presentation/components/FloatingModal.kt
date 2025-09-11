/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Reusable compact glassmorphism floating modal used across the app.
 *
 * Goals:
 * - Consistent iOS-like floating window styling
 * - Compact width with wrap content height (no tall containers)
 * - Header slot (icon + title + subtitle) and content slot
 */
@Composable
fun FloatingModal(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 280.dp,
    cornerRadius: Dp = 20.dp,
    strictModal: Boolean = false,
    header: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val alphaAnim by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(200, easing = FastOutSlowInEasing)
    )

    if (!visible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = !strictModal,
            dismissOnClickOutside = !strictModal
        )
    ) {
        // Direct card without full-screen backdrop
        Card(
            modifier = modifier
                .width(width)
                .wrapContentHeight(),
            backgroundColor = Color.Transparent,
            elevation = 0.dp,
            shape = RoundedCornerShape(cornerRadius)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E293B).copy(alpha = 0.92f),
                                Color(0xFF0F172A).copy(alpha = 0.95f)
                            )
                        ),
                        shape = RoundedCornerShape(cornerRadius)
                    )
                    .border(
                        width = 0.5.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        ),
                        shape = RoundedCornerShape(cornerRadius)
                    )
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    header?.invoke()
                    content()
                }
            }
        }
    }
}

@Composable
fun FloatingModalHeader(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = Icons.Default.Warning,
    iconTint: Color = Color(0xFF10B981) // default success-green; override per use-case
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier.size(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.h6.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                fontSize = 18.sp
            ),
            textAlign = TextAlign.Center
        )
        if (!subtitle.isNullOrEmpty()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.body2.copy(
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}
