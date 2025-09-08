/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.screen

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.Canvas
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.unit.Dp
import com.oxyzenq.kconvert.ui.theme.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.oxyzenq.kconvert.R
import com.oxyzenq.kconvert.BuildConfig
import com.oxyzenq.kconvert.data.model.Currency
import com.oxyzenq.kconvert.presentation.components.ConfirmationDialog
import com.oxyzenq.kconvert.presentation.components.CurrencyStrengthGauge
import com.oxyzenq.kconvert.presentation.components.FloatingNotification
import com.oxyzenq.kconvert.presentation.components.BottomSheetSettingsPanel
import com.oxyzenq.kconvert.presentation.viewmodel.ConfirmationType
import com.oxyzenq.kconvert.presentation.viewmodel.KconvertViewModel
import kotlinx.coroutines.launch

/**
 * Animated background component with smooth transitions and particle effects
 */
@Composable
fun AnimatedBackground(isScrolling: Boolean) {
    var screenWidth by remember { mutableStateOf(0f) }
    var screenHeight by remember { mutableStateOf(0f) }

    // Particle lists
    val shootingStars = remember { mutableStateListOf<ShootingStar>() }
    val bubbles = remember { mutableStateListOf<Bubble>() }

    // Pause scaling while the list is actively scrolling to avoid jank
    val targetScale = if (isScrolling) 1.0f else 1.05f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "background_scale"
    )

    // Animation trigger for particles
    val infiniteTransition = rememberInfiniteTransition(label = "particle_animation")
    val animationTrigger by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "animation_trigger"
    )

    // Update particles
    LaunchedEffect(animationTrigger) {
        if (screenWidth > 0 && screenHeight > 0) {
            // Update shooting stars
            shootingStars.removeAll { star ->
                star.x += star.speed * 2f
                star.y += star.speed * 1.5f
                star.alpha -= 0.02f
                star.x > screenWidth || star.y > screenHeight || star.alpha <= 0f
            }

            // Update bubbles
            bubbles.removeAll { bubble ->
                bubble.y -= bubble.speed
                bubble.alpha -= 0.008f
                bubble.y < -bubble.size || bubble.alpha <= 0f
            }

            // Add new shooting star (1-2 every few seconds)
            if (kotlin.random.Random.nextFloat() < 0.008f && shootingStars.size < 3) {
                shootingStars.add(
                    ShootingStar(
                        x = kotlin.random.Random.nextFloat() * screenWidth * 0.3f - 100f,
                        y = kotlin.random.Random.nextFloat() * screenHeight * 0.3f - 50f,
                        speed = kotlin.random.Random.nextFloat() * 8f + 12f,
                        alpha = 1f,
                        size = kotlin.random.Random.nextFloat() * 3f + 2f
                    )
                )
            }

            // Add new bubble (constant with random delay)
            if (kotlin.random.Random.nextFloat() < 0.15f && bubbles.size < 8) {
                bubbles.add(
                    Bubble(
                        x = kotlin.random.Random.nextFloat() * screenWidth,
                        y = screenHeight + 50f,
                        speed = kotlin.random.Random.nextFloat() * 2f + 1f,
                        alpha = kotlin.random.Random.nextFloat() * 0.6f + 0.4f,
                        size = kotlin.random.Random.nextFloat() * 8f + 4f
                    )
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background wallpaper with scaling
        Image(
            painter = painterResource(id = R.drawable.hdr_stellar_edition_v2),
            contentDescription = "HDR Stellar Background",
            modifier = Modifier
                .fillMaxSize()
                .scale(scale),
            contentScale = ContentScale.Crop
        )

        // Particle overlay
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    screenWidth = size.width.toFloat()
                    screenHeight = size.height.toFloat()
                }
        ) {
            // Draw shooting stars
            shootingStars.forEach { star ->
                // Star trail effect
                for (i in 0..3) {
                    val trailAlpha = star.alpha * (1f - i * 0.25f)
                    val trailX = star.x - i * star.speed * 0.5f
                    val trailY = star.y - i * star.speed * 0.375f

                    if (trailAlpha > 0f) {
                        drawCircle(
                            color = Color.White.copy(alpha = trailAlpha),
                            radius = star.size * (1f - i * 0.2f),
                            center = Offset(trailX, trailY)
                        )
                    }
                }
            }

            // Draw bubbles
            bubbles.forEach { bubble ->
                drawCircle(
                    color = Color.White.copy(alpha = bubble.alpha * 0.3f),
                    radius = bubble.size,
                    center = Offset(bubble.x, bubble.y),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Inner glow
                drawCircle(
                    color = Color.White.copy(alpha = bubble.alpha * 0.1f),
                    radius = bubble.size * 0.7f,
                    center = Offset(bubble.x, bubble.y)
                )
            }
        }

        // Subtle overlay gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f)
                        )
                    )
                )
        )
    }
}

