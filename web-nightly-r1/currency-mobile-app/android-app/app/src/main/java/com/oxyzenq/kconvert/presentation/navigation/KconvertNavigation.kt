/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.oxyzenq.kconvert.presentation.screen.KconvertMainScreen
import com.oxyzenq.kconvert.presentation.screen.SplashScreen

/**
 * Navigation setup for Kconvert app
 */
@Composable
fun KconvertNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "splash_screen"
    ) {
        // Splash Screen
        composable("splash_screen") {
            SplashScreen(navController = navController)
        }
        
        // Main Screen
        composable("main_screen") {
            KconvertMainScreen()
        }
    }
}

/**
 * Navigation routes constants
 */
object NavigationRoutes {
    const val SPLASH_SCREEN = "splash_screen"
    const val MAIN_SCREEN = "main_screen"
}
