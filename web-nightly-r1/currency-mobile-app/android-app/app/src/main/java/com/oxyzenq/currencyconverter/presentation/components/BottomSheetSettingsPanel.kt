/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.components

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.oxyzenq.kconvert.BuildConfig
import com.oxyzenq.kconvert.presentation.viewmodel.SecurityViewModel
import kotlinx.coroutines.launch

/**
 * Expandable Bottom Sheet Settings Panel for Kconvert
 * Features:
 * - Drag handle for smooth expansion/collapse
 * - Background blur effect
 * - Security status display
 * - App settings and maintenance options
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheetSettingsPanel(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    securityViewModel: SecurityViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    
    // Security state
    val securityState by securityViewModel.securityState.collectAsState()
    
    // Animation states
    val animatedOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    val backgroundBlur by animateFloatAsState(
        targetValue = if (isVisible) 8f else 0f,
        animationSpec = tween(300)
    )
    
    // Drag state for handle
    var dragOffset by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    
    // Panel height states
    var panelHeight by remember { mutableStateOf(0.5f) } // 0.5 = half screen, 1.0 = full screen
    
    LaunchedEffect(isVisible) {
        if (isVisible) {
            securityViewModel.performSecurityCheck(context)
        }
    }
    
    if (isVisible) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .zIndex(10f)
        ) {
            // Background blur overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f * (1f - animatedOffset)))
                    .blur(backgroundBlur.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (panelHeight < 0.7f) {
                            onDismiss()
                        }
                    }
            )
            
            // Bottom Sheet Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(panelHeight)
                    .align(Alignment.BottomCenter)
                    .offset(y = (animatedOffset * 100).dp),
                backgroundColor = Color(0xFF121212),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                elevation = 16.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Drag Handle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFF4B5563))
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { isDragging = true },
                                        onDragEnd = {
                                            isDragging = false
                                            // Snap to positions
                                            coroutineScope.launch {
                                                when {
                                                    panelHeight < 0.3f -> onDismiss()
                                                    panelHeight < 0.7f -> panelHeight = 0.5f
                                                    else -> panelHeight = 1.0f
                                                }
                                            }
                                        }
                                    ) { _, dragAmount ->
                                        val newHeight = panelHeight - (dragAmount.y / size.height)
                                        panelHeight = newHeight.coerceIn(0.2f, 1.0f)
                                    }
                                }
                        )
                    }
                    
                    // Panel Content
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header
                        item {
                            Text(
                                text = "Settings & Info",
                                style = MaterialTheme.typography.h5.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        // Security Check Section
                        item {
                            SecurityCheckSection(
                                securityState = securityState,
                                onRunManualCheck = { securityViewModel.performSecurityCheck(context) }
                            )
                        }
                        
                        // About Section
                        item {
                            AboutSection()
                        }
                        
                        // Maintenance Section
                        item {
                            MaintenanceSection()
                        }
                        
                        // App Settings Section
                        item {
                            AppSettingsSection()
                        }
                        
                        // Footer spacing
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityCheckSection(
    securityState: com.oxyzenq.kconvert.presentation.viewmodel.SecurityState,
    onRunManualCheck: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF1F2937),
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Security",
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Security Check",
                    style = MaterialTheme.typography.h6.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Security Status
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (statusText, statusColor, statusIcon) = when {
                    securityState.isLoading -> Triple("Checking...", Color(0xFFF59E0B), Icons.Default.Refresh)
                    securityState.isPassed -> Triple("Security Check: Passed", Color(0xFF10B981), Icons.Default.CheckCircle)
                    else -> Triple("Security Check: Failed", Color(0xFFEF4444), Icons.Default.Warning)
                }
                
                Icon(
                    imageVector = statusIcon,
                    contentDescription = statusText,
                    tint = statusColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.body2.copy(
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            
            // Security details
            if (!securityState.isLoading && securityState.threatDetails.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Environment: ${if (securityState.isPassed) "Secure device detected" else "Security threats detected"}",
                    style = MaterialTheme.typography.caption.copy(
                        color = Color(0xFF9CA3AF)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Manual check button
            Button(
                onClick = onRunManualCheck,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF3B82F6)
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = !securityState.isLoading
            ) {
                if (securityState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (securityState.isLoading) "Checking..." else "Run Manual Check",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun AboutSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF1F2937),
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "About",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "About",
                    style = MaterialTheme.typography.h6.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoRow("App name", "Kconvert")
                InfoRow("Version", BuildConfig.VERSION_NAME)
                InfoRow("License", "MIT")
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "© Oxyzenq 2025",
                        style = MaterialTheme.typography.caption.copy(
                            color = Color(0xFF9CA3AF)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "GitHub",
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MaintenanceSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF1F2937),
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Maintenance",
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Maintenance",
                    style = MaterialTheme.typography.h6.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoRow("Status", "All systems operational")
                InfoRow("Last updated", "7 Sep 2025")
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { /* Clear cache */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF6B7280)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Clear Cache",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                    
                    Button(
                        onClick = { /* Reset settings */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Reset Settings",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppSettingsSection() {
    var autoUpdateEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(true) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF1F2937),
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "App Settings",
                    style = MaterialTheme.typography.h6.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Auto-update toggle
                SettingToggleRow(
                    title = "Auto-update on launch",
                    isEnabled = autoUpdateEnabled,
                    onToggle = { autoUpdateEnabled = it }
                )
                
                // Dark mode toggle
                SettingToggleRow(
                    title = "Dark Mode",
                    isEnabled = darkModeEnabled,
                    onToggle = { darkModeEnabled = it }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                InfoRow("Storage used", "2.4 MB for cache")
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.body2.copy(
                color = Color(0xFF9CA3AF)
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.body2.copy(
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.body2.copy(
                color = Color.White
            )
        )
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF10B981),
                checkedTrackColor = Color(0xFF10B981).copy(alpha = 0.5f),
                uncheckedThumbColor = Color(0xFF6B7280),
                uncheckedTrackColor = Color(0xFF374151)
            )
        )
    }
}