/**
 * Elegant gradient info card (unified style)
 */
@Composable
private fun ElegantInfoCard(
    title: String,
    modifier: Modifier = Modifier,
    titleIcon: (@Composable (() -> Unit))? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val inter = FontFamily(Font(R.font.inter))
    val cardBg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0B1530),
            Color(0xFF0F1F3F)
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
                .background(Color.Black.copy(alpha = 0.18f))
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

/**
 * Main screen for Kconvert app with all 5 containers
 */
@Composable
fun KconvertMainScreen(
    viewModel: KconvertViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencies by viewModel.currencies.collectAsState()
    val autoUpdateEnabled by viewModel.autoUpdateEnabled.collectAsState()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    
    // Settings panel state
    var showSettingsPanel by remember { mutableStateOf(false) }
    // hapticsEnabled persisted in DataStore via ViewModel; no local state needed
    
    // Initialize app on first composition
    LaunchedEffect(Unit) {
        viewModel.initializeApp(context)
    }
    
    // Handle scroll to top
    LaunchedEffect(uiState.shouldScrollToTop) {
        if (uiState.shouldScrollToTop) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
            viewModel.onScrollToTopHandled()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Animated Background
        val isScrolling by remember { derivedStateOf { listState.isScrollInProgress } }
        AnimatedBackground(isScrolling = isScrolling)
        
        // Removed duplicate backdrop overlay to reduce overdraw (AnimatedBackground already overlays a gradient)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(top = 20.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Container 0 : Header with Circle Logo and Settings Title
            item(key = "header", contentType = "header") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    // Semi-dark backdrop panel behind header to dim wallpaper
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.28f))
                            .padding(vertical = 16.dp, horizontal = 12.dp)
                    ) {
                        // Center content
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                        // Animated Circle Logo
                        AnimatedLogo(
                            painter = painterResource(id = R.drawable.kconvert_logo_new),
                            size = 84.dp,
                            contentDescription = "Kconvert Logo"
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Gradient title using Inter font
                        val inter = FontFamily(Font(R.font.inter))
                        val titleGradient = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF60A5FA), // blue-400
                                Color(0xFFA78BFA), // violet-400
                                Color(0xFFF472B6)  // pink-400
                            )
                        )
                        Text(
                            text = buildAnnotatedString {
                                pushStyle(SpanStyle(brush = titleGradient, fontWeight = FontWeight.ExtraBold))
                                append("Kconvert")
                                pop()
                            },
                            style = MaterialTheme.typography.h4.copy(
                                fontFamily = inter
                            ),
                            textAlign = TextAlign.Center
                        )

                        // Subtext with subtle gradient and Inter
                        val subtitleGradient = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF93C5FD), // blue-300
                                Color(0xFFC4B5FD)  // violet-300
                            )
                        )
                        Text(
                            text = buildAnnotatedString {
                                pushStyle(SpanStyle(brush = subtitleGradient, fontWeight = FontWeight.Medium))
                                append("Currency Converter")
                                pop()
                            },
                            style = MaterialTheme.typography.subtitle1.copy(
                                fontFamily = inter
                            ),
                            textAlign = TextAlign.Center
                        )
                        }
                    }
                }
            }
            
            // Container 1: Calculator Converter (Elegant card)
            item(key = "calc_container", contentType = "calc") {
                // Semi-dark backdrop behind container 1 to dim wallpaper
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.22f))
                        .padding(8.dp)
                ) {
                    ElegantInfoCard(
                        title = "Calculator Converter",
                        titleIcon = {
                            Icon(imageVector = Icons.Default.Calculate, contentDescription = null, tint = Color(0xFF93C5FD))
                        }
                    ) {
                        CalculatorConverterContent(
                            uiState = uiState,
                            currencies = currencies,
                            onAmountChange = viewModel::updateAmount,
                            onSourceCurrencySelect = viewModel::selectSourceCurrency,
                            onTargetCurrencySelect = viewModel::selectTargetCurrency,
                            onConvert = {
                                if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.convertCurrency()
                            }
                        )
                    }
                }
            }
            
            // Container 2: Full Control Panel (Elegant card)
            item(key = "control_panel", contentType = "panel") {
                // Semi-dark backdrop behind container 2 to dim wallpaper
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.22f))
                        .padding(8.dp)
                ) {
                    ElegantInfoCard(
                        title = "Full Control Panel",
                        titleIcon = {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = Color(0xFF93C5FD))
                        }
                    ) {
                        FullControlPanelContent(
                            dataIndicator = uiState.dataIndicator,
                            autoUpdateEnabled = autoUpdateEnabled,
                            isRefreshing = uiState.isRefreshing,
                            onRefreshData = {
                                if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.showConfirmationDialog(ConfirmationType.REFRESH_DATA)
                            },
                            onDeleteData = {
                                if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.showConfirmationDialog(ConfirmationType.DELETE_DATA)
                            },
                            onToggleAutoUpdate = {
                                if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.toggleAutoUpdate()
                            },
                            onRefreshApp = {
                                if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.refreshApp()
                            }
                        )
                    }
                }
            }
            
            // Container 3: Currency Strength Comparison (Elegant card)
            item(key = "gauge", contentType = "gauge") {
                ElegantInfoCard(
                    title = "Currency Strength Comparison",
                    titleIcon = {
                        Icon(imageVector = Icons.Default.ShowChart, contentDescription = null, tint = Color(0xFF93C5FD))
                    }
                ) {
                    ChartGaugeContent(
                        sourceCurrency = uiState.sourceCurrency?.code,
                        targetCurrency = uiState.targetCurrency?.code,
                        exchangeRate = uiState.conversionResult?.exchangeRate
                    )
                }
            }

            // Container 4: What is currency converter?
            item(key = "what_is_currency", contentType = "info") {
                val inter = FontFamily(Font(R.font.inter))
                ElegantInfoCard(
                    title = "What is currency converter?",
                    titleIcon = { Icon(imageVector = Icons.Outlined.HelpOutline, contentDescription = null, tint = Color(0xFF93C5FD)) }
                ) {
                    Text(
                        text = "A currency converter is a digital tool that calculates the equivalent value of one currency in terms of another currency. It uses real-time or near real-time exchange rates to provide accurate conversions between world currencies.",
                        style = MaterialTheme.typography.body2.copy(color = Color(0xFFE5E7EB), fontFamily = inter)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Key features:",
                        style = MaterialTheme.typography.subtitle2.copy(color = Color(0xFFBFDBFE), fontFamily = inter, fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("• Real-time exchange rates", style = MaterialTheme.typography.body2.copy(color = Color(0xFFE5E7EB), fontFamily = inter))
                        Text("• Support for multiple currencies", style = MaterialTheme.typography.body2.copy(color = Color(0xFFE5E7EB), fontFamily = inter))
                        Text("• Historical rate tracking", style = MaterialTheme.typography.body2.copy(color = Color(0xFFE5E7EB), fontFamily = inter))
                        Text("• Offline functionality", style = MaterialTheme.typography.body2.copy(color = Color(0xFFE5E7EB), fontFamily = inter))
                        Text("• Easy-to-use interface", style = MaterialTheme.typography.body2.copy(color = Color(0xFFE5E7EB), fontFamily = inter))
                    }
                }
            }

            // Container 5: About the app (bundled details)
            item(key = "about_app", contentType = "about") {
                val inter = FontFamily(Font(R.font.inter))
                ElegantInfoCard(
                    title = "About the app",
                    titleIcon = { Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = Color(0xFF93C5FD)) }
                ) {
                    Text(
                        text = "Kconvert is a modern, elegant currency converter designed for speed, clarity, and security.",
                        style = MaterialTheme.typography.body2.copy(color = Color(0xFFE5E7EB), fontFamily = inter)
                    )
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = Color(0xFF93C5FD))
                            Spacer(Modifier.width(8.dp))
                            Text("Version: ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.body2.copy(color = Color(0xFFE5E7EB), fontFamily = inter))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = Color(0xFF93C5FD))
                            Spacer(Modifier.width(8.dp))
                            Text("Type: Open Source Project", style = MaterialTheme.typography.body2.copy(color = Color(0xFFE5E7EB), fontFamily = inter))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = Color(0xFF93C5FD))
                            Spacer(Modifier.width(8.dp))
                            Text("Status: Maintenance", style = MaterialTheme.typography.body2.copy(color = Color(0xFFE5E7EB), fontFamily = inter))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = Color(0xFF93C5FD))
                            Spacer(Modifier.width(8.dp))
                            Text("License: MIT", style = MaterialTheme.typography.body2.copy(color = Color(0xFFE5E7EB), fontFamily = inter))
                        }
                    }
                }
            }
            
            // Container 6: Exit Button
            item(key = "exit_btn", contentType = "footer") {
                ElegantInfoCard(
                    title = "Exit App",
                    titleIcon = {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, tint = Color(0xFFDC2626))
                    },
                    modifier = Modifier.clickable {
                        if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.showConfirmationDialog(ConfirmationType.EXIT_APP)
                    }
                ) {
                    Text(
                        text = "Tap to safely exit the application",
                        style = MaterialTheme.typography.body2.copy(color = Color(0xFFE5E7EB)),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        // Global Top-Right Settings button (overlay, notch-safe) with haptic + circular bg
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(12.dp)
                .size(44.dp)
                .background(Color.Black.copy(alpha = 0.24f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = {
                    if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showSettingsPanel = true
                },
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Floating notifications and dialogs
        Column(
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            FloatingNotification(
                isVisible = uiState.notification.isVisible,
                message = uiState.notification.message,
                type = uiState.notification.type,
                onDismiss = viewModel::hideNotification
            )
        }
        
        ConfirmationDialog(
            isVisible = uiState.confirmationDialog.isVisible,
            title = uiState.confirmationDialog.title,
            type = uiState.confirmationDialog.type,
            onConfirm = { viewModel.onConfirmationResult(true, context) },
            onDismiss = { viewModel.onConfirmationResult(false, context) }
        )
        
        // Error handling
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                // Auto clear error after showing
                kotlinx.coroutines.delay(5000)
                viewModel.clearError()
            }
        }
        
        // Settings Panel
        BottomSheetSettingsPanel(
            isVisible = showSettingsPanel,
            onDismiss = { showSettingsPanel = false },
            hapticsEnabled = hapticsEnabled,
            onToggleHaptics = { enabled -> viewModel.setHapticsEnabled(enabled) }
        )
    }
}

