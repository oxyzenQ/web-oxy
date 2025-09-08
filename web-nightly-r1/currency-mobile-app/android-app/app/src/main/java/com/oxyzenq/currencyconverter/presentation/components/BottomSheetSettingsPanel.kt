/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.components

import android.content.Context
import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.hilt.navigation.compose.hiltViewModel
import com.oxyzenq.kconvert.BuildConfig
import com.oxyzenq.kconvert.data.local.SettingsDataStore
import com.oxyzenq.kconvert.utils.StorageUtils
import com.oxyzenq.kconvert.presentation.viewmodel.SecurityViewModel
import com.oxyzenq.kconvert.presentation.util.setImmersiveMode
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.ColumnScope

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
    hapticsEnabled: Boolean = true,
    onToggleHaptics: (Boolean) -> Unit = {},
    securityViewModel: SecurityViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    
    // Security state
    val securityState by securityViewModel.securityState.collectAsState()
    
    // Animation states
    val animatedOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 1f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
    )
    
    val backgroundBlur by animateFloatAsState(
        targetValue = if (isVisible) 6f else 0f,
        animationSpec = tween(180, easing = LinearOutSlowInEasing)
    )
    
    // Drag state for handle
    var dragOffset by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    
    // Panel height states
    var panelHeight by remember { mutableStateOf(0.5f) } // 0.5 = half screen, 1.0 = full screen
    // Smooth any changes to panelHeight (snapping/entrance) for nicer motion
    val animatedPanelHeight by animateFloatAsState(
        targetValue = panelHeight,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
    )
    val panelHeightForLayout = if (isDragging) panelHeight else animatedPanelHeight
    
    LaunchedEffect(isVisible) {
        if (isVisible) {
            // Always start at half when opened
            panelHeight = 0.5f
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
                            if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDismiss()
                        }
                    }
            )
            
            // Bottom Sheet Panel
            // Compute slide distance equal to the current panel height fraction of the screen height
            val cfgForSlide = LocalConfiguration.current
            val slideDistancePx = with(density) { cfgForSlide.screenHeightDp.dp.toPx() * panelHeightForLayout }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(panelHeightForLayout)
                    .align(Alignment.BottomCenter)
                    .offset { IntOffset(0, (animatedOffset * slideDistancePx).roundToInt()) }
                    .graphicsLayer { alpha = animatedAlpha },
                backgroundColor = Color.Transparent,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                elevation = 16.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Background layer: always show gradient (prevents black flicker while dragging)
                    val bgModifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    Box(
                        modifier = bgModifier.background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1E3A8A), // blue-800
                                    Color(0xFF0B1020)  // deep black-blue
                                )
                            )
                        )
                    )
                    // Main content column (drag handle + list)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (panelHeight >= 0.95f) Modifier.statusBarsPadding() else Modifier)
                            .padding(top = if (panelHeight >= 0.95f) 20.dp else 0.dp)
                    ) {
                        // Drag Handle (visible at half and intermediate; hidden at full)
                        if (panelHeight < 0.95f) {
                            val configuration = LocalConfiguration.current
                            val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp)
                                    // Make the whole top zone easy to grab for dragging
                                    .heightIn(min = 56.dp)
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = { isDragging = true },
                                            onDragCancel = { isDragging = false },
                                            onDragEnd = {
                                                isDragging = false
                                                coroutineScope.launch {
                                                    val target = when {
                                                        panelHeight < 0.20f -> 0.0f // close
                                                        panelHeight < 0.80f -> 0.5f // half
                                                        else -> 1.0f // full
                                                    }
                                                    if (target == 0.0f) {
                                                        panelHeight = 0.0f
                                                        // give time for height tween before dismiss
                                                        kotlinx.coroutines.delay(180)
                                                        onDismiss()
                                                    } else {
                                                        panelHeight = target
                                                    }
                                                }
                                            }
                                        ) { _, dragAmount ->
                                            val delta = dragAmount.y / screenHeightPx
                                            var newHeight = panelHeight - delta
                                            // Immediate snap-to-full threshold when dragging upward past 0.92
                                            if (newHeight >= 0.92f) newHeight = 1.0f
                                            panelHeight = newHeight.coerceIn(0.0f, 1.0f)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                // Visible handle pill
                                Box(
                                    modifier = Modifier
                                        .width(64.dp)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF4B5563))
                                )
                            }
                        }

                        // Fixed centered title (does not scroll)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val cfg = LocalConfiguration.current
                            val titleSp = when {
                                cfg.screenWidthDp < 360 -> 32.sp  // was 48
                                cfg.screenWidthDp < 412 -> 37.sp  // was 56
                                else -> 40.sp                      // was 60
                            }
                            Text(
                                text = "Settings",
                                style = MaterialTheme.typography.h5.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = titleSp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }

                        // Panel Content (scrollable)
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                        // Extra breathing room below fixed title
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                        
                        // Security Check Section
                        item {
                            SecurityCheckSection(
                                securityState = securityState,
                                securityViewModel = securityViewModel,
                                onRunManualCheck = {
                                    if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    securityViewModel.performSecurityCheck(context)
                                }
                            )
                        }
                        
                        // About Section
                        item {
                            AboutSection()
                        }
                        
                        // Maintenance Section
                        item {
                            MaintenanceSection(
                                onClearCache = {
                                    if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    // TODO implement clear cache
                                },
                                onResetSettings = {
                                    if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    // TODO implement reset settings
                                }
                            )
                        }
                        
                        // App Settings Section
                        item {
                            AppSettingsSection(
                                autoUpdateEnabledDefault = true,
                                darkModeEnabledDefault = true,
                                hapticsEnabledDefault = hapticsEnabled,
                                onToggleHaptics = { enabled -> onToggleHaptics(enabled) },
                                onAnyToggle = { if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
                            )
                        }
                        
                        // Footer spacing
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                        }
                    }
                    // Overlay action: only at full show ArrowDown to go back to half
                    if (panelHeight >= 0.95f) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .statusBarsPadding()
                                .padding(top = 12.dp, end = 12.dp)
                                .size(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.Black.copy(alpha = 0.20f))
                                .zIndex(2f),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = {
                                    // From full to half
                                    panelHeight = 0.5f
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Back to Half",
                                    tint = Color.White
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
private fun SecurityCheckSection(
    securityState: com.oxyzenq.kconvert.presentation.viewmodel.SecurityState,
    securityViewModel: SecurityViewModel,
    onRunManualCheck: () -> Unit
) {
    SettingsCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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

        Row(verticalAlignment = Alignment.CenterVertically) {
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

        if (!securityState.isLoading && securityState.threatDetails.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            val env = if (securityState.isPassed) "Secure device detected" else "Security threats detected"
            Text(
                text = "Environment: $env",
                style = MaterialTheme.typography.caption.copy(
                    color = Color(0xFF9CA3AF)
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        val context = LocalContext.current
        Button(
            onClick = {
                // Notify start of manual security check
                android.widget.Toast.makeText(context, "Starting security check...", android.widget.Toast.LENGTH_SHORT).show()
                securityViewModel.performSecurityCheck(context)
                onRunManualCheck()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF3B82F6)),
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

        // Toast on completion based on result
        var prevLoading by remember { mutableStateOf(false) }
        LaunchedEffect(securityState.isLoading) {
            if (prevLoading && !securityState.isLoading) {
                val msg = if (securityState.isPassed) "Security check passed" else "Security check failed"
                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
            }
            prevLoading = securityState.isLoading
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Security Log Actions (Copy/Save) shown after check completes
        if (!securityState.isLoading) Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Copy Log Button
            Button(
                onClick = {
                    val log = securityViewModel.getSecurityLog()
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Security Log", log)
                    clipboard.setPrimaryClip(clip)
                    android.widget.Toast.makeText(context, "Log security has copy to your clipboard", android.widget.Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = when {
                        securityState.isPassed -> Color(0xFF10B981) // Green for passed
                        securityState.threatDetails.isNotEmpty() -> Color(0xFFEF4444) // Red for failed
                        else -> Color(0xFFF59E0B) // Yellow for unknown/warning
                    }
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Log",
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Copy Log",
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
            
            // Save Log Button
            Button(
                onClick = {
                    val log = securityViewModel.getSecurityLog()
                    val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                        .format(java.util.Date())
                    val filename = "Kconvert-$timestamp.log"
                    
                    try {
                        val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                        val file = java.io.File(downloadsDir, filename)
                        file.writeText(log)
                        android.widget.Toast.makeText(context, "Log saved as $filename in Downloads", android.widget.Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        // Fallback: copy to clipboard
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Security Log", log)
                        clipboard.setPrimaryClip(clip)
                        android.widget.Toast.makeText(context, "Log security has copy to your clipboard", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = when {
                        securityState.isPassed -> Color(0xFF10B981) // Green for passed
                        securityState.threatDetails.isNotEmpty() -> Color(0xFFEF4444) // Red for failed
                        else -> Color(0xFFF59E0B) // Yellow for unknown/warning
                    }
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Save Log",
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Save Log",
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun AboutSection() {
    SettingsCard(modifier = Modifier.fillMaxWidth()) {
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
                    text = " Oxyzenq 2025",
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

@Composable
private fun MaintenanceSection(
    onClearCache: () -> Unit,
    onResetSettings: () -> Unit
) {
    SettingsCard(modifier = Modifier.fillMaxWidth()) {
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

        }
    }
}

@Composable
private fun AppSettingsSection(
    autoUpdateEnabledDefault: Boolean,
    darkModeEnabledDefault: Boolean,
    hapticsEnabledDefault: Boolean,
    onToggleHaptics: (Boolean) -> Unit,
    onAnyToggle: () -> Unit
) {
    val context = LocalContext.current
    val settingsStore = remember { SettingsDataStore(context) }
    // Observe persisted values; fall back to defaults on first launch
    val persistedAutoUpdate by settingsStore.autoUpdateFlow.collectAsState(initial = autoUpdateEnabledDefault)
    val persistedHaptics by settingsStore.hapticsFlow.collectAsState(initial = hapticsEnabledDefault)

    var autoUpdateEnabled by remember(persistedAutoUpdate) { mutableStateOf(persistedAutoUpdate) }
    var hapticsEnabled by remember(persistedHaptics) { mutableStateOf(persistedHaptics) }
    // Observe persisted value (default true)
    val persistedFullScreen by settingsStore.fullScreenFlow.collectAsState(initial = true)
    var fullScreenEnabled by remember(persistedFullScreen) { mutableStateOf(persistedFullScreen) }
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    onAnyToggle()
                }
            )

            SettingToggleRow(
                title = "Haptic Feedback",
                isEnabled = hapticsEnabled,
                onToggle = { enabled ->
                    hapticsEnabled = enabled
                    // persist and notify VM/UI
                    scope.launch { settingsStore.setHaptics(enabled) }
                    onToggleHaptics(enabled)
                    onAnyToggle()
                }
            )

            SettingToggleRow(
                title = "Auto-update on launch",
                isEnabled = autoUpdateEnabled,
                onToggle = { enabled ->
                    autoUpdateEnabled = enabled
                    // persist immediately
                    scope.launch { settingsStore.setAutoUpdate(enabled) }
                    onAnyToggle()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            // Cache size indicator (real-time on launch)
            var cacheInfoText by remember { mutableStateOf("") }
            var isScanningCache by remember { mutableStateOf(false) }
            val cacheContext = LocalContext.current

            // One-time scan on composition
            LaunchedEffect(Unit) {
                isScanningCache = true
                android.widget.Toast.makeText(cacheContext, "scanning the cache apps", android.widget.Toast.LENGTH_SHORT).show()
                val size = StorageUtils.getCacheSize(cacheContext)
                val mb = size / 1048576.0
                cacheInfoText = if (size > 0L) String.format("%.1f MB for cache", mb) else ""
                isScanningCache = false
                android.widget.Toast.makeText(cacheContext, "scanning cache apps done", android.widget.Toast.LENGTH_SHORT).show()
            }

            InfoRow("Storage used", cacheInfoText)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Clear Cache Button (moved below storage indicator)
            val context = LocalContext.current
            var showClearCacheDialog by remember { mutableStateOf(false) }
            
            Button(
                onClick = {
                    val currentCacheSize = StorageUtils.getCacheSize(context)
                    if (currentCacheSize > 0) {
                        showClearCacheDialog = true
                    } else {
                        // Show "cache has been clearing" message
                        android.widget.Toast.makeText(context, "Cache has been clearing", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF374151),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Clear Cache",
                    style = MaterialTheme.typography.button.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            
            // Clear Cache Confirmation Dialog
            if (showClearCacheDialog) {
                AlertDialog(
                    onDismissRequest = { showClearCacheDialog = false },
                    title = {
                        Text(
                            text = "Clear Cache",
                            style = MaterialTheme.typography.h6.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    },
                    text = {
                        Text(
                            text = "Remove the cache also remove the data currency on offline storage, continue?",
                            style = MaterialTheme.typography.body2.copy(
                                color = Color(0xFFE5E7EB)
                            )
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    val success = StorageUtils.clearCache(context)
                                    if (success) {
                                        android.widget.Toast.makeText(context, "Cache cleared successfully", android.widget.Toast.LENGTH_SHORT).show()
                                        // Refresh cache indicator after clearing
                                        val size = StorageUtils.getCacheSize(context)
                                        val mb = size / 1048576.0
                                        cacheInfoText = if (size > 0L) String.format("%.1f MB for cache", mb) else ""
                                    } else {
                                        android.widget.Toast.makeText(context, "Failed to clear cache", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    showClearCacheDialog = false
                                }
                            }
                        ) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showClearCacheDialog = false }
                        ) {
                            Text("Cancel", color = Color(0xFF9CA3AF))
                        }
                    },
                    backgroundColor = Color(0xFF1F2937),
                    contentColor = Color.White
                )
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
