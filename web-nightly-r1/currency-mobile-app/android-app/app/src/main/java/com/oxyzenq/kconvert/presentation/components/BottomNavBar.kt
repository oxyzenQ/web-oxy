/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

/**
 * Bottom Navigation Bar with iOS-style glassmorphism and auto-hide functionality
 */
@Composable
fun BottomNavBar(
    isVisible: Boolean,
    onExitClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onResetAppClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val navBarHeight = 64.dp // Reduced height
    
    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else with(density) { navBarHeight.toPx() },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "navbar_offset"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(navBarHeight)
            .offset(y = with(density) { offsetY.toDp() })
    ) {
        // Container 0 style background with iOS rounded corners and more transparency
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 24.dp))
        ) {
            // Outer glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
                    .blur(20.dp)
            )
            // Main background with ElegantInfoCard style
            Box(
                modifier = Modifier
                    .fillMaxSize()
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
                        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
            )
        }
        
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Exit Button (Left)
            NavBarButton(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                onClick = onExitClick,
                contentDescription = "Exit App"
            )
            
            // Settings Button (Center) with rotation animation
            NavBarButton(
                icon = Icons.Default.Settings,
                onClick = onSettingsClick,
                contentDescription = "Settings",
                isCenter = true,
                animateRotation = true
            )
            
            // Reset App Button (Right)
            NavBarButton(
                icon = Icons.Default.Refresh,
                onClick = onResetAppClick,
                contentDescription = "Refresh App"
            )
        }
    }
}

/**
 * iOS-style navigation bar button component
 */
@Composable
fun NavBarButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String,
    isCenter: Boolean = false,
    animateRotation: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current
    
    // Infinite rotation animation for settings gear
    val infiniteTransition = rememberInfiniteTransition(label = "gear_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animateRotation) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 8000, // 8 seconds for smooth, slow rotation
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_angle"
    )
    
    Box(
        modifier = Modifier
            .size(if (isCenter) 48.dp else 42.dp) // Reduced size
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null // Remove ripple/background effect
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .then(bouncyPress(interactionSource)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isCenter) Color.White else Color.White.copy(alpha = 0.95f),
            modifier = Modifier
                .size(if (isCenter) 24.dp else 20.dp)
                .rotate(rotationAngle)
        )
    }
}

/**
 * Bouncy press effect modifier: shrinks on press, overshoots slightly on release, then settles.
 */
@Composable
private fun bouncyPress(interactionSource: MutableInteractionSource, pressedScale: Float = 0.94f): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = if (pressed) tween(durationMillis = 110, easing = FastOutLinearInEasing)
        else spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMediumLow),
        label = "bouncy_press_scale"
    )
    return Modifier.scale(scale)
}