/**
 * Particle data classes for animation
 */
data class ShootingStar(
    var x: Float,
    var y: Float,
    var speed: Float,
    var alpha: Float,
    var size: Float
)

data class Bubble(
    var x: Float,
    var y: Float,
    var speed: Float,
    var alpha: Float,
    var size: Float
)


/**
 * Animated Logo Component with pulse, shimmer, and sway effects
 */
@Composable
fun AnimatedLogo(
    painter: Painter,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    contentDescription: String? = null
) {
    // Infinite transition for looped pulse only (throbbing)
    val infiniteTransition = rememberInfiniteTransition(label = "logo_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(pulseScale),
        contentAlignment = Alignment.Center
    ) {
        // Base logo with solid black circular background
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color.Black)
                .padding(8.dp),
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * Elegant Button Component matching the sleek design
 */
@Composable
private fun ElegantButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    backgroundColor: Color = Color(0xFF1E293B),
    contentColor: Color = Color.White,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = backgroundColor.copy(alpha = if (enabled) 0.9f else 0.5f),
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled,
        elevation = ButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = contentColor,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.button.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = contentColor
            )
        }
    }
}

/**
 * Container 1: Calculator Converter Content (no outer card)
 */
@Composable
private fun CalculatorConverterContent(
    uiState: com.oxyzenq.kconvert.presentation.viewmodel.KconvertUiState,
    currencies: List<Currency>,
    onAmountChange: (String) -> Unit,
    onSourceCurrencySelect: (Currency) -> Unit,
    onTargetCurrencySelect: (Currency) -> Unit,
    onConvert: () -> Unit
) {
    var showSourceCurrencyPicker by remember { mutableStateOf(false) }
    var showTargetCurrencyPicker by remember { mutableStateOf(false) }
    
    Column {
            
            // Amount input
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = onAmountChange,
                label = { Text("Amount", color = Color(0xFF94A3B8)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    cursorColor = Color(0xFF059669),
                    focusedBorderColor = Color(0xFF059669),
                    unfocusedBorderColor = Color(0xFF475569)
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Currency selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CurrencySelector(
                    modifier = Modifier.weight(1f),
                    label = "From",
                    selectedCurrency = uiState.sourceCurrency,
                    onClick = { showSourceCurrencyPicker = true }
                )
                
                CurrencySelector(
                    modifier = Modifier.weight(1f),
                    label = "To",
                    selectedCurrency = uiState.targetCurrency,
                    onClick = { showTargetCurrencyPicker = true }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Convert button
            ElegantButton(
                text = "Convert",
                onClick = onConvert,
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFF059669),
                isLoading = uiState.isConverting,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }
            )
            
            // Conversion result
            uiState.conversionResult?.let { result ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFF059669),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${String.format("%.2f", result.convertedAmount)} ${result.toCurrency}",
                            style = MaterialTheme.typography.h5.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "1 ${result.fromCurrency} = ${String.format("%.4f", result.exchangeRate)} ${result.toCurrency}",
                            style = MaterialTheme.typography.caption.copy(
                                color = Color(0xFFD1FAE5)
                            )
                        )
                    }
                }
            }
        }
        
        // Currency picker dialogs
        if (showSourceCurrencyPicker) {
            CurrencyPickerDialog(
                currencies = currencies,
                onCurrencySelected = { currency ->
                    onSourceCurrencySelect(currency)
                    showSourceCurrencyPicker = false
                },
                onDismiss = { showSourceCurrencyPicker = false }
            )
        }
        
        if (showTargetCurrencyPicker) {
            CurrencyPickerDialog(
                currencies = currencies,
                onCurrencySelected = { currency ->
                    onTargetCurrencySelect(currency)
                    showTargetCurrencyPicker = false
                },
                onDismiss = { showTargetCurrencyPicker = false }
            )
        }
}

