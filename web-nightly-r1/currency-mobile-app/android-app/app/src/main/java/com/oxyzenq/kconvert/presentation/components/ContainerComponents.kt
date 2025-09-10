/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.oxyzenq.kconvert.presentation.theme.KconvertIcons
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oxyzenq.kconvert.BuildConfig
import com.oxyzenq.kconvert.R
import com.oxyzenq.kconvert.presentation.theme.body2Centered

/**
 * Container 0: Header with Circle Logo and Currency Converter Branding
 */
@Composable
fun HeaderContainer() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // Semi-dark backdrop behind container 0 to match other containers
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Outer glow
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.35f))
                        .blur(40.dp)
                )
                // Main card with ElegantInfoCard background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0B1530).copy(alpha = 0.9f),
                                    Color(0xFF0F1F3F).copy(alpha = 0.95f)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.06f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(32.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Circular app icon with animated stroke
                        Box(
                            modifier = Modifier.size(93.dp), // 140/1.5 = 93.33
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedCircularStroke(
                                modifier = Modifier.size(93.dp) // 140/1.5 = 93.33
                            )
                            Box(
                                modifier = Modifier
                                    .size(80.dp) // 120/1.5 = 80
                                    .clip(CircleShape)
                                    .background(Color.Black)
                                    .padding(5.dp), // 8/1.5 = 5.33
                                contentAlignment = Alignment.Center
                            ) {
                                KconvertLogoImage(
                                    contentDescription = "Kconvert Logo",
                                    modifier = Modifier.size(69.dp) // 104/1.5 = 69.33
                                )
                            }
                        }

                        // Main title with gradient
                        val inter = FontFamily(Font(R.font.inter))
                        val titleBrush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF93C5FD), // blue-300
                                Color(0xFFC4B5FD)  // violet-300
                            )
                        )
                        Text(
                            text = buildAnnotatedString {
                                pushStyle(SpanStyle(brush = titleBrush, fontWeight = FontWeight.Bold))
                                append("Kconvert")
                                pop()
                            },
                            style = MaterialTheme.typography.h3.copy(
                                fontFamily = inter
                            ),
                            textAlign = TextAlign.Center
                        )

                        // Subtitle description with gradient
                        val subtitleBrush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF93C5FD).copy(alpha = 0.8f), // blue-300
                                Color(0xFFC4B5FD).copy(alpha = 0.8f)  // violet-300
                            )
                        )
                        Text(
                            text = "Exchange rates across the globe — discover your financial balance under the stars.",
                            style = MaterialTheme.typography.body1.copy(
                                fontFamily = inter,
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        // Feature highlights row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            FeatureHighlight(
                                icon = KconvertIcons.TimerArrowUp,
                                text = "Real-time Rates"
                            )
                            FeatureHighlight(
                                icon = KconvertIcons.Cognition,
                                text = "Market Insights"
                            )
                            FeatureHighlight(
                                icon = KconvertIcons.ShieldLock,
                                text = "Secure Exchange"
                            )
                        }

                        // Version text
                        Text(
                            text = "Stellar Edition v${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.caption.copy(
                                fontFamily = inter,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Container 4: What is currency converter?
 */
@Composable
fun WhatIsCurrencyConverterContainer() {
    ElegantInfoCard(
        title = "What is currency converter?",
        titleIcon = {
            Icon(imageVector = KconvertIcons.HelpOutline, contentDescription = null, tint = Color(0xFF93C5FD))
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Understanding Currency Conversion",
                style = MaterialTheme.typography.h6.copy(
                    color = Color(0xFF93C5FD),
                    fontWeight = FontWeight.SemiBold
                )
            )
            
            Text(
                text = "A currency converter is a sophisticated digital tool that calculates the equivalent value of one currency in terms of another currency. It leverages real-time or near real-time exchange rates from global financial markets to provide accurate conversions between different world currencies.",
                style = MaterialTheme.typography.body2.copy(
                    color = Color.White,
                    lineHeight = 20.sp
                )
            )
            
            Text(
                text = "Essential Features:",
                style = MaterialTheme.typography.subtitle2.copy(
                    color = Color(0xFF93C5FD),
                    fontWeight = FontWeight.Medium
                )
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "• Real-time exchange rates from global financial markets",
                    style = MaterialTheme.typography.body2.copy(
                        color = Color.White
                    )
                )
                Text(
                    text = "• Support for 150+ international currencies",
                    style = MaterialTheme.typography.body2.copy(
                        color = Color.White
                    )
                )
                Text(
                    text = "• Historical rate tracking and trend analysis",
                    style = MaterialTheme.typography.body2.copy(
                        color = Color.White
                    )
                )
                Text(
                    text = "• Offline functionality for remote locations",
                    style = MaterialTheme.typography.body2.copy(
                        color = Color.White
                    )
                )
                Text(
                    text = "• Intuitive and user-friendly interface design",
                    style = MaterialTheme.typography.body2.copy(
                        color = Color.White
                    )
                )
            }
            
            Text(
                text = "Professional Applications:",
                style = MaterialTheme.typography.subtitle2.copy(
                    color = Color(0xFF93C5FD),
                    fontWeight = FontWeight.Medium
                )
            )
            
            Text(
                text = "Currency converters are indispensable tools for international travelers planning budgets, global businesses managing cross-border transactions, forex traders analyzing market opportunities, e-commerce platforms handling international sales, and financial professionals requiring accurate exchange rate calculations for their daily operations.",
                style = MaterialTheme.typography.body2.copy(
                    color = Color.White,
                    lineHeight = 20.sp
                )
            )
        }
    }
}

