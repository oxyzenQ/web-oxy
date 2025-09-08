/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 * Custom gauge chart to show currency strength comparison
 */
@Composable
fun CurrencyStrengthGauge(
    fromCurrency: String?,
    toCurrency: String?,
    exchangeRate: Double?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        
        if (fromCurrency != null && toCurrency != null && exchangeRate != null) {
            // Calculate gauge position based on exchange rate
            val gaugeValue = calculateGaugeValue(exchangeRate)
            
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    drawGauge(gaugeValue)
                }
                
                // Center text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "1 $fromCurrency",
                        style = MaterialTheme.typography.caption.copy(
                            color = Color(0xFF94A3B8)
                        )
                    )
                    Text(
                        text = "= ${String.format("%.4f", exchangeRate)} $toCurrency",
                        style = MaterialTheme.typography.body2.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp)) // Increased from 16dp to 32dp (2rem)
            
            // Currency labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$fromCurrency ${if (exchangeRate > 1) "Weaker" else "Stronger"}",
                    style = MaterialTheme.typography.caption.copy(
                        color = if (exchangeRate > 1) Color(0xFFDC2626) else Color(0xFF059669),
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = "$toCurrency ${if (exchangeRate > 1) "Stronger" else "Weaker"}",
                    style = MaterialTheme.typography.caption.copy(
                        color = if (exchangeRate > 1) Color(0xFF059669) else Color(0xFFDC2626),
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        } else {
            // Placeholder when no data
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        drawGauge(0.5f) // Neutral position for placeholder
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Select currencies to compare",
                    style = MaterialTheme.typography.body2.copy(
                        color = Color(0xFF94A3B8)
                    )
                )
            }
        }
    }
}

/**
 * Calculate gauge value (0.0 to 1.0) based on exchange rate
 */
private fun calculateGaugeValue(exchangeRate: Double): Float {
    // Logarithmic scale for better visualization
    val logRate = ln(exchangeRate)
    val normalizedValue = (logRate + 5) / 10 // Normalize to 0-1 range
    return normalizedValue.coerceIn(0.0, 1.0).toFloat()
}

/**
 * Draw the gauge on canvas
 */
private fun DrawScope.drawGauge(value: Float) {
    val center = Offset(size.width / 2, size.height / 2)
    val radius = size.minDimension / 2 - 20.dp.toPx()
    val strokeWidth = 12.dp.toPx()
    
    // Background arc
    drawArc(
        color = Color(0xFF374151),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
    
    // Progress arc
    val sweepAngle = 180f * value
    drawArc(
        color = when {
            value < 0.3f -> Color(0xFF059669) // Green for stronger left currency
            value > 0.7f -> Color(0xFFDC2626) // Red for weaker left currency
            else -> Color(0xFFF59E0B) // Yellow for balanced
        },
        startAngle = 180f,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
    
    // Needle
    val needleAngle = 180f + sweepAngle
    val needleLength = radius - 10.dp.toPx()
    val needleEnd = Offset(
        center.x + needleLength * cos(Math.toRadians(needleAngle.toDouble())).toFloat(),
        center.y + needleLength * sin(Math.toRadians(needleAngle.toDouble())).toFloat()
    )
    
    drawLine(
        color = Color.White,
        start = center,
        end = needleEnd,
        strokeWidth = 4.dp.toPx(),
        cap = StrokeCap.Round
    )
    
    // Center circle
    drawCircle(
        color = Color.White,
        radius = 8.dp.toPx(),
        center = center
    )
}
