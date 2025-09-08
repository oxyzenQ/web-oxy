/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.oxyzenq.kconvert.R
import kotlinx.coroutines.delay

/**
 * Splash Screen with animated logo and rotating gear
 */
@Composable
fun SplashScreen(navController: NavController) {
    var isVisible by remember { mutableStateOf(false) }
    var shouldNavigate by remember { mutableStateOf(false) }
    
    // Infinite rotation for gear
    val infiniteTransition = rememberInfiniteTransition(label = "gear_rotation")
    val gearRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "gear_rotation_angle"
    )
    
    // Splash screen timing and navigation
    LaunchedEffect(Unit) {
        // Start fade-in animation immediately
        isVisible = true
        
        // Wait for 2 seconds total splash duration
        delay(2000)
        
        // Start fade-out and navigate
        isVisible = false
        delay(300) // Wait for fade-out animation to complete
        
        // Navigate to main screen
        navController.navigate("main_screen") {
            popUpTo("splash_screen") { inclusive = true }
        }
    }
    
    // Background pure black
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Animated content
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = 600,
                    easing = FastOutSlowInEasing
                )
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // App Logo (2x bigger)
                Image(
                    painter = painterResource(id = R.drawable.kconvert_logo_new),
                    contentDescription = "Kconvert Logo",
                    modifier = Modifier
                        .size(240.dp)
                        .padding(bottom = 32.dp),
                    contentScale = ContentScale.Fit
                )
                
                // Rotating Gear Icon
                Image(
                    painter = painterResource(id = R.drawable.ic_gear),
                    contentDescription = "Loading Gear",
                    modifier = Modifier
                        .size(48.dp)
                        .rotate(gearRotation),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Color(0xFF9CA3AF))
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Loading text using Inter font
                val inter = FontFamily(Font(R.font.inter))
                Text(
                    text = "Loading....",
                    style = MaterialTheme.typography.body2.copy(
                        color = Color(0xFF9CA3AF),
                        fontFamily = inter
                    )
                )
            }
        }
    }
}
