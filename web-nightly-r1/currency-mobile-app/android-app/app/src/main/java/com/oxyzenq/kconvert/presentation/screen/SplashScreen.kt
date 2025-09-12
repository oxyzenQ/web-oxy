/*
 * Ultra-Premium Lag-Free Plasma Circle Splash Screen (GPU Optimized)
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.oxyzenq.kconvert.R
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

/**
 * Ultra-Premium Lag-Free Plasma Circle Splash Screen (GPU Optimized)
 */
@Composable
fun SplashScreen(navController: NavController) {
    val configuration = LocalConfiguration.current
    val view = LocalView.current
    val density = LocalDensity.current
    val context = LocalContext.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    
    // Device performance detection
    val isLowEndDevice = remember {
        // Simple heuristic: devices with low screen density or older Android versions
        configuration.densityDpi < 320 || android.os.Build.VERSION.SDK_INT < 26
    }
    
    // Adaptive screen dimensions for stroke width calculation
    val minScreenDimension = remember {
        minOf(configuration.screenWidthDp, configuration.screenHeightDp)
    }
    
    // Animation states
    var isVisible by remember { mutableStateOf(false) }
    var circleProgress by remember { mutableStateOf(0f) }
    var logoGlow by remember { mutableStateOf(false) }
    var circleFlash by remember { mutableStateOf(false) }
    
    // Preload cached brush
    val plasmaBrush = remember {
        createCachedPlasmaShader(density, screenWidth, screenHeight)
    }
    
    // Infinite animations
    val infiniteTransition = rememberInfiniteTransition(label = "cosmic_animations")
    
    // Floating stardust particles
    val particleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_drift"
    )
    
    // Logo breathing pulse animation (optimized with spring easing)
    val logoPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_pulse"
    )
    
    val logoPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_pulse_alpha"
    )
    
    // Plasma ring rotation (smooth PathEffect animation)
    val plasmaRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isLowEndDevice) 12000 else 8000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "plasma_rotation"
    )
    
    // Removed dash phase - using solid continuous line instead
    
    // Logo glow animation
    val logoGlowAlpha by animateFloatAsState(
        targetValue = if (logoGlow) 1f else 0.4f,
        animationSpec = tween(1000, easing = EaseInOutCubic),
        label = "logo_glow"
    )
    
    // Circle flash effect
    val circleFlashAlpha by animateFloatAsState(
        targetValue = if (circleFlash) 1f else 0f,
        animationSpec = tween(300, easing = EaseInOutCubic),
        label = "circle_flash"
    )
    
    // Logo scale animation
    val logoScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "logo_scale"
    )
    
    // Production-ready splash sequence with app init sync
    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        
        delay(300)
        isVisible = true
        delay(200)
        logoGlow = true
        
        // 5-second circular progress animation with smooth easing
        val animationDuration = if (isLowEndDevice) 5500L else 5000L
        val steps = if (isLowEndDevice) 80 else 100
        val stepDelay = animationDuration / steps
        
        for (i in 0..steps) {
            // Apply EaseInOutCubic easing for smoother animation
            val linearProgress = i.toFloat() / steps
            val easedProgress = if (linearProgress < 0.5f) {
                4f * linearProgress * linearProgress * linearProgress
            } else {
                1f - (-2f * linearProgress + 2f).pow(3f) / 2f
            }
            circleProgress = easedProgress
            
            // Trigger flash when circle completes (95%)
            if (i >= (steps * 0.95f).toInt() && !circleFlash) {
                circleFlash = true
                // Haptic feedback when plasma ring completes
                try {
                    com.oxyzenq.kconvert.utils.HapticHelper.performHaptic(
                        context = context,
                        type = com.oxyzenq.kconvert.utils.HapticType.LIGHT,
                        enabled = true
                    )
                } catch (e: Exception) {
                    // Graceful fallback if haptic fails
                }
            }
            
            delay(stepDelay)
        }
        
        // Ensure minimum 2 seconds display time even if app loads faster
        val elapsedTime = System.currentTimeMillis() - startTime
        val minimumDisplayTime = 2000L
        if (elapsedTime < minimumDisplayTime) {
            delay(minimumDisplayTime - elapsedTime)
        }
        
        delay(300) // Hold flash effect
        
        // Safe navigation with animation completion
        try {
            navController.navigate("main_screen") {
                popUpTo("splash_screen") { inclusive = true }
            }
        } catch (e: Exception) {
            // Fallback navigation if needed
        }
    }
    
    // Pure AMOLED black background for contrast and battery saving
    val amoledBackground = Color.Black
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(amoledBackground),
        contentAlignment = Alignment.Center
    ) {
        // Floating stardust particles
        FloatingStardust(
            modifier = Modifier.fillMaxSize(),
            offset = particleOffset,
            screenWidth = screenWidth,
            screenHeight = screenHeight
        )
        
        // Kconvert logo at center with breathing effect
        KconvertLogoV3(
            modifier = Modifier
                .scale(logoScale * logoPulseScale)
                .alpha(if (isVisible) logoPulseAlpha else 0f),
            glowAlpha = logoGlowAlpha,
            size = 140.dp
        )
        
        // Optimized plasma loading circle
        OptimizedPlasmaCircle(
            modifier = Modifier.fillMaxSize(),
            progress = circleProgress,
            rotation = plasmaRotation,
            flashAlpha = circleFlashAlpha,
            logoSize = 140.dp,
            minScreenDimension = minScreenDimension,
            isLowEndDevice = isLowEndDevice,
            cachedBrush = plasmaBrush
        )
    }
}

