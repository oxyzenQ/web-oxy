/*
 * Creativity Authored by oxyzenq 2025
 * 
 * Global Spring Animation Integration Guide
 * How to integrate the spring animation system into your Kconvert app
 * Step-by-step implementation for premium bouncy interactions
 */

package com.oxyzenq.currencyconverter.presentation.animation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * STEP 1: Wrap your entire app with SpringAnimationTheme
 * Add this to your MainActivity or main composable
 */
@Composable
fun KconvertAppWithSpringAnimations(
    content: @Composable () -> Unit
) {
    // Configure global spring animation theme
    val springTheme = SpringAnimationTheme(
        intensity = SpringIntensity.MEDIUM,  // Balanced bounce
        performanceMode = PerformanceMode.HIGH_PERFORMANCE,
        enableReducedMotion = false
    )
    
    SpringAnimationTheme(theme = springTheme) {
        content()
    }
}

/**
 * STEP 2: Replace standard Material 3 components with Spring versions
 * Example: Currency Converter Main Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpringCurrencyConverterScreen() {
    var fromCurrency by remember { mutableStateOf("USD") }
    var toCurrency by remember { mutableStateOf("EUR") }
    var amount by remember { mutableStateOf("100") }
    var result by remember { mutableStateOf("85.50") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Spring-enhanced header
        SpringCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1976D2).copy(alpha = 0.1f)
            ),
            pressScale = 0.98f
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💱 Currency Converter",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Premium spring animations",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
        
        // Amount input with spring focus animation
        SpringTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
            focusScale = 1.02f,
            leadingIcon = {
                Icon(Icons.Default.AttachMoney, contentDescription = null)
            }
        )
        
        // Currency selection buttons with spring press
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SpringButton(
                onClick = { /* Show currency picker */ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                pressScale = 0.95f
            ) {
                Text("From: $fromCurrency")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            
            // Swap button with extreme bounce
            SpringIconButton(
                onClick = { 
                    val temp = fromCurrency
                    fromCurrency = toCurrency
                    toCurrency = temp
                },
                pressScale = 0.8f
            ) {
                Icon(
                    Icons.Default.SwapHoriz,
                    contentDescription = "Swap currencies",
                    tint = Color(0xFF2196F3)
                )
            }
            
            SpringButton(
                onClick = { /* Show currency picker */ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                ),
                pressScale = 0.95f
            ) {
                Text("To: $toCurrency")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        }
        
        // Convert button with premium bounce
        SpringButton(
            onClick = { /* Perform conversion */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF5722)
            ),
            pressScale = 0.92f
        ) {
            Icon(Icons.Default.Calculate, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Convert Currency",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Result card with hover effect
        SpringCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
            ),
            pressScale = 0.99f,
            hoverScale = 1.01f
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Converted Amount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$result $toCurrency",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }
        }
        
        // Recent conversions with animated list
        SpringAnimatedRecentConversions()
    }
}

/**
 * STEP 3: Animated Lists with Staggered Entrance
 */
@Composable
fun SpringAnimatedRecentConversions() {
    val recentConversions = remember {
        listOf(
            "100 USD → 85.50 EUR",
            "50 GBP → 62.30 USD", 
            "1000 JPY → 7.45 EUR",
            "200 CAD → 148.20 USD"
        )
    }
    
    SpringCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📈 Recent Conversions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recentConversions) { conversion ->
                    SpringCard(
                        onClick = { /* Select this conversion */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .globalSpringEntrance(
                                visible = true,
                                enterScale = 0.95f
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.08f)
                        ),
                        pressScale = 0.97f
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = Color(0xFF00BCD4),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = conversion,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * STEP 4: Settings Screen with Spring Controls
 */
@Composable
fun SpringSettingsScreen() {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }
    var animationIntensity by remember { mutableStateOf(SpringIntensity.MEDIUM) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Settings header
        SpringCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF9C27B0).copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    tint = Color(0xFF9C27B0),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "⚙️ Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Customize your experience",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        // Settings options with spring animations
        SpringCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.05f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Notifications toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFFFF9800)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Push Notifications",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    SpringSwitch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                }
                
                Divider(color = Color.White.copy(alpha = 0.1f))
                
                // Dark mode toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = Color(0xFF2196F3)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Dark Mode",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    SpringCheckbox(
                        checked = darkModeEnabled,
                        onCheckedChange = { darkModeEnabled = it }
                    )
                }
                
                Divider(color = Color.White.copy(alpha = 0.1f))
                
                // Animation intensity selector
                Column {
                    Text(
                        text = "Animation Intensity",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SpringIntensity.values().forEach { intensity ->
                            SpringButton(
                                onClick = { animationIntensity = intensity },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (animationIntensity == intensity)
                                        Color(0xFF4CAF50) else Color.White.copy(alpha = 0.1f)
                                ),
                                pressScale = 0.9f
                            ) {
                                Text(
                                    text = intensity.name,
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * STEP 5: Navigation with Spring Transitions
 * Add this to your navigation setup
 */
@Composable
fun SpringNavigationExample() {
    var currentScreen by remember { mutableStateOf("converter") }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Content with spring entrance animation
        Box(
            modifier = Modifier
                .weight(1f)
                .globalSpringEntrance(
                    visible = true,
                    enterScale = 0.95f
                )
        ) {
            when (currentScreen) {
                "converter" -> SpringCurrencyConverterScreen()
                "settings" -> SpringSettingsScreen()
                else -> SpringCurrencyConverterScreen()
            }
        }
        
        // Bottom navigation with spring buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SpringButton(
                onClick = { currentScreen = "converter" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentScreen == "converter") 
                        Color(0xFF2196F3) else Color.White.copy(alpha = 0.1f)
                ),
                pressScale = 0.9f
            ) {
                Icon(Icons.Default.CurrencyExchange, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Convert")
            }
            
            SpringButton(
                onClick = { currentScreen = "settings" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentScreen == "settings") 
                        Color(0xFF2196F3) else Color.White.copy(alpha = 0.1f)
                ),
                pressScale = 0.9f
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Settings")
            }
        }
    }
}
