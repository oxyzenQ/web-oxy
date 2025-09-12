/*
 * Creativity Authored by oxyzenq 2025
 * Fixed version with smooth scrolling and elastic drag behavior
 */

package com.oxyzenq.kconvert.presentation.components

import android.app.Activity
import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.oxyzenq.kconvert.AppVersion
import com.oxyzenq.kconvert.data.local.SettingsDataStore
import com.oxyzenq.kconvert.presentation.util.setImmersiveMode
import com.oxyzenq.kconvert.presentation.viewmodel.SecurityViewModel
import com.oxyzenq.kconvert.presentation.viewmodel.SettingsViewModel
import com.oxyzenq.kconvert.utils.StorageUtils
import com.oxyzenq.kconvert.data.repository.UpdateRepository
import com.oxyzenq.kconvert.data.repository.VersionComparison
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import javax.inject.Inject
import kotlin.math.roundToInt

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

/**
 * FIXED: Expandable Bottom Sheet Settings Panel for Kconvert
 * Improvements:
 * - Fixed overscroll lag/vibration with LocalOverscrollConfiguration
 * - Smooth elastic drag behavior (50% → 95% follows user, >95% snaps to full)
 * - Improved animation timing and easing
 * - Better drag threshold handling
 */
@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun BottomSheetSettingsPanel(
    onDismiss: () -> Unit,
    hapticsEnabled: Boolean = true,
    onToggleHaptics: (Boolean) -> Unit,
    isFullscreenMode: Boolean = true,
    onToggleFullscreen: (Boolean) -> Unit = {},
    darkLevel: Int = 0,
    onDarkLevelChange: (Int) -> Unit = {},
    navbarAutoHideEnabled: Boolean = true,
    onToggleNavbarAutoHide: (Boolean) -> Unit = {},
    autoRemindEnabled: Boolean = true,
    onToggleAutoRemind: (Boolean) -> Unit = {},
    isVisible: Boolean = false,
    securityViewModel: SecurityViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val updateRepository = settingsViewModel.updateRepository
    
    // Security state
    val securityState by securityViewModel.securityState.collectAsState()
    
    // FIXED: Enhanced drag state management - controlled by isVisible
    var panelHeight by remember { mutableStateOf(if (isVisible) 0.5f else 0.0f) }
    
    // Update panel height when visibility changes
    LaunchedEffect(isVisible) {
        panelHeight = if (isVisible) 0.5f else 0.0f
    }
    
    // FIXED: Improved animation timing for smoother transitions
    val animatedOffset by animateFloatAsState(
        targetValue = if (panelHeight > 0f) 0f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ), label = "sheet_offset"
    )
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (panelHeight > 0f) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ), label = "sheet_alpha"
    )
    
    val backgroundBlur by animateFloatAsState(
        targetValue = if (panelHeight > 0f) 6f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ), label = "background_blur"
    )
    var isDragging by remember { mutableStateOf(false) }
    var dragStartY by remember { mutableStateOf(0f) }
    var dragVelocity by remember { mutableStateOf(0f) }
    
    // FIXED: Smooth panel height animation with proper spring physics
    val animatedPanelHeight by animateFloatAsState(
        targetValue = panelHeight,
        animationSpec = if (isDragging) {
            // Immediate response during drag
            spring(
                dampingRatio = Spring.DampingRatioHighBouncy,
                stiffness = Spring.StiffnessHigh
            )
        } else {
            // Smooth settling after drag
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        }, label = "panel_height"
    )
    
    val panelHeightForLayout = if (isDragging) panelHeight else animatedPanelHeight
    
    LaunchedEffect(Unit) {
        // Only perform security check, don't auto-show panel
        securityViewModel.performSecurityCheck(context)
    }
    
    if (panelHeight > 0f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10f)
        ) {
            // Background blur overlay with proper touch interception
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f * (1f - animatedOffset)))
                    .blur(backgroundBlur.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        // Always consume clicks to prevent touch leaks
                        if (panelHeight < 0.7f) {
                            if (hapticsEnabled) hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDismiss()
                        }
                        // Consume click even if not dismissing to prevent background interaction
                    }
            )
            
            // FIXED: Bottom Sheet Panel with improved calculations
            val cfgForSlide = LocalConfiguration.current
            val slideDistancePx = with(LocalDensity.current) { cfgForSlide.screenHeightDp.dp.toPx() * panelHeightForLayout }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(panelHeightForLayout)
                    .align(Alignment.BottomCenter)
                    .offset { IntOffset(0, (animatedOffset * slideDistancePx.toFloat()).roundToInt()) }
                    .graphicsLayer { alpha = animatedAlpha },
                backgroundColor = Color.Transparent,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                elevation = 16.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Background layers remain the same
                    val bgModifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    
                    // Outer glow
                    Box(
                        modifier = bgModifier
                            .background(Color.Black.copy(alpha = 0.25f))
                            .blur(20.dp)
                    )
                    
                    // Main background
                    Box(
                        modifier = bgModifier.background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0B1530).copy(alpha = 0.8f),
                                    Color(0xFF0F1F3F).copy(alpha = 0.9f)
                                )
                            )
                        )
                    )
                    
                    // Main content column
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (panelHeight >= 0.95f) Modifier.statusBarsPadding() else Modifier)
                            .padding(top = if (panelHeight >= 0.95f) 20.dp else 0.dp)
                    ) {
                        // FIXED: Drag handle - only for header area, separate from content scroll
                        if (panelHeight < 0.95f) {
                            val configuration = LocalConfiguration.current
                            val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp)
                                    .heightIn(min = 56.dp)
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = { offset ->
                                                isDragging = true
                                                dragStartY = offset.y
                                                if (hapticsEnabled) {
                                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                }
                                            },
                                            onDragCancel = { 
                                                isDragging = false
                                                dragVelocity = 0f
                                            },
                                            onDragEnd = {
                                                isDragging = false
                                                scope.launch {
                                                    // Google Maps-style anchor-based snapping
                                                    val target = when {
                                                        panelHeight < 0.3f -> {
                                                            // Close threshold - below 30%
                                                            if (hapticsEnabled) {
                                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            }
                                                            0.0f
                                                        }
                                                        panelHeight > 0.85f -> {
                                                            // Full screen threshold - above 85%
                                                            if (hapticsEnabled) {
                                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            }
                                                            1.0f
                                                        }
                                                        else -> {
                                                            // Default anchor at 50%
                                                            if (hapticsEnabled) {
                                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            }
                                                            0.5f
                                                        }
                                                    }
                                                    
                                                    if (target == 0.0f) {
                                                        kotlinx.coroutines.delay(200)
                                                        onDismiss()
                                                    } else {
                                                        panelHeight = target
                                                    }
                                                    dragVelocity = 0f
                                                }
                                            }
                                        ) { _, dragAmount ->
                                            // Smooth drag calculation without auto-snapping
                                            val delta = dragAmount.y / screenHeightPx.toFloat()
                                            val previousHeight = panelHeight
                                            var newHeight = panelHeight - delta
                                            
                                            // Track velocity for snap decisions
                                            dragVelocity = (previousHeight - newHeight) * 1000f
                                            
                                            // Allow free movement between 0% and 100%
                                            panelHeight = newHeight.coerceIn(0.0f, 1.0f)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                // Enhanced handle with visual feedback
                                Box(
                                    modifier = Modifier
                                        .width(if (isDragging) 72.dp else 64.dp)
                                        .height(if (isDragging) 10.dp else 8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (isDragging) 
                                                Color(0xFF6B7280) 
                                            else 
                                                Color(0xFF4B5563)
                                        )
                                        .graphicsLayer {
                                            scaleX = if (isDragging) 1.05f else 1.0f
                                            scaleY = if (isDragging) 1.1f else 1.0f
                                        }
                                )
                            }
                        }

                        // Fixed centered title (same as before)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val cfg = LocalConfiguration.current
                            val titleSp = when {
                                cfg.screenWidthDp < 360 -> 32.sp
                                cfg.screenWidthDp < 412 -> 37.sp
                                else -> 40.sp
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

                        // FIXED: Panel Content with overscroll prevention
                        CompositionLocalProvider(
                            LocalOverscrollConfiguration provides null // This prevents the jumping/vibration
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp)
                                    .nestedScroll(rememberNestedScrollInteropConnection()),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                userScrollEnabled = true
                            ) {
                                // Extra breathing room below fixed title
                                item { Spacer(modifier = Modifier.height(8.dp)) }
                                
                                // Box 1 : App Settings Section
                                item {
                                    com.oxyzenq.kconvert.presentation.components.AppSettingsSection(
                                        darkModeEnabledDefault = false,
                                        hapticsEnabledDefault = hapticsEnabled,
                                        onToggleHaptics = { enabled -> onToggleHaptics(enabled) },
                                        autoRemindEnabled = autoRemindEnabled,
                                        onToggleAutoRemind = onToggleAutoRemind,
                                        isFullscreenMode = isFullscreenMode,
                                        onToggleFullscreen = onToggleFullscreen,
                                        darkLevel = darkLevel,
                                        onDarkLevelChange = onDarkLevelChange,
                                        navbarAutoHideEnabled = navbarAutoHideEnabled,
                                        onToggleNavbarAutoHide = onToggleNavbarAutoHide
                                    )
                                }
                                
                                // Box 2 : Security Check Section
                                item {
                                    SecurityCheckSection(
                                        securityState = securityState,
                                        securityViewModel = securityViewModel,
                                        onRunManualCheck = {
                                            if (hapticsEnabled) hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            securityViewModel.performSecurityCheck(context)
                                        }
                                    )
                                }
                                
                                // Box 3 : About Section
                                item {
                                    AboutSection()
                                }
                                
                                // Box 4 : Maintenance Section
                                item {
                                    MaintenanceSection(updateRepository)
                                }
                                
                                // Footer spacing
                                item {
                                    Spacer(modifier = Modifier.height(32.dp))
                                }
                            }
                        }
                    }
                    
                    // FIXED: Enhanced overlay action with smooth animation
                    if (panelHeight >= 0.95f) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .statusBarsPadding()
                                .padding(top = 12.dp, end = 12.dp)
                                .size(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.Black.copy(alpha = 0.20f))
                                .zIndex(2f)
                                .graphicsLayer {
                                    // Smooth fade in animation
                                    alpha = ((panelHeight - 0.90f) / 0.05f).coerceIn(0f, 1f)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = {
                                    if (hapticsEnabled) {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
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
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF3B82F6).copy(alpha = 0.8f),
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 2.dp
            ),
            enabled = !securityState.isLoading
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (securityState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (securityState.isLoading) "Checking..." else "Run Manual Check",
                    style = MaterialTheme.typography.button.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = Color.White
                )
            }
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
            // Copy Log Button with iOS style
            Button(
                onClick = {
                    val log = securityViewModel.getSecurityLog()
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Security Log", log)
                    clipboard.setPrimaryClip(clip)
                    android.widget.Toast.makeText(context, "Log security has copy to your clipboard", android.widget.Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = when {
                        securityState.isPassed -> Color(0xFF10B981).copy(alpha = 0.8f)
                        securityState.threatDetails.isNotEmpty() -> Color(0xFFEF4444).copy(alpha = 0.8f)
                        else -> Color(0xFFF59E0B).copy(alpha = 0.8f)
                    },
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Log",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Copy Log",
                        style = MaterialTheme.typography.button.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        color = Color.White
                    )
                }
            }
            
            // Save Log Button with iOS style
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
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = when {
                        securityState.isPassed -> Color(0xFF10B981).copy(alpha = 0.8f)
                        securityState.threatDetails.isNotEmpty() -> Color(0xFFEF4444).copy(alpha = 0.8f)
                        else -> Color(0xFFF59E0B).copy(alpha = 0.8f)
                    },
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Save Log",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Save Log",
                        style = MaterialTheme.typography.button.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun AboutSection() {
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
            InfoRow("App Version", AppVersion.VERSION_NAME)
            InfoRow("Build Type", if (com.oxyzenq.kconvert.BuildConfig.DEBUG) "Debug" else "Release")
            InfoRow("Target SDK", "35")

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
    updateRepository: UpdateRepository
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
            val updateStatus = "Ready"
            // Real app version installation/update date
            val context = LocalContext.current
            val settingsStore = remember { SettingsDataStore(context) }
            val updateCheckTime by settingsStore.lastUpdateCheckFlow.collectAsState(initial = "Never")
            InfoRow("Update Status", updateStatus)
            InfoRow("Last Update Check", updateCheckTime)
            val uriHandler = LocalUriHandler.current
            val scope = rememberCoroutineScope()
            
            var showUpdateDialog by remember { mutableStateOf(false) }
            var updateChecking by remember { mutableStateOf(false) }
            var isOutdated by remember { mutableStateOf(false) }
            var latestVersion by remember { mutableStateOf("") }
            var updateMessage by remember { mutableStateOf("") }
            var updateError by remember { mutableStateOf(false) }
            var updateTitle by remember { mutableStateOf("") }
            var isWarning by remember { mutableStateOf(false) }
            var showGitHubLink by remember { mutableStateOf(true) }
            var uiState by remember { mutableStateOf(com.oxyzenq.kconvert.data.repository.UpdateUIState.UP_TO_DATE) }
            val appLastUpdated = remember {
                try {
                    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                    val lastUpdateTime = packageInfo.lastUpdateTime
                    val formatter = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm:ss", java.util.Locale.getDefault())
                    formatter.format(java.util.Date(lastUpdateTime))
                } catch (e: Exception) {
                    "Unknown"
                }
            }
            
            InfoRow("Last updated", appLastUpdated)

            // Check for updates action
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (updateChecking) return@Button
                    updateChecking = true
                    scope.launch {
                        try {
                            val result = updateRepository.getLatestRelease()
                            result.fold(
                                onSuccess = { release: com.oxyzenq.kconvert.data.remote.GitHubRelease ->
                                    latestVersion = release.tag_name
                                    val current = AppVersion.VERSION_NAME
                                    
                                    // Update last check timestamp for manual check
                                    val formatter = java.text.SimpleDateFormat("HH:mm dd/MM/yy", java.util.Locale.getDefault())
                                    val timestamp = formatter.format(java.util.Date())
                                    scope.launch {
                                        settingsStore.setLastUpdateCheck(timestamp)
                                    }
                                    
                                    val comparison = updateRepository.compareVersions(latestVersion, current)
                                    val updateMsg = updateRepository.generateUpdateMessage(latestVersion, current, comparison)
                                    
                                    isOutdated = comparison == VersionComparison.NEWER_AVAILABLE
                                    updateError = false
                                    updateMessage = updateMsg.message
                                    updateTitle = updateMsg.title
                                    isWarning = updateMsg.isWarning
                                    showGitHubLink = updateMsg.showGitHubLink
                                    uiState = updateMsg.uiState
                                    showUpdateDialog = true
                                },
                                onFailure = { exception ->
                                    updateMessage = when {
                                        exception.message?.contains("timeout", ignoreCase = true) == true -> "Connection timeout. Please check your internet connection."
                                        exception.message?.contains("UnknownHost", ignoreCase = true) == true -> "Unable to reach GitHub. Please check your internet connection."
                                        exception.message?.contains("403", ignoreCase = true) == true -> "GitHub API rate limit exceeded. Please try again later."
                                        exception.message?.contains("404", ignoreCase = true) == true -> "No releases found in repository. Check https://github.com/oxyzenQ/web-oxy/releases"
                                        else -> "Unable to check for updates: ${exception.message}. Visit https://github.com/oxyzenQ/web-oxy/releases"
                                    }
                                    isOutdated = false
                                    latestVersion = ""
                                    updateError = true
                                    showUpdateDialog = true
                                }
                            )
                        } finally {
                            updateChecking = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF3B82F6).copy(alpha = 0.85f),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 2.dp
                ),
                enabled = !updateChecking
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (updateChecking) {
                        // Animated spinning refresh icon
                        val infiniteTransition = rememberInfiniteTransition(label = "spin")
                        val angle by infiniteTransition.animateFloat(
                            initialValue = 0F,
                            targetValue = 360F,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ), label = "spin_angle"
                        )
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer { rotationZ = angle },
                            tint = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (updateChecking) "Checking for updates..." else "Check for Updates",
                        style = MaterialTheme.typography.button.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        color = Color.White
                    )
                }
            }

            // Update result dialog using centralized UpdateResultWindow
            UpdateResultWindow(
                visible = showUpdateDialog && !updateChecking,
                updateError = updateError,
                isOutdated = isOutdated,
                latestVersion = latestVersion,
                updateMessage = updateMessage,
                currentVersion = AppVersion.VERSION_NAME,
                onDismiss = { showUpdateDialog = false },
                onOpenLatest = { uriHandler.openUri("https://github.com/oxyzenQ/web-oxy/releases/latest") },
                onOpenReleases = { uriHandler.openUri("https://github.com/oxyzenQ/web-oxy/releases") },
                isWarning = isWarning,
                showGitHubLink = showGitHubLink,
                updateTitle = updateTitle,
                uiState = uiState
            )

        }
    }
}

// Duplicate AppSettingsSection removed - using external file from AppSettingsSection.kt

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