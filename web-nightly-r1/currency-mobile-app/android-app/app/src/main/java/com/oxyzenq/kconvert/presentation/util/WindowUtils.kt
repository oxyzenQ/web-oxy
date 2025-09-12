/*
 * Window utils for immersive mode
 */
package com.oxyzenq.kconvert.presentation.util

import android.app.Activity
import android.os.Build
import android.view.WindowInsets
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

fun setImmersiveMode(activity: Activity, enabled: Boolean) {
    val window = activity.window
    // Always draw edge-to-edge so the wallpaper can extend behind system bars
    WindowCompat.setDecorFitsSystemWindows(window, false)
    // Allow content into display cutout (notch) areas
    @Suppress("DEPRECATION")
    window.attributes = window.attributes.apply {
        layoutInDisplayCutoutMode = android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }

    val controller = WindowInsetsControllerCompat(window, window.decorView)
    if (enabled) {
        // Full immersive - hide status and nav bars
        controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Keep bar surfaces transparent so when they transiently appear, wallpaper still shows
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            @Suppress("DEPRECATION")
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            @Suppress("DEPRECATION") 
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        }
    } else {
        // Show bars but keep them transparent so the background remains visible
        controller.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            @Suppress("DEPRECATION")
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            @Suppress("DEPRECATION")
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        }
        // Optional: ensure icons are legible on dark wallpaper (light status bar icons)
        // You can adjust this based on theme if desired
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false
    }
}
