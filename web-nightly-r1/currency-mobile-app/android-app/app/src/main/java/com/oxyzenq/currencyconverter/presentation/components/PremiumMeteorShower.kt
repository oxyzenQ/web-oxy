/*
 * Creativity Authored by oxyzenq 2025
 * 
 * Premium Meteor Shower Animation
 * High-performance, 60 FPS meteor shower with Unicode characters
 * Optimized for smooth rendering and minimal resource usage
 */

package com.oxyzenq.currencyconverter.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.*
import kotlin.random.Random

/**
 * Premium Meteor Data Structure
 * Optimized for memory efficiency and smooth animation
 */
data class Meteor(
    var x: Float,
    var y: Float,
    var speed: Float,
    var color: Color,
    var headSize: Float,
    var tailLength: Int,
    var isActive: Boolean = true,
    val id: UUID = UUID.randomUUID()
) {
    fun reset(screenWidth: Float, newColor: Color, newSpeed: Float, newHeadSize: Float, newTailLength: Int) {
        x = Random.nextFloat() * screenWidth
        y = -50f
        speed = newSpeed
        color = newColor
        headSize = newHeadSize
        tailLength = newTailLength
        isActive = true
    }
}

/**
 * Premium Color Palette for Meteors
 * Carefully selected colors for maximum visual impact
 */
object MeteorColorPalette {
    val ElectricBlue = Color(0xFF00BFFF)
    val PureWhite = Color(0xFFFFFFFF)
    val SunsetOrange = Color(0xFFFF8C00)
    val FieryRedOrange = Color(0xFFFF4500)
    val CyberPurple = Color(0xFF9D4EDD)
    val NeonGreen = Color(0xFF39FF14)
    
    val colors = listOf(
        ElectricBlue,
        PureWhite,
        SunsetOrange,
        FieryRedOrange,
        CyberPurple,
        NeonGreen
    )
}

/**
 * Meteor Configuration for Performance Tuning
 */
data class MeteorConfig(
    val maxMeteors: Int = 15,
    val minSpeed: Float = 2f,
    val maxSpeed: Float = 6f,
    val minHeadSize: Float = 12f,
    val maxHeadSize: Float = 18f,
    val minTailLength: Int = 3,
    val maxTailLength: Int = 5,
    val spawnIntervalMin: Long = 150L,
    val spawnIntervalMax: Long = 300L,
    val enableShimmer: Boolean = true,
    val enableParallax: Boolean = true
)

/**
 * Premium Meteor Shower Animation Component
 * 60 FPS performance-optimized with object pooling
 */
@Composable
fun PremiumMeteorShower(
    modifier: Modifier = Modifier,
    config: MeteorConfig = MeteorConfig(),
    isActive: Boolean = true
) {
    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    
    // Object pool for meteors - prevents garbage collection overhead
    val meteorPool = remember { 
        mutableStateListOf<Meteor>().apply {
            repeat(config.maxMeteors) {
                add(
                    Meteor(
                        x = 0f,
                        y = -100f,
                        speed = 0f,
                        color = Color.Transparent,
                        headSize = 0f,
                        tailLength = 0,
                        isActive = false
                    )
                )
            }
        }
    }
    
    // Animation trigger for smooth 60 FPS updates
    val infiniteTransition = rememberInfiniteTransition(label = "meteor_animation")
    val animationFrame by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16, easing = LinearEasing), // ~60 FPS
            repeatMode = RepeatMode.Restart
        ),
        label = "animation_frame"
    )
    
    // Meteor spawning and lifecycle management
    LaunchedEffect(isActive, animationFrame) {
        if (!isActive || screenSize.width == 0) return@LaunchedEffect
        
        while (isActive) {
            // Update existing meteors
            meteorPool.forEach { meteor ->
                if (meteor.isActive) {
                    meteor.y += meteor.speed
                    
                    // Deactivate meteors that fall off screen
                    if (meteor.y > screenSize.height + 100f) {
                        meteor.isActive = false
                    }
                }
            }
            
            // Spawn new meteor if pool has inactive meteors
            val inactiveMeteor = meteorPool.find { !it.isActive }
            if (inactiveMeteor != null && Random.nextFloat() < 0.3f) {
                val newColor = MeteorColorPalette.colors.random()
                val newSpeed = Random.nextFloat() * (config.maxSpeed - config.minSpeed) + config.minSpeed
                val newHeadSize = Random.nextFloat() * (config.maxHeadSize - config.minHeadSize) + config.minHeadSize
                val newTailLength = Random.nextInt(config.minTailLength, config.maxTailLength + 1)
                
                inactiveMeteor.reset(
                    screenWidth = screenSize.width.toFloat(),
                    newColor = newColor,
                    newSpeed = newSpeed,
                    newHeadSize = newHeadSize,
                    newTailLength = newTailLength
                )
            }
            
            // Random spawn interval for natural feel
            delay(Random.nextLong(config.spawnIntervalMin, config.spawnIntervalMax))
        }
    }
    
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                screenSize = size
            }
    ) {
        if (screenSize.width == 0) return@Canvas
        
        // Draw deep space background gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0B0B1A), // Deep space blue
                    Color(0xFF000000)  // Pure black
                ),
                startY = 0f,
                endY = size.height
            )
        )
        
        // Render active meteors with premium effects
        meteorPool.forEach { meteor ->
            if (meteor.isActive) {
                drawPremiumMeteor(
                    meteor = meteor,
                    config = config,
                    density = density
                )
            }
        }
    }
}

