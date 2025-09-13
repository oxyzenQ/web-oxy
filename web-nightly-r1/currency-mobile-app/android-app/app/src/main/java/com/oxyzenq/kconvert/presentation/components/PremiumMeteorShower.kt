/*
 * Creativity Authored by oxyzenq 2025
 * 
 * Premium Meteor Shower Animation
 * High-performance, 60 FPS meteor shower with Unicode characters
 * Optimized for smooth rendering and minimal resource usage
 */

package com.oxyzenq.kconvert.presentation.components

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
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
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
    
    // Animation trigger state for Canvas invalidation
    var animationTick by remember { mutableStateOf(0L) }
    
    // Independent animation timer - completely separate from Compose
    val animationTimer = remember {
        Executors.newSingleThreadScheduledExecutor()
    }
    // Keep reference to the scheduled task so it can be canceled when disabled
    val scheduledFutureRef = remember { mutableStateOf<ScheduledFuture<*>?>(null) }
    
    // Initialize meteors when screen size changes
    LaunchedEffect(screenSize.width) {
        if (screenSize.width > 0) {
            // Initialize meteors at startup
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
    
    // Start/stop independent animation timer
    LaunchedEffect(isActive, screenSize.width) {
        // Always cancel any existing task before (re)starting
        scheduledFutureRef.value?.cancel(false)
        scheduledFutureRef.value = null

        if (isActive && screenSize.width > 0) {
            // Immediately seed a few meteors so the user sees activity right away
            meteorPool.take(3).forEach { meteor ->
                if (!meteor.isActive) {
                    meteor.reset(
                        screenWidth = screenSize.width.toFloat(),
                        newColor = MeteorColorPalette.colors.random(),
                        newSpeed = Random.nextFloat() * (config.maxSpeed - config.minSpeed) + config.minSpeed,
                        newHeadSize = Random.nextFloat() * (config.maxHeadSize - config.minHeadSize) + config.minHeadSize,
                        newTailLength = Random.nextInt(config.minTailLength, config.maxTailLength + 1)
                    )
                    // Start from slightly above the screen for a natural entrance
                    meteor.y = -50f
                }
            }
            val animationTask = Runnable {
                // Update meteors on background thread
                meteorPool.forEach { meteor ->
                    if (meteor.isActive) {
                        meteor.y += meteor.speed
                        
                        // Reset meteor when it goes off screen
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
                
                // Trigger Canvas redraw by updating state
                animationTick = System.currentTimeMillis()
            }
            
            // Schedule animation at 60 FPS (16ms intervals)
            val scheduledFuture = animationTimer.scheduleAtFixedRate(
                animationTask,
                0,
                16,
                TimeUnit.MILLISECONDS
            )
            scheduledFutureRef.value = scheduledFuture
            
        } else {
            // When inactive: mark meteors inactive and stop updating
            meteorPool.forEach { it.isActive = false }
        }
    }
    
    // Cleanup timer when component is disposed
    DisposableEffect(Unit) {
        onDispose {
            // Cancel any running task and shutdown executor
            scheduledFutureRef.value?.cancel(false)
            scheduledFutureRef.value = null
            animationTimer.shutdown()
        }
    }
    
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                screenSize = size
            }
    ) {
        // Force Canvas to observe animation tick for redraws
        animationTick // This triggers recomposition when animationTick changes
        
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