/**
 * Container 2: Full Control Panel Content (no outer card)  
 */
@Composable
private fun FullControlPanelContent(
    dataIndicator: String,
    autoUpdateEnabled: Boolean,
    isRefreshing: Boolean,
    onRefreshData: () -> Unit,
    onDeleteData: () -> Unit,
    onToggleAutoUpdate: () -> Unit,
    onRefreshApp: () -> Unit
) {
    Column {
        // Indicator text
        Text(
            text = if (autoUpdateEnabled) "auto update is on" else "auto update is off",
            style = MaterialTheme.typography.caption.copy(
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Medium
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Data status
        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF374151),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = dataIndicator,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.caption.copy(
                    color = Color(0xFF94A3B8)
                )
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Refresh data button
        ElegantButton(
            text = "Refresh Data of Price",
            onClick = onRefreshData,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF059669),
            enabled = !isRefreshing,
            isLoading = isRefreshing,
            icon = {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
            }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Delete data button
        ElegantButton(
            text = "Clear All Data",
            onClick = onDeleteData,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFFDC2626),
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
            }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Auto update toggle
        ElegantButton(
            text = "Auto Update on Launch (Off)",
            onClick = onToggleAutoUpdate,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF6B7280),
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
            }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Refresh app button
        ElegantButton(
            text = "Refresh App",
            onClick = onRefreshApp,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF7C3AED),
            icon = {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
            }
        )
    }
}

