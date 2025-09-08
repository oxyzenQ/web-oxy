/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.oxyzenq.kconvert.R

// Inter variable font family (offline)
private val InterFontFamily = FontFamily(
    // Variable font can cover all weights; map common weights to the same file
    Font(R.font.intervariable, weight = FontWeight.W400, style = FontStyle.Normal),
    Font(R.font.intervariable, weight = FontWeight.W500, style = FontStyle.Normal),
    Font(R.font.intervariable, weight = FontWeight.W600, style = FontStyle.Normal),
    Font(R.font.intervariable, weight = FontWeight.W700, style = FontStyle.Normal),
    Font(R.font.intervariable_italic, weight = FontWeight.W400, style = FontStyle.Italic),
    Font(R.font.intervariable_italic, weight = FontWeight.W600, style = FontStyle.Italic)
)

// Set Material typography to use Inter everywhere
val Typography = Typography(
    defaultFontFamily = InterFontFamily,
    h1 = TextStyle(
        fontWeight = FontWeight.W700,
        fontSize = 30.sp,
        lineHeight = 36.sp
    ),
    h2 = TextStyle(
        fontWeight = FontWeight.W700,
        fontSize = 26.sp,
        lineHeight = 32.sp
    ),
    h3 = TextStyle(
        fontWeight = FontWeight.W700,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    h4 = TextStyle(
        fontWeight = FontWeight.W700,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    h5 = TextStyle(
        fontWeight = FontWeight.W600,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    h6 = TextStyle(
        fontWeight = FontWeight.W600,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    subtitle1 = TextStyle(
        fontWeight = FontWeight.W600,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    subtitle2 = TextStyle(
        fontWeight = FontWeight.W500,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    body1 = TextStyle(
        fontWeight = FontWeight.W400,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    body2 = TextStyle(
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    button = TextStyle(
        fontWeight = FontWeight.W600,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    caption = TextStyle(
        fontWeight = FontWeight.W500,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    overline = TextStyle(
        fontWeight = FontWeight.W500,
        fontSize = 10.sp,
        lineHeight = 12.sp
    )
)
