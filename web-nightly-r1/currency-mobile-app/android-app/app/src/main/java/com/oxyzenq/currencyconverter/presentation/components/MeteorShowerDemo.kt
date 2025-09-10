/*
 * Creativity Authored by oxyzenq 2025
 * 
 * Premium Meteor Shower Demo Integration
 * Example implementation showing how to use the meteor shower animation
 */

package com.oxyzenq.currencyconverter.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Demo Screen showing Premium Meteor Shower Animation
 * Perfect for splash screens, loading screens, or background effects
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeteorShowerDemo(
    modifier: Modifier = Modifier
) {
    var isAnimationActive by remember { mutableStateOf(true) }
    var currentFps by remember { mutableStateOf(0) }
    
    // Custom configuration for different effects
    val lightConfig = MeteorConfig(
        maxMeteors = 8,
        minSpeed = 1.5f,
        maxSpeed = 3f,
        enableShimmer = false,
        spawnIntervalMin = 400L,
        spawnIntervalMax = 800L
    )
    
    val heavyConfig = MeteorConfig(
        maxMeteors = 20,
        minSpeed = 3f,
        maxSpeed = 8f,
        enableShimmer = true,
        spawnIntervalMin = 100L,
        spawnIntervalMax = 200L
    )
    
    var currentConfig by remember { mutableStateOf(lightConfig) }
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Premium Meteor Shower Background
        PremiumMeteorShower(
            modifier = Modifier.fillMaxSize(),
            config = currentConfig,
            isActive = isAnimationActive
        )
        
        // Performance Monitor (optional)
        MeteorPerformanceMonitor { fps ->
            currentFps = fps
        }
        
        // Demo Controls
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            // FPS Display
            Text(
                text = "FPS: $currentFps",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Animation Toggle
            Button(
                onClick = { isAnimationActive = !isAnimationActive },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.2f)
                )
            ) {
                Text(
                    text = if (isAnimationActive) "Pause" else "Resume",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Configuration Toggle
            Button(
                onClick = { 
                    currentConfig = if (currentConfig == lightConfig) heavyConfig else lightConfig
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.2f)
                )
            ) {
                Text(
                    text = if (currentConfig == lightConfig) "Heavy" else "Light",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
        
        // Demo Content Overlay
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Premium Meteor Shower",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "60 FPS • Object Pooled • Performance Optimized",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "✨ Electric Blue • Pure White • Sunset Orange\n🔥 Fiery Red-Orange • Cyber Purple • Neon Green",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * Integration Example for Splash Screen
 */
@Composable
fun SplashScreenWithMeteors(
    onSplashComplete: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000) // 3 second splash
        onSplashComplete()
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Meteor shower background
        PremiumMeteorShower(
            modifier = Modifier.fillMaxSize(),
            config = MeteorConfig(
                maxMeteors = 12,
                enableShimmer = true,
                spawnIntervalMin = 200L,
                spawnIntervalMax = 400L
            )
        )
        
        // App logo and content
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Kconvert",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Currency Converter",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Integration Example for Loading Screen
 */
@Composable
fun LoadingScreenWithMeteors(
    isLoading: Boolean = true,
    loadingText: String = "Loading..."
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Light meteor shower for loading
            PremiumMeteorShower(
                modifier = Modifier.fillMaxSize(),
                config = MeteorConfig(
                    maxMeteors = 6,
                    minSpeed = 1f,
                    maxSpeed = 3f,
                    enableShimmer = false,
                    spawnIntervalMin = 500L,
                    spawnIntervalMax = 1000L
                )
            )
            
            // Loading indicator
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 3.dp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = loadingText,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}
