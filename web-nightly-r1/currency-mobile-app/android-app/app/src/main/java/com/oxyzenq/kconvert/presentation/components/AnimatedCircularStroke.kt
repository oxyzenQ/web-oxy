/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.oxyzenq.kconvert.R
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated circular stroke component with gradient and glow effect
 */
@Composable
fun AnimatedCircularStroke(
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 4.dp,
    sweepAngle: Float = 60f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "circular_stroke")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "stroke_rotation"
    )

    Box(modifier = modifier) {
        var boxSize by remember { mutableStateOf(Size.Zero) }
        
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .onSizeChanged { size ->
                    boxSize = Size(size.width.toFloat(), size.height.toFloat())
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension / 2f) - strokeWidth.toPx() / 2f
            val strokeWidthPx = strokeWidth.toPx()

            // Main blue stroke with solid color (no gradient)
            val mainBrush = SolidColor(Color(0xFF42A5F5)) // Solid blue
            
            // Second snake with solid grey color
            val secondBrush = SolidColor(Color(0xFF9E9E9E)) // Solid grey

            // Draw first snake with solid blue color
            rotate(degrees = rotation) {
                // Main stroke
                drawArc(
                    brush = mainBrush,
                    startAngle = 0f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt)
                )
            }

            // Second grey snake with phase offset
            val offsetRotation = (rotation + 160f) % 360f
            rotate(degrees = offsetRotation) {
                drawArc(
                    brush = secondBrush,
                    startAngle = 0f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt)
                )
            }
        }

    }
}
