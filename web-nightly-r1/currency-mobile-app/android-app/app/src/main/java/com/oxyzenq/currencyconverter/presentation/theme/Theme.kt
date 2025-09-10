/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.currencyconverter.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.oxyzenq.currencyconverter.presentation.theme.InterTypography
import com.oxyzenq.currencyconverter.presentation.theme.KconvertShapes

private val DarkColorPalette = darkColors(
    primary = Color(0xFF007AFF),
    primaryVariant = Color(0xFF0051D5),
    secondary = Color(0xFF93C5FD),
    background = Color(0xFF0B1530),
    surface = Color(0xFF1E293B),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorPalette = lightColors(
    primary = Color(0xFF007AFF),
    primaryVariant = Color(0xFF0051D5),
    secondary = Color(0xFF93C5FD),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun KconvertTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = InterTypography,
        shapes = KconvertShapes,
        content = content
    )
}