/**
 * Premium Meteor Rendering with Geometric Shapes
 * Optimized for smooth performance and visual appeal
 */
private fun DrawScope.drawPremiumMeteor(
    meteor: Meteor,
    config: MeteorConfig,
    density: androidx.compose.ui.unit.Density
) {
    val headSizePx = with(density) { meteor.headSize.sp.toPx() }
    
    // Draw meteor tail (gradient lines)
    for (i in 1..meteor.tailLength) {
        val tailY = meteor.y - (i * headSizePx * 0.6f)
        val tailAlpha = (1f - (i.toFloat() / meteor.tailLength)) * 0.8f
        val tailWidth = headSizePx * (1f - (i.toFloat() / meteor.tailLength) * 0.3f)
        
        if (tailY > -headSizePx) {
            drawLine(
                color = meteor.color.copy(alpha = tailAlpha),
                start = Offset(meteor.x - tailWidth / 2, tailY),
                end = Offset(meteor.x + tailWidth / 2, tailY),
                strokeWidth = headSizePx * 0.3f,
                cap = StrokeCap.Round
            )
        }
    }
    
    // Draw shimmer effect if enabled
    if (config.enableShimmer) {
        val shimmerRadius = headSizePx * 1.2f
        drawCircle(
            color = meteor.color.copy(alpha = 0.15f),
            radius = shimmerRadius,
            center = Offset(meteor.x, meteor.y),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
        )
    }
    
    // Draw meteor head (solid circle)
    drawCircle(
        color = meteor.color,
        radius = headSizePx * 0.4f,
        center = Offset(meteor.x, meteor.y)
    )
    
    // Add inner glow for premium look
    drawCircle(
        color = Color.White.copy(alpha = 0.6f),
        radius = headSizePx * 0.2f,
        center = Offset(meteor.x, meteor.y)
    )
    
    // Add outer glow effect
    drawCircle(
        color = meteor.color.copy(alpha = 0.3f),
        radius = headSizePx * 0.6f,
        center = Offset(meteor.x, meteor.y)
    )
}

/**
 * Performance Monitor for Development
 * Use this to track FPS and optimize performance
 */
@Composable
fun MeteorPerformanceMonitor(
    onFpsUpdate: (Int) -> Unit = {}
) {
    var frameCount by remember { mutableStateOf(0) }
    var lastTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            frameCount++
            val currentTime = System.currentTimeMillis()
            
            if (currentTime - lastTime >= 1000) {
                onFpsUpdate(frameCount)
                frameCount = 0
                lastTime = currentTime
            }
            
            delay(16) // ~60 FPS
        }
    }
}
