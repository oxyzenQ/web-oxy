/*
 * Creativity Authored by oxyzenq 2025
 * 
 * Global Spring Animation System Demo
 * Comprehensive showcase of all bouncy animation features
 * Interactive playground for testing and customization
 */

package com.oxyzenq.kconvert.presentation.animation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Main Spring Animation Demo Screen
 * Interactive showcase of all animation features
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpringAnimationDemo(
    modifier: Modifier = Modifier
) {
    var currentIntensity by remember { mutableStateOf(SpringIntensity.MEDIUM) }
    var showPerformanceMetrics by remember { mutableStateOf(false) }
    var performanceMetrics by remember { mutableStateOf(AnimationPerformanceMetrics(0, 0, 0L)) }
    
    val currentTheme = SpringAnimationTheme(
        intensity = currentIntensity,
        performanceMode = PerformanceMode.HIGH_PERFORMANCE
    )
    
    SpringAnimationTheme(theme = currentTheme) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E),
                            Color(0xFF0F3460)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Header with performance metrics
            DemoHeader(
                currentIntensity = currentIntensity,
                onIntensityChange = { currentIntensity = it },
                showMetrics = showPerformanceMetrics,
                onToggleMetrics = { showPerformanceMetrics = !showPerformanceMetrics },
                metrics = performanceMetrics
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Demo sections
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ButtonDemoSection()
                }
                
                item {
                    CardDemoSection()
                }
                
                item {
                    InputDemoSection()
                }
                
                item {
                    InteractiveDemoSection()
                }
                
                item {
                    ListAnimationDemo()
                }
            }
        }
        
        // Performance monitoring
        if (showPerformanceMetrics) {
            SpringAnimationPerformanceMonitor { metrics ->
                performanceMetrics = metrics
            }
        }
    }
}

/**
 * Demo Header with Controls
 */
@Composable
private fun DemoHeader(
    currentIntensity: SpringIntensity,
    onIntensityChange: (SpringIntensity) -> Unit,
    showMetrics: Boolean,
    onToggleMetrics: () -> Unit,
    metrics: AnimationPerformanceMetrics
) {
    SpringCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🌟 Global Spring Animation System",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Premium bouncy interactions • 60 FPS • Performance optimized",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Intensity controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Animation Intensity:",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SpringIntensity.values().forEach { intensity ->
                        SpringButton(
                            onClick = { onIntensityChange(intensity) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentIntensity == intensity) 
                                    Color(0xFF00BCD4) else Color.White.copy(alpha = 0.2f)
                            ),
                            pressScale = 0.9f
                        ) {
                            Text(
                                text = intensity.name,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            
            // Performance metrics
            if (showMetrics) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "FPS: ${metrics.fps}",
                        color = if (metrics.fps >= 55) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Active Animations: ${metrics.activeAnimations}",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            SpringButton(
                onClick = onToggleMetrics,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.2f)
                ),
                pressScale = 0.95f
            ) {
                Icon(
                    imageVector = if (showMetrics) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (showMetrics) "Hide Metrics" else "Show Metrics",
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Button Demo Section
 */
@Composable
private fun ButtonDemoSection() {
    DemoSection(
        title = "🎯 Bouncy Buttons",
        description = "Every button press feels premium and responsive"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SpringButton(
                onClick = {},
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                pressScale = 0.9f
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Success")
            }
            
            SpringButton(
                onClick = {},
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF5722)
                ),
                pressScale = 0.95f
            ) {
                Icon(Icons.Default.Warning, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Alert")
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SpringFloatingActionButton(
                onClick = {},
                containerColor = Color(0xFF2196F3),
                pressScale = 0.85f,
                hoverScale = 1.15f
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
            
            SpringIconButton(
                onClick = {},
                pressScale = 0.8f
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFE91E63)
                )
            }
            
            SpringIconButton(
                onClick = {},
                pressScale = 0.85f
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = null,
                    tint = Color(0xFF00BCD4)
                )
            }
        }
    }
}

/**
 * Card Demo Section
 */
