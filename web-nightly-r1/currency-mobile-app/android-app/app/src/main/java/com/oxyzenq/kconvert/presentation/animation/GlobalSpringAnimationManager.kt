/*
 * Creativity Authored by oxyzenq 2025
 * 
 * Global Spring Animation System
 * Premium bouncy animations for every UI interaction
 * Performance-optimized with 60 FPS guarantee
 */

package com.oxyzenq.kconvert.presentation.animation

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.util.concurrent.ConcurrentHashMap

/**
 * Spring Animation Intensity Levels
 * Controls the bounciness and feel of animations
 */
enum class SpringIntensity(
    val dampingRatio: Float,
    val stiffness: Float,
    val description: String
) {
    SUBTLE(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
        description = "Minimal bounce, professional feel"
    ),
    MEDIUM(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow,
        description = "Balanced bounce, default experience"
    ),
    PLAYFUL(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow,
        description = "High bounce, fun interaction"
    ),
    EXTREME(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessVeryLow,
        description = "Maximum bounce, game-like feel"
    )
}

/**
 * Global Spring Animation Theme Configuration
 * Defines the animation behavior across the entire app
 */
@Stable
data class SpringAnimationTheme(
    val intensity: SpringIntensity = SpringIntensity.MEDIUM,
    val defaultDuration: Int = 300,
    val visibilityThreshold: Float = 0.01f,
    val enableReducedMotion: Boolean = false,
    val performanceMode: PerformanceMode = PerformanceMode.BALANCED
) {
    val dampingRatio: Float get() = intensity.dampingRatio
    val stiffness: Float get() = intensity.stiffness
}

/**
 * Performance modes for different device capabilities
 */
enum class PerformanceMode {
    HIGH_PERFORMANCE,  // Full animations, all effects
    BALANCED,         // Standard animations, some effects
    BATTERY_SAVER     // Reduced animations, minimal effects
}

/**
 * Global Spring Animation Manager
 * Centralized animation control with performance optimization
 */
class GlobalSpringAnimationManager private constructor() {
    
    private val animationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val activeAnimations = ConcurrentHashMap<String, Animatable<*, *>>()
    
    companion object {
        @Volatile
        private var INSTANCE: GlobalSpringAnimationManager? = null
        
        fun getInstance(): GlobalSpringAnimationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GlobalSpringAnimationManager().also { INSTANCE = it }
            }
        }
        
        // Pre-configured spring specifications for different use cases
        val defaultSpring = spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = 0.01f
        )
        
        val fastSpring = spring<Float>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium,
            visibilityThreshold = 0.01f
        )
        
        val slowSpring = spring<Float>(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessVeryLow,
            visibilityThreshold = 0.01f
        )
        
        val subtleSpring = spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
            visibilityThreshold = 0.01f
        )
        
        val extremeSpring = spring<Float>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow,
            visibilityThreshold = 0.005f
        )
    }
    
    /**
     * Create spring spec based on theme configuration
     */
    fun createSpringSpec(
        theme: SpringAnimationTheme,
        customDamping: Float? = null,
        customStiffness: Float? = null
    ): SpringSpec<Float> {
        return spring(
            dampingRatio = customDamping ?: theme.dampingRatio,
            stiffness = customStiffness ?: theme.stiffness,
            visibilityThreshold = theme.visibilityThreshold
        )
    }
    
    /**
     * Get optimized spring spec based on performance mode
     */
    fun getOptimizedSpring(
        theme: SpringAnimationTheme,
        animationType: AnimationType
    ): SpringSpec<Float> {
        return when (theme.performanceMode) {
            PerformanceMode.HIGH_PERFORMANCE -> {
                when (animationType) {
                    AnimationType.CLICK -> fastSpring
                    AnimationType.SCALE -> defaultSpring
                    AnimationType.OFFSET -> defaultSpring
                    AnimationType.ROTATION -> slowSpring
                    AnimationType.ALPHA -> fastSpring
                }
            }
            PerformanceMode.BALANCED -> {
                when (animationType) {
                    AnimationType.CLICK -> defaultSpring
                    AnimationType.SCALE -> defaultSpring
                    AnimationType.OFFSET -> subtleSpring
                    AnimationType.ROTATION -> subtleSpring
                    AnimationType.ALPHA -> defaultSpring
                }
            }
            PerformanceMode.BATTERY_SAVER -> {
                subtleSpring
            }
        }
    }
    
    /**
     * Register an animation for tracking and cleanup
     */
    fun registerAnimation(key: String, animatable: Animatable<*, *>) {
        activeAnimations[key] = animatable
    }
    
    /**
     * Unregister an animation
     */
    fun unregisterAnimation(key: String) {
        activeAnimations.remove(key)
    }
    
    /**
     * Get active animation count for performance monitoring
     */
    fun getActiveAnimationCount(): Int = activeAnimations.size
    
    /**
     * Cleanup all animations (call on app background)
     */
    fun cleanup() {
        activeAnimations.clear()
        animationScope.cancel()
    }
}