/**
 * Optimized floating stardust particles background
 */
@Composable
private fun FloatingStardust(
    modifier: Modifier = Modifier,
    offset: Float,
    screenWidth: androidx.compose.ui.unit.Dp,
    screenHeight: androidx.compose.ui.unit.Dp
) {
    val particles = remember {
        (1..15).map { // Reduced particles for better performance
            Particle(
                x = Random.nextFloat() * screenWidth.value,
                y = Random.nextFloat() * screenHeight.value,
                size = Random.nextFloat() * 1.5f + 0.5f,
                speed = Random.nextFloat() * 0.2f + 0.05f,
                brightness = Random.nextFloat() * 0.3f + 0.2f
            )
        }
    }
    
    Canvas(modifier = modifier) {
        particles.forEach { particle ->
            val currentY = (particle.y - (offset * particle.speed * size.height)) % size.height
            val adjustedY = if (currentY < 0) size.height + currentY else currentY
            
            // Main particle (very subtle)
            drawCircle(
                color = Color.White,
                radius = particle.size,
                center = Offset(particle.x * density, adjustedY),
                alpha = particle.brightness * 0.6f
            )
        }
    }
}

/**
 * Clean Kconvert logo with subtle breathing aura for V3
 */
@Composable
private fun KconvertLogoV3(
    modifier: Modifier = Modifier,
    glowAlpha: Float,
    size: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Soft aura behind logo (breathing light)
        Box(
            modifier = Modifier
                .size(size * 1.8f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00FFFF).copy(alpha = glowAlpha * 0.2f), // Cyan center
                            Color(0xFF60A5FA).copy(alpha = glowAlpha * 0.1f), // Neon blue
                            Color.Transparent
                        ),
                        radius = size.value * 0.9f
                    )
                )
        )
        
        // Main logo
        Image(
            painter = painterResource(id = R.drawable.kconvert_logo_orig),
            contentDescription = "Kconvert Logo",
            modifier = Modifier
                .size(size)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * GPU-Optimized plasma loading circle with cached brushes and PathEffect
 */
@Composable
private fun OptimizedPlasmaCircle(
    modifier: Modifier = Modifier,
    progress: Float,
    rotation: Float,
    flashAlpha: Float,
    logoSize: androidx.compose.ui.unit.Dp,
    minScreenDimension: Int,
    isLowEndDevice: Boolean,
    cachedBrush: Brush?
) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = logoSize.toPx() * 0.8f
        
        if (progress > 0f) {
            if (isLowEndDevice) {
                // Simple fallback for low-end devices
                drawSimpleProgressRing(
                    centerX = centerX,
                    centerY = centerY,
                    radius = radius,
                    progress = progress,
                    flashAlpha = flashAlpha,
                    minScreenDimension = minScreenDimension
                )
            } else {
                // High-performance plasma effect for capable devices
                drawOptimizedPlasmaRing(
                    centerX = centerX,
                    centerY = centerY,
                    radius = radius,
                    progress = progress,
                    rotation = rotation,
                    flashAlpha = flashAlpha,
                    minScreenDimension = minScreenDimension,
                    cachedBrush = cachedBrush
                )
            }
        }
    }
}

