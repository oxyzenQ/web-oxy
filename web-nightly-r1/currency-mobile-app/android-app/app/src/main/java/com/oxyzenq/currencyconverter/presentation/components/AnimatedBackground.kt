/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.oxyzenq.kconvert.R

/**
 * Static background component without animations
 */
@Composable
fun AnimatedBackground(isScrolling: Boolean, isFullscreen: Boolean = true, darkLevel: Int = 0) {

    Box(modifier = Modifier.fillMaxSize()) {
        // Static background wallpaper without animations
        Image(
            painter = painterResource(id = R.drawable.hdr_stellar_edition_v2),
            contentDescription = "HDR Stellar Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            colorFilter = if (isFullscreen) {
                ColorFilter.tint(
                    Color.Black.copy(alpha = 0.18f),
                    blendMode = BlendMode.Darken
                )
            } else null
        )

        // Extra adjustable dark overlay from Settings (0..100 => 0..0.5 alpha)
        val adjustableAlpha = (darkLevel.coerceIn(0, 100) / 100f) * 0.5f
        if (adjustableAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = adjustableAlpha))
            )
        }

        // Subtle overlay gradient: no top darkening in either mode; just a soft bottom vignette
        val topAlpha = 0.0f
        val bottomAlpha = if (isFullscreen) 0.24f else 0.16f
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = topAlpha),
                            Color.Black.copy(alpha = bottomAlpha)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )
    }
}