/**
 * Container 5: About
 */
@Composable
fun AboutContainer() {
    ElegantInfoCard(
        title = "About",
        titleIcon = {
            Icon(imageVector = KconvertIcons.Info, contentDescription = null, tint = Color(0xFF93C5FD))
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Application Information",
                style = MaterialTheme.typography.h6.copy(
                    color = Color(0xFF93C5FD),
                    fontWeight = FontWeight.SemiBold
                )
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Product Name: Kconvert Currency Converter",
                    style = MaterialTheme.typography.body2.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                )
                
                Text(
                    text = "Package ID: com.oxyzenq.kconvert",
                    style = MaterialTheme.typography.body2.copy(
                        color = Color.White
                    )
                )
                
                Text(
                    text = "Version: ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                    style = MaterialTheme.typography.body2.copy(
                        color = Color.White
                    )
                )
                
                Text(
                    text = "License: MIT Open Source License",
                    style = MaterialTheme.typography.body2.copy(
                        color = Color(0xFF059669),
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            
            Text(
                text = "Developer Information",
                style = MaterialTheme.typography.h6.copy(
                    color = Color(0xFF93C5FD),
                    fontWeight = FontWeight.SemiBold
                )
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡",
                        style = MaterialTheme.typography.h6
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Developed by oxyzenq",
                        style = MaterialTheme.typography.body2.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
                
                Text(
                    text = "Copyright © 2025 oxyzenq. All rights reserved.",
                    style = MaterialTheme.typography.caption.copy(
                        color = Color.White
                    )
                )
                
                Text(
                    text = "Kconvert is a professional-grade currency converter designed for traders, travelers, and financial professionals. Our application combines cutting-edge Android technologies with intuitive design to deliver the most accurate and reliable currency conversion experience available on mobile platforms.",
                    style = MaterialTheme.typography.body2.copy(
                        color = Color.White,
                        lineHeight = 20.sp
                    )
                )
                
                Text(
                    text = "Core Features:",
                    style = MaterialTheme.typography.subtitle2.copy(
                        color = Color(0xFF93C5FD),
                        fontWeight = FontWeight.Medium
                    )
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "• Real-time exchange rates from multiple financial data sources",
                        style = MaterialTheme.typography.body2.copy(
                            color = Color.White
                        )
                    )
                    Text(
                        text = "• Support for 150+ international currencies and cryptocurrencies",
                        style = MaterialTheme.typography.body2.copy(
                            color = Color.White
                        )
                    )
                    Text(
                        text = "• Offline mode with intelligent caching system",
                        style = MaterialTheme.typography.body2.copy(
                            color = Color.White
                        )
                    )
                    Text(
                        text = "• Advanced security with 98% protection against threats",
                        style = MaterialTheme.typography.body2.copy(
                            color = Color.White
                        )
                    )
                    Text(
                        text = "• Beautiful glassmorphism UI with smooth animations",
                        style = MaterialTheme.typography.body2.copy(
                            color = Color.White
                        )
                    )
                    Text(
                        text = "• Historical rate tracking and trend analysis",
                        style = MaterialTheme.typography.body2.copy(
                            color = Color.White
                        )
                    )
                }
                
                Text(
                    text = "Professional Applications:",
                    style = MaterialTheme.typography.subtitle2.copy(
                        color = Color(0xFF93C5FD),
                        fontWeight = FontWeight.Medium
                    )
                )
                
                Text(
                    text = "Kconvert serves as an essential tool for international business operations, foreign exchange trading, travel planning, e-commerce transactions, and financial analysis. Whether you're a professional trader monitoring market fluctuations or a traveler planning your budget, Kconvert provides the accuracy and reliability you need.",
                    style = MaterialTheme.typography.body2.copy(
                        color = Color.White,
                        lineHeight = 20.sp
                    )
                )
            }
            
            Text(
                text = "Technical Specifications",
                style = MaterialTheme.typography.h6.copy(
                    color = Color(0xFF93C5FD),
                    fontWeight = FontWeight.SemiBold
                )
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "• Target SDK: Android 14 (API 34)",
                    style = MaterialTheme.typography.body2.copy(
                        color = Color.White
                    )
                )
                Text(
                    text = "• Minimum SDK: Android 7.0 (API 24)",
                    style = MaterialTheme.typography.body2.copy(
                        color = Color.White
                    )
                )
                Text(
                    text = "• Architecture: MVVM with Clean Architecture",
                    style = MaterialTheme.typography.body2.copy(
                        color = Color.White
                    )
                )
                Text(
                    text = "• UI Framework: Jetpack Compose with Material 3",
                    style = MaterialTheme.typography.body2.copy(
                        color = Color.White
                    )
                )
                Text(
                    text = "• Data Source: Real-time currency API integration",
                    style = MaterialTheme.typography.body2.copy(
                        color = Color.White
                    )
                )
            }
        }
    }
}
