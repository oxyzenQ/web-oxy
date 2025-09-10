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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
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
        
        // Wait for 6 seconds total splash duration
        delay(6000)
        
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
                    painter = painterResource(id = R.drawable.kconvert_logo_orig),
                    contentDescription = "Kconvert Logo",
                    modifier = Modifier
                        .size(240.dp)
                        .padding(bottom = 32.dp),
                    contentScale = ContentScale.Fit
                )
                
                // Rotating Gear Icon with gradient tint
                Image(
                    painter = painterResource(id = R.drawable.ic_gear),
                    contentDescription = "Loading Gear",
                    modifier = Modifier
                        .size(48.dp)
                        .rotate(gearRotation),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Color(0xFF93C5FD))
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Loading text using Inter font with blue-light plasma gradient
                val inter = FontFamily(Font(R.font.inter))
                val loadingBrush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF93C5FD), // blue-300
                        Color(0xFF81D4FA), // light blue
                        Color(0xFFC4B5FD)  // violet-300
                    )
                )
                val loadingText = buildAnnotatedString {
                    pushStyle(SpanStyle(brush = loadingBrush))
                    append("Loading....")
                    pop()
                }
                Text(
                    text = loadingText,
                    style = MaterialTheme.typography.body2.copy(
                        fontFamily = inter
                    )
                )
            }
        }
    }
}
