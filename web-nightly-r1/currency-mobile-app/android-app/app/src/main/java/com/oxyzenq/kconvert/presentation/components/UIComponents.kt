/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oxyzenq.kconvert.R

/**
 * Feature highlight component for the header card
 */
@Composable
fun FeatureHighlight(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF93C5FD).copy(alpha = 0.3f),
                            Color(0xFFC4B5FD).copy(alpha = 0.2f)
                        )
                    )
                )
                .border(
                    1.dp,
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF93C5FD).copy(alpha = 0.5f),
                            Color(0xFFC4B5FD).copy(alpha = 0.5f)
                        )
                    ),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color(0xFF93C5FD),
                modifier = Modifier.size(20.dp)
            )
        }
        val textBrush = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF93C5FD).copy(alpha = 0.9f),
                Color(0xFFC4B5FD).copy(alpha = 0.9f)
            )
        )
        Text(
            text = buildAnnotatedString {
                pushStyle(SpanStyle(brush = textBrush, fontWeight = FontWeight.Medium))
                append(text)
                pop()
            },
            style = MaterialTheme.typography.caption.copy(
                fontSize = 11.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Kconvert logo painter helper: use kconvert_logo_orig when available, otherwise fallback
 * to the previous kconvert_logo_new resource.
 */
@Composable
fun KconvertLogoImage(
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    // Load the official orig logo (old assets removed)
    val painter = painterResource(id = R.drawable.kconvert_logo_orig)
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

/**
 * Elegant gradient info card (unified style)
 */
@Composable
fun ElegantInfoCard(
    title: String,
    modifier: Modifier = Modifier,
    titleIcon: (@Composable (() -> Unit))? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val inter = FontFamily(Font(R.font.inter))
    val cardBg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0B1530).copy(alpha = 0.9f),
            Color(0xFF0F1F3F).copy(alpha = 0.95f)
        )
    )
    val titleBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF93C5FD), // blue-300
            Color(0xFFC4B5FD)  // violet-300
        )
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.Transparent)
    ) {
        // Outer glow
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.35f))
                .blur(40.dp)
        )
        // Main card
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg)
                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            // Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (titleIcon != null) {
                    titleIcon()
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = buildAnnotatedString {
                        pushStyle(SpanStyle(brush = titleBrush, fontWeight = FontWeight.SemiBold))
                        append(title)
                        pop()
                    },
                    style = MaterialTheme.typography.h6.copy(fontFamily = inter),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

/**
 * Glassmorphism container component
 */
@Composable
fun GlassmorphismContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = 0.12f,
        animationSpec = tween(durationMillis = 800, easing = EaseInOutCubic),
        label = "glassmorphism_alpha"
    )
    
    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = animatedAlpha),
                        Color.White.copy(alpha = animatedAlpha * 0.6f)
                    )
                ),
                RoundedCornerShape(18.dp)
            )
    ) {
        content()
    }
}