@Composable
private fun CardDemoSection() {
    DemoSection(
        title = "📱 Interactive Cards",
        description = "Cards with hover and press spring effects"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SpringCard(
                onClick = {},
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF9C27B0).copy(alpha = 0.2f)
                ),
                pressScale = 0.96f,
                hoverScale = 1.03f
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFF9C27B0),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Premium",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            SpringCard(
                onClick = {},
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFF9800).copy(alpha = 0.2f)
                ),
                pressScale = 0.97f,
                hoverScale = 1.02f
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Speed,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Fast",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Input Demo Section
 */
@Composable
private fun InputDemoSection() {
    var textValue by remember { mutableStateOf("") }
    var sliderValue by remember { mutableStateOf(0.5f) }
    var switchValue by remember { mutableStateOf(false) }
    var checkboxValue by remember { mutableStateOf(false) }
    
    DemoSection(
        title = "⌨️ Animated Inputs",
        description = "Form controls with spring focus and state animations"
    ) {
        SpringTextField(
            value = textValue,
            onValueChange = { textValue = it },
            label = { Text("Spring TextField") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.1f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
            ),
            focusScale = 1.02f
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Spring Slider: ${(sliderValue * 100).toInt()}%",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
        
        SpringSlider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF00BCD4),
                activeTrackColor = Color(0xFF00BCD4)
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                SpringSwitch(
                    checked = switchValue,
                    onCheckedChange = { switchValue = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF4CAF50),
                        checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Spring Switch",
                    color = Color.White
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                SpringCheckbox(
                    checked = checkboxValue,
                    onCheckedChange = { checkboxValue = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF2196F3)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Spring Checkbox",
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Interactive Demo Section
 */
@Composable
private fun InteractiveDemoSection() {
    var rotationAngle by remember { mutableStateOf(0f) }
    var scaleValue by remember { mutableStateOf(1f) }
    var alphaValue by remember { mutableStateOf(1f) }
    
    DemoSection(
        title = "🎮 Interactive Animations",
        description = "Direct control over spring animation parameters"
    ) {
        // Animated demo box
        Box(
            modifier = Modifier
                .size(100.dp)
                .globalSpringRotation(rotationAngle)
                .globalSpringScale(scaleValue)
                .globalSpringAlpha(alphaValue)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00BCD4),
                            Color(0xFF2196F3)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Animation,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpringButton(
                onClick = { rotationAngle += 90f },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.2f)
                ),
                pressScale = 0.9f
            ) {
                Text("Rotate", fontSize = 12.sp)
            }
            
            SpringButton(
                onClick = { scaleValue = if (scaleValue == 1f) 1.5f else 1f },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.2f)
                ),
                pressScale = 0.9f
            ) {
                Text("Scale", fontSize = 12.sp)
            }
            
            SpringButton(
                onClick = { alphaValue = if (alphaValue == 1f) 0.3f else 1f },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.2f)
                ),
                pressScale = 0.9f
            ) {
                Text("Fade", fontSize = 12.sp)
            }
        }
    }
}

/**
 * List Animation Demo
 */
@Composable
private fun ListAnimationDemo() {
    val demoItems = remember {
        listOf(
            DemoItem("Currency Exchange", Icons.Default.CurrencyExchange, Color(0xFF4CAF50)),
            DemoItem("Analytics", Icons.Default.Analytics, Color(0xFF2196F3)),
            DemoItem("Settings", Icons.Default.Settings, Color(0xFFFF9800)),
            DemoItem("Security", Icons.Default.Security, Color(0xFFE91E63)),
            DemoItem("Notifications", Icons.Default.Notifications, Color(0xFF9C27B0))
        )
    }
    
    DemoSection(
        title = "📋 Animated List Items",
        description = "List items with staggered entrance animations"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            demoItems.forEachIndexed { index, item ->
                SpringCard(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .globalSpringEntrance(
                            visible = true,
                            enterScale = 0.9f
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = item.color.copy(alpha = 0.2f)
                    ),
                    pressScale = 0.98f
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = null,
                            tint = item.color,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = item.title,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Demo Section Container
 */
@Composable
private fun DemoSection(
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit
) {
    SpringCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            content()
        }
    }
}

/**
 * Demo Item Data Class
 */
private data class DemoItem(
    val title: String,
    val icon: ImageVector,
    val color: Color
)