/**
 * Container 3: Currency Strength Gauge Content (no outer card)
 */
@Composable
private fun ChartGaugeContent(
    sourceCurrency: String?,
    targetCurrency: String?,
    exchangeRate: Double?
) {
    Column {
        CurrencyStrengthGauge(
            fromCurrency = sourceCurrency,
            toCurrency = targetCurrency,
            exchangeRate = exchangeRate
        )
        Spacer(Modifier.height(8.dp))
        // Helper tip only when data is missing (avoid duplicate with gauge placeholder text)
        if (sourceCurrency == null || targetCurrency == null || exchangeRate == null) {
            Text(
                text = "Select currencies to compare",
                style = MaterialTheme.typography.caption.copy(color = Color(0xFF93C5FD)),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Container 4: What is currency converter?
 */
@Composable
private fun WhatIsCurrencyConverterContainer() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF1E293B),
        elevation = 0.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "What is currency converter?",
                style = MaterialTheme.typography.h6.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "A currency converter is a digital tool that calculates the equivalent value of one currency in terms of another currency. It uses real-time or near real-time exchange rates to provide accurate conversions between different world currencies.\n\n" +
                        "Key features:\n" +
                        "• Real-time exchange rates\n" +
                        "• Support for multiple currencies\n" +
                        "• Historical rate tracking\n" +
                        "• Offline functionality\n" +
                        "• Easy-to-use interface\n\n" +
                        "Currency converters are essential tools for travelers, international businesses, forex traders, and anyone dealing with multiple currencies in their daily activities.",
                style = MaterialTheme.typography.body2.copy(
                    color = Color(0xFF94A3B8),
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
private fun AboutContainer() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF1E293B),
        elevation = 0.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "About",
                style = MaterialTheme.typography.h6.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Kconvert — version com.oxyzenq.kconvert",
                style = MaterialTheme.typography.body2.copy(
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "MIT License",
                style = MaterialTheme.typography.caption.copy(
                    color = Color(0xFF059669)
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡",
                    style = MaterialTheme.typography.h6
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "oxyzenq 2025",
                    style = MaterialTheme.typography.body2.copy(
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

/**
 * Currency selector component
 */
@Composable
private fun CurrencySelector(
    modifier: Modifier = Modifier,
    label: String,
    selectedCurrency: Currency?,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        backgroundColor = Color(0xFF374151),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.caption.copy(
                    color = Color(0xFF94A3B8)
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedCurrency?.code ?: "Select",
                    style = MaterialTheme.typography.body1.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8)
                )
            }
        }
    }
}

/**
 * Currency picker dialog
 */
@Composable
private fun CurrencyPickerDialog(
    currencies: List<Currency>,
    onCurrencySelected: (Currency) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            backgroundColor = Color(0xFF1E293B),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                Text(
                    text = "Select Currency",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.h6.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                
                LazyColumn {
                    items(currencies) { currency ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCurrencySelected(currency) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = currency.code,
                                    style = MaterialTheme.typography.subtitle1.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = currency.name,
                                    style = MaterialTheme.typography.caption.copy(
                                        color = Color(0xFF94A3B8)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
