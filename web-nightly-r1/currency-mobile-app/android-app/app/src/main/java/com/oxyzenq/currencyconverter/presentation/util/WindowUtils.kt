/*
 * Window utils for immersive mode
 */
package com.oxyzenq.kconvert.presentation.util

import android.app.Activity
import android.view.WindowInsets
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

fun setImmersiveMode(activity: Activity, enabled: Boolean) {
    val window = activity.window
    // When immersive is enabled, we do NOT fit system windows so content goes edge to edge
    WindowCompat.setDecorFitsSystemWindows(window, !enabled)

    val controller = WindowInsetsControllerCompat(window, window.decorView)
    if (enabled) {
        controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    } else {
        controller.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
    }
}
