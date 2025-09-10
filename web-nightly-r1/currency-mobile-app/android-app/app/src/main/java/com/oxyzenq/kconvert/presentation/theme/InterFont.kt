/*
 * Creativity Authored by oxyzenq 2025
 * 
 * Consolidated Inter Font Configuration
 * This file contains all Inter font settings, typography, and ligature configurations
 * for easy maintenance and consistent application throughout the project.
 */

package com.oxyzenq.kconvert.presentation.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.oxyzenq.kconvert.R

/**
 * Inter Font Family Configuration
 * Includes all font weights with automatic ligature support
 */
val InterFontFamily = FontFamily(
    Font(R.font.inter, FontWeight.Light),
    Font(R.font.inter, FontWeight.Normal),
    Font(R.font.inter, FontWeight.Medium),
    Font(R.font.inter, FontWeight.SemiBold),
    Font(R.font.inter, FontWeight.Bold),
    Font(R.font.inter, FontWeight.ExtraBold)
)

/**
 * Default Inter Typography System
 * Optimized for readability with proper spacing and ligatures
 * This replaces MaterialTheme.typography throughout the app
 */
val InterTypography = Typography(
    defaultFontFamily = InterFontFamily,
    
    h1 = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    
    h2 = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.25).sp
    ),
    
    h3 = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    
    h4 = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.25.sp
    ),
    
    h5 = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    
    h6 = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    
    subtitle1 = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    
    subtitle2 = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    
    body1 = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    
    body2 = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    
    button = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.25.sp
    ),
    
    caption = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    
    overline = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.5.sp
    )
)

/**
 * Convenience Extensions for Common Text Styles
 * Use these for consistent styling throughout the app
 */

// Centered text styles
val Typography.body1Centered: TextStyle
    get() = body1.copy(textAlign = TextAlign.Center)

val Typography.body2Centered: TextStyle
    get() = body2.copy(textAlign = TextAlign.Center)

val Typography.h6Centered: TextStyle
    get() = h6.copy(textAlign = TextAlign.Center)

// Colored text styles for common use cases
fun Typography.body2Light(alpha: Float = 0.7f) = body2.copy(
    color = androidx.compose.ui.graphics.Color.White.copy(alpha = alpha)
)

fun Typography.captionSecondary() = caption.copy(
    color = androidx.compose.ui.graphics.Color(0xFF94A3B8)
)

/**
 * Quick Access to Inter Font
 * Use this when you need to explicitly set fontFamily in TextStyle.copy()
 */
val Inter = InterFontFamily