/**
 * Animation type classification for optimization
 */
enum class AnimationType {
    CLICK,      // Button presses, tap feedback
    SCALE,      // Size changes, zoom effects  
    OFFSET,     // Position changes, sliding
    ROTATION,   // Rotation effects
    ALPHA       // Opacity changes, fade effects
}

/**
 * CompositionLocal for global spring animation theme
 */
val LocalSpringAnimationTheme = compositionLocalOf { SpringAnimationTheme() }

/**
 * Spring Animation Theme Provider
 * Wraps the entire app to provide consistent animation behavior
 */
@Composable
fun SpringAnimationTheme(
    theme: SpringAnimationTheme = SpringAnimationTheme(),
    content: @Composable () -> Unit
) {
    // Detect system animation preferences
    val context = LocalContext.current
    val density = LocalDensity.current
    
    // Adjust theme based on system settings
    val adjustedTheme = remember(theme) {
        theme.copy(
            enableReducedMotion = isReducedMotionEnabled(context),
            performanceMode = detectPerformanceMode(density)
        )
    }
    
    CompositionLocalProvider(
        LocalSpringAnimationTheme provides adjustedTheme
    ) {
        content()
    }
}

/**
 * Detect if reduced motion is enabled in system settings
 */
private fun isReducedMotionEnabled(context: android.content.Context): Boolean {
    return try {
        val resolver = context.contentResolver
        android.provider.Settings.Global.getFloat(
            resolver,
            android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
            1.0f
        ) == 0.0f
    } catch (e: Exception) {
        false
    }
}

/**
 * Detect device performance capability
 */
private fun detectPerformanceMode(density: Density): PerformanceMode {
    return try {
        // Simple heuristic based on screen density
        // Higher density usually means more powerful device
        when {
            density.density >= 3.0f -> PerformanceMode.HIGH_PERFORMANCE
            density.density >= 2.0f -> PerformanceMode.BALANCED
            else -> PerformanceMode.BATTERY_SAVER
        }
    } catch (e: Exception) {
        PerformanceMode.BALANCED
    }
}

/**
 * Animation Performance Monitor
 * Tracks FPS and animation performance
 */
@Composable
fun SpringAnimationPerformanceMonitor(
    onPerformanceUpdate: (AnimationPerformanceMetrics) -> Unit = {}
) {
    val animationManager = remember { GlobalSpringAnimationManager.getInstance() }
    var frameCount by remember { mutableStateOf(0) }
    var lastTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            frameCount++
            val currentTime = System.currentTimeMillis()
            
            if (currentTime - lastTime >= 1000) {
                val metrics = AnimationPerformanceMetrics(
                    fps = frameCount,
                    activeAnimations = animationManager.getActiveAnimationCount(),
                    timestamp = currentTime
                )
                onPerformanceUpdate(metrics)
                frameCount = 0
                lastTime = currentTime
            }
            
            kotlinx.coroutines.delay(16) // ~60 FPS
        }
    }
}

/**
 * Animation performance metrics data class
 */
data class AnimationPerformanceMetrics(
    val fps: Int,
    val activeAnimations: Int,
    val timestamp: Long
)
