/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.currencyconverter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = PlasmaBlue,
    primaryVariant = PlasmaPurple,
    secondary = NebulaGlow,
    background = DeepSpaceBlue,
    surface = DistantNebulaBlue,
    onPrimary = SoftWhite,
    onSecondary = SoftWhite,
    onBackground = SoftWhite,
    onSurface = LightBluishWhite,
)

private val LightColorPalette = lightColors(
    primary = LightPrimary,
    primaryVariant = LightSecondary,
    secondary = PlasmaPurple,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = LightOnPrimary,
    onSecondary = LightOnSecondary,
    onBackground = DeepSpaceBlue,
    onSurface = DarkGalacticBlack,
)

@Composable
fun CurrencyConverterTheme(
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
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