/**
 * High-performance plasma ring with cached brushes
 */
private fun DrawScope.drawOptimizedPlasmaRing(
    centerX: Float,
    centerY: Float,
    radius: Float,
    progress: Float,
    rotation: Float,
    flashAlpha: Float,
    minScreenDimension: Int,
    cachedBrush: Brush?
) {
    // Background ring (minimal overdraw)
    drawCircle(
        color = Color(0xFF60A5FA),
        radius = radius,
        center = Offset(centerX, centerY),
        style = Stroke(width = 2.dp.toPx()),
        alpha = 0.15f
    )
    
    // Progressive plasma arc - solid continuous line with adaptive scaling
    val sweepAngle = progress * 360f
    val strokeWidth = minScreenDimension * 0.02f // Adaptive stroke width for all screen sizes
    
    // Rotating sweep gradient for smooth plasma effect
    val rotatingGradient = Brush.sweepGradient(
        colors = listOf(
            Color(0xFF00FFFF), // Cyan
            Color(0xFF0080FF), // Neon blue
            Color(0xFF60A5FA), // Light blue
            Color(0xFF00FFFF)  // Cyan (seamless loop)
        ),
        center = Offset(centerX, centerY)
    )
    
    // Main plasma arc - solid continuous line
    drawArc(
        brush = rotatingGradient,
        startAngle = -90f + rotation,
        sweepAngle = sweepAngle,
        useCenter = false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        ),
        topLeft = Offset(centerX - radius, centerY - radius),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
    )
    
    // Subtle outer glow for continuous plasma line
    if (progress > 0.1f) {
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color(0xFF00FFFF).copy(alpha = 0.3f),
                    Color(0xFF0080FF).copy(alpha = 0.4f),
                    Color(0xFF60A5FA).copy(alpha = 0.2f),
                    Color(0xFF00FFFF).copy(alpha = 0.3f)
                ),
                center = Offset(centerX, centerY)
            ),
            startAngle = -90f + rotation,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(
                width = strokeWidth * 1.8f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            ),
            topLeft = Offset(centerX - radius, centerY - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            alpha = 0.6f
        )
    }
    
    // Flash effect (reduced overdraw)
    if (flashAlpha > 0f) {
        drawCircle(
            color = Color.White,
            radius = radius + 15f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 6.dp.toPx()),
            alpha = flashAlpha * 0.7f
        )
    }
}

/**
 * Simple fallback for low-end devices
 */
private fun DrawScope.drawSimpleProgressRing(
    centerX: Float,
    centerY: Float,
    radius: Float,
    progress: Float,
    flashAlpha: Float,
    minScreenDimension: Int
) {
    // Background ring
    drawCircle(
        color = Color(0xFF60A5FA),
        radius = radius,
        center = Offset(centerX, centerY),
        style = Stroke(width = 3.dp.toPx()),
        alpha = 0.3f
    )
    
    // Progress arc (simple solid color with adaptive width)
    val sweepAngle = progress * 360f
    val strokeWidth = minScreenDimension * 0.02f // Adaptive stroke width
    
    drawArc(
        color = Color(0xFF00FFFF),
        startAngle = -90f,
        sweepAngle = sweepAngle,
        useCenter = false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        ),
        topLeft = Offset(centerX - radius, centerY - radius),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
    )
    
    // Simple flash effect
    if (flashAlpha > 0f) {
        drawCircle(
            color = Color.White,
            radius = radius + 10f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 4.dp.toPx()),
            alpha = flashAlpha * 0.6f
        )
    }
}

/**
 * Create cached plasma brush for performance
 */
private fun createCachedPlasmaShader(
    density: androidx.compose.ui.unit.Density,
    screenWidth: androidx.compose.ui.unit.Dp,
    screenHeight: androidx.compose.ui.unit.Dp
): Brush? {
    return try {
        with(density) {
            val centerX = screenWidth.toPx() / 2f
            val centerY = screenHeight.toPx() / 2f
            
            // Use Compose Brush for GPU optimization
            Brush.radialGradient(
                colors = listOf(
                    Color(0xFF00FFFF),
                    Color(0xFF0080FF),
                    Color(0xFF60A5FA)
                ),
                center = Offset(centerX, centerY),
                radius = 200f
            )
        }
    } catch (e: Exception) {
        null // Fallback to solid colors
    }
}

/**
 * Data class for particle properties
 */
private data class Particle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val brightness: Float
)
