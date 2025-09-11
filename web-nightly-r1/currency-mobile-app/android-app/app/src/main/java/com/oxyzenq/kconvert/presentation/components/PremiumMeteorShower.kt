/*
 * Creativity Authored by oxyzenq 2025
 * 
 * Premium Meteor Shower Animation
 * High-performance, 60 FPS meteor shower with Unicode characters
 * Optimized for smooth rendering and minimal resource usage
 */

package com.oxyzenq.kconvert.presentation.components

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
 * Limited to blue and white for clean aesthetic
 */
object MeteorColorPalette {
    val ElectricBlue = Color(0xFF00BFFF)
    val PureWhite = Color(0xFFFFFFFF)
    
    val colors = listOf(
        ElectricBlue,
        PureWhite
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
    
    // Animation state that forces recomposition
    val infiniteTransition = rememberInfiniteTransition(label = "meteor_animation")
    val animationTrigger by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "animation_trigger"
    )
    
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
    
    // Initialize meteors when screen size is available
    LaunchedEffect(screenSize.width) {
        if (screenSize.width > 0 && isActive) {
            // Initialize some meteors at startup
            meteorPool.take(3).forEach { meteor ->
                meteor.reset(
                    screenWidth = screenSize.width.toFloat(),
                    newColor = MeteorColorPalette.colors.random(),
                    newSpeed = Random.nextFloat() * (config.maxSpeed - config.minSpeed) + config.minSpeed,
                    newHeadSize = Random.nextFloat() * (config.maxHeadSize - config.minHeadSize) + config.minHeadSize,
                    newTailLength = Random.nextInt(config.minTailLength, config.maxTailLength + 1)
                )
                // Stagger initial positions
                meteor.y = Random.nextFloat() * screenSize.height
            }
        }
    }
    
    // Update meteor positions on every animation frame
    LaunchedEffect(animationTrigger, isActive) {
        if (!isActive || screenSize.width == 0) return@LaunchedEffect
        
        // Update existing meteors position
        meteorPool.forEach { meteor ->
            if (meteor.isActive) {
                meteor.y += meteor.speed
                
                // Reset meteor when it goes off screen (infinite loop)
                if (meteor.y > screenSize.height + 100f) {
                    meteor.reset(
                        screenWidth = screenSize.width.toFloat(),
                        newColor = MeteorColorPalette.colors.random(),
                        newSpeed = Random.nextFloat() * (config.maxSpeed - config.minSpeed) + config.minSpeed,
                        newHeadSize = Random.nextFloat() * (config.maxHeadSize - config.minHeadSize) + config.minHeadSize,
                        newTailLength = Random.nextInt(config.minTailLength, config.maxTailLength + 1)
                    )
                }
            }
        }
        
        // Spawn new meteor occasionally
        val inactiveMeteor = meteorPool.find { !it.isActive }
        if (inactiveMeteor != null && Random.nextFloat() < 0.02f) {
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
    }
    
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                screenSize = size
            }
    ) {
        if (screenSize.width == 0) return@Canvas
        
        // NO BACKGROUND - Let wallpaper show through
        // Render active meteors with premium effects on transparent canvas
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
