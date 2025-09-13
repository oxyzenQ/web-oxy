/*
 * Creativity Authored by oxyzenq 2025
 * App Settings Section for BottomSheetSettingsPanel
 */

package com.oxyzenq.kconvert.presentation.components

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oxyzenq.kconvert.data.local.SettingsDataStore
import com.oxyzenq.kconvert.presentation.util.setImmersiveMode
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AppSettingsSection(
    darkModeEnabledDefault: Boolean,
    hapticsEnabledDefault: Boolean,
    onToggleHaptics: (Boolean) -> Unit,
    autoRemindEnabled: Boolean,
    onToggleAutoRemind: (Boolean) -> Unit,
    isFullscreenMode: Boolean,
    onToggleFullscreen: (Boolean) -> Unit,
    darkLevel: Int,
    onDarkLevelChange: (Int) -> Unit,
    navbarAutoHideEnabled: Boolean,
    onToggleNavbarAutoHide: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val settingsStore = remember { SettingsDataStore(context) }
    // Use the haptics state passed from ViewModel instead of local state
    val hapticsEnabled = hapticsEnabledDefault
    // Observe persisted value (default true)
    val persistedFullScreen by settingsStore.fullScreenFlow.collectAsState(initial = true)
    var fullScreenEnabled by remember(persistedFullScreen) { mutableStateOf(persistedFullScreen) }
    
    // Meteor animation setting
    val persistedMeteorAnimation by settingsStore.meteorAnimationFlow.collectAsState(initial = true)
    var meteorAnimationEnabled by remember(persistedMeteorAnimation) { mutableStateOf(persistedMeteorAnimation) }
    
    // Auto-remind setting
    val persistedAutoRemindEnabled by settingsStore.autoRemindEnabled.collectAsState(initial = true)
    var localAutoRemindEnabled by remember(persistedAutoRemindEnabled, autoRemindEnabled) { mutableStateOf(autoRemindEnabled) }
    
    
    val activity = (LocalContext.current as? Activity)
    val scope = rememberCoroutineScope()

    LaunchedEffect(persistedFullScreen) {
        activity?.let { setImmersiveMode(it, persistedFullScreen) }
    }

    SettingsCard(modifier = Modifier.fillMaxWidth()) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingToggleRow(
                title = "Full Screen Mode",
                isEnabled = fullScreenEnabled,
                onToggle = { enabled ->
                    fullScreenEnabled = enabled
                    activity?.let { setImmersiveMode(it, enabled) }
                    // persist setting
                    scope.launch {
                        settingsStore.setFullScreen(enabled)
                    }
                    onToggleFullscreen(enabled)
                }
            )
            
            SettingToggleRow(
                title = "Auto-Hide Bottom Navbar",
                isEnabled = navbarAutoHideEnabled,
                onToggle = { enabled ->
                    // Persist to DataStore
                    scope.launch {
                        settingsStore.setNavbarAutoHide(enabled)
                    }
                    onToggleNavbarAutoHide(enabled)
                }
            )
            
            SettingToggleRow(
                title = "Meteor Animation",
                isEnabled = meteorAnimationEnabled,
                onToggle = { enabled ->
                    meteorAnimationEnabled = enabled
                    // Persist to DataStore
                    scope.launch {
                        settingsStore.setMeteorAnimation(enabled)
                    }
                }
            )
            
            SettingToggleRow(
                title = "Automatic check update when on main screen",
                isEnabled = localAutoRemindEnabled,
                onToggle = { enabled ->
                    localAutoRemindEnabled = enabled
                    // Persist to DataStore
                    scope.launch {
                        settingsStore.setAutoRemindEnabled(enabled)
                    }
                    // Also notify parent component
                    onToggleAutoRemind(enabled)
                }
            )

            // Background dark level slider (0..100)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Adjust background darkness",
                style = MaterialTheme.typography.subtitle2.copy(color = Color(0xFFCBD5E1))
            )
            val levelState = remember(darkLevel) { mutableStateOf(darkLevel) }
            Slider(
                value = levelState.value.toFloat(),
                onValueChange = { newValue ->
                    val intValue = newValue.roundToInt()
                    levelState.value = intValue
                    onDarkLevelChange(intValue)
                },
                valueRange = 0f..100f,
                steps = 10,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF059669),
                    activeTrackColor = Color(0xFF059669).copy(alpha = 0.7f),
                    inactiveTrackColor = Color(0xFF374151)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF059669))
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Brightness6,
                    contentDescription = "Brightness Level: ${levelState.value}",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Low", style = MaterialTheme.typography.caption, color = Color(0xFF94A3B8))
                Text("Medium", style = MaterialTheme.typography.caption, color = Color(0xFF94A3B8))
                Text("Max", style = MaterialTheme.typography.caption, color = Color(0xFF94A3B8))
            }

            SettingToggleRow(
                title = "Haptic Feedback",
                isEnabled = hapticsEnabled,
                onToggle = { enabled ->
                    // Only use the ViewModel's method to ensure single source of truth
                    onToggleHaptics(enabled)
                }
            )


            Spacer(modifier = Modifier.height(8.dp))
            // Cache size indicator with proper state management
            val cacheContext = LocalContext.current
            val cacheSettingsStore = remember { SettingsDataStore(cacheContext) }
            var cacheSize by remember { mutableStateOf(0L) }
            var lastScanTime by remember { mutableStateOf("") }
            
            // Initial cache scan on first load
            LaunchedEffect(Unit) {
                try {
                    cacheSize = com.oxyzenq.kconvert.utils.StorageUtils.getCacheSize(cacheContext)
                    val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                    lastScanTime = timestamp
                    
                    // Save to persistent storage
                    cacheSettingsStore.setCacheSize(cacheSize)
                    cacheSettingsStore.setCacheLastScan(timestamp)
                } catch (e: Exception) {
                    cacheSize = 0L
                }
            }
            
            val cacheInfoText = remember(cacheSize) {
                formatFileSize(cacheSize)
            }
            
            val lastScanText = remember(lastScanTime) {
                if (lastScanTime.isNotEmpty()) {
                    "Last scan: $lastScanTime"
                } else {
                    "Not scanned yet"
                }
            }

            InfoRow("Storage Cache used", cacheInfoText)
            InfoRow("Cache status", lastScanText)
            
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Cache management button
            val cacheManagementContext = LocalContext.current
            var showCacheActionDialog by remember { mutableStateOf(false) }
            var showDeleteConfirmDialog by remember { mutableStateOf(false) }
            var isProcessingCache by remember { mutableStateOf(false) }
            var isScanningCache by remember { mutableStateOf(false) }
            var isClearingCache by remember { mutableStateOf(false) }
            
            // Single unified cache management button with iOS style
            Button(
                onClick = {
                    showCacheActionDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF059669).copy(alpha = 0.8f),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 2.dp
                ),
                enabled = !isProcessingCache
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isProcessingCache) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isProcessingCache) "Processing..." else "Manage Cache Storage",
                        style = MaterialTheme.typography.button.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        color = Color.White
                    )
                }
            }
            
            // Compact Cache Action Dialog using centralized CacheManagementWindow
            CacheManagementWindow(
                visible = showCacheActionDialog,
                cacheSizeText = formatFileSize(cacheSize),
                isScanning = isScanningCache,
                isClearing = isClearingCache,
                onScan = {
                    scope.launch {
                        isScanningCache = true
                        isProcessingCache = true
                        try {
                            val newCacheSize = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { 
                                kotlinx.coroutines.withTimeout(30000) { 
                                    com.oxyzenq.kconvert.utils.StorageUtils.getCacheSize(cacheManagementContext) 
                                } 
                            }
                            if (newCacheSize > 5L * 1024 * 1024 * 1024) {
                                android.widget.Toast.makeText(
                                    cacheManagementContext,
                                    "Warning: Cache size is very large (${formatFileSize(newCacheSize)}). Consider clearing it.",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                            val timestamp = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                            cacheSize = newCacheSize
                            lastScanTime = timestamp
                            cacheSettingsStore.setCacheSize(newCacheSize)
                            cacheSettingsStore.setCacheLastScan(timestamp)
                            android.widget.Toast.makeText(cacheManagementContext, "Cache scanned: ${formatFileSize(newCacheSize)}", android.widget.Toast.LENGTH_SHORT).show()
                        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                            android.widget.Toast.makeText(cacheManagementContext, "Scan timeout: Cache too large to scan quickly", android.widget.Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(cacheManagementContext, "Scan failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                        } finally {
                            isScanningCache = false
                            isProcessingCache = false
                        }
                    }
                },
                onRequestClear = {
                    if (cacheSize > 1L * 1024 * 1024 * 1024) {
                        android.widget.Toast.makeText(
                            cacheManagementContext,
                            "Large cache detected (${formatFileSize(cacheSize)}). This may take a while.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                    showCacheActionDialog = false
                    showDeleteConfirmDialog = true
                },
                onDismiss = { showCacheActionDialog = false }
            )
            
            // Beautiful floating confirmation dialog for cache clearing
            ClearCacheConfirmationDialog(
                isVisible = showDeleteConfirmDialog,
                cacheSize = formatFileSize(cacheSize),
                onConfirm = {
                    scope.launch {
                        isClearingCache = true
                        isProcessingCache = true
                        showDeleteConfirmDialog = false
                        
                        try {
                            // Add timeout for large cache clearing (60 seconds max)
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                kotlinx.coroutines.withTimeout(60000) {
                                    com.oxyzenq.kconvert.utils.StorageUtils.clearCache(cacheManagementContext)
                                }
                            }
                            
                            cacheSize = 0L
                            val timestamp = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                            lastScanTime = timestamp
                            
                            cacheSettingsStore.setCacheSize(0L)
                            cacheSettingsStore.setCacheLastScan(timestamp)
                            
                            android.widget.Toast.makeText(cacheManagementContext, "Cache cleared successfully", android.widget.Toast.LENGTH_SHORT).show()
                        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                            android.widget.Toast.makeText(cacheManagementContext, "Clear timeout: Cache too large to clear quickly", android.widget.Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(cacheManagementContext, "Clear failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                        } finally {
                            isClearingCache = false
                            isProcessingCache = false
                        }
                    }
                },
                onDismiss = { showDeleteConfirmDialog = false },
                hapticsEnabled = hapticsEnabled
            )
        }
    }
}

/**
 * Settings card background to match main screen ElegantInfoCard style
 */
@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardBg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0B1530),
            Color(0xFF0F1F3F)
        )
    )
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        content()
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

/**
 * Format file size dynamically based on size (B/KB/MB/GB)
 */
private fun formatFileSize(bytes: Long): String {
    return when {
        bytes == 0L -> "0 B"
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}
