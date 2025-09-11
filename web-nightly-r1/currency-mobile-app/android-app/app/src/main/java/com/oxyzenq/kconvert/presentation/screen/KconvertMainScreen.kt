/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.screen

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.oxyzenq.kconvert.AppVersion
import com.oxyzenq.kconvert.R
import com.oxyzenq.kconvert.data.local.SettingsDataStore
import com.oxyzenq.kconvert.data.model.Currency
import com.oxyzenq.kconvert.presentation.components.BottomNavBar
import com.oxyzenq.kconvert.presentation.components.BottomSheetSettingsPanel
import com.oxyzenq.kconvert.presentation.components.ConfirmationDialog
import com.oxyzenq.kconvert.presentation.components.*
import com.oxyzenq.kconvert.presentation.viewmodel.ConfirmationType
import com.oxyzenq.kconvert.presentation.viewmodel.KconvertViewModel
import com.oxyzenq.kconvert.utils.StorageUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

/**
 * Bouncy press effect modifier: shrinks on press, overshoots slightly on release, then settles.
 */
@Composable
private fun bouncyPress(interactionSource: MutableInteractionSource, pressedScale: Float = 0.94f): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = if (pressed) tween(durationMillis = 110, easing = FastOutLinearInEasing)
        else spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMediumLow),
        label = "bouncy_press_scale"
    )
    return Modifier.scale(scale)
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
    var isFullscreenMode by remember { mutableStateOf(true) }
    var darkLevel by rememberSaveable { mutableStateOf(0) }
    val settingsStore = remember { SettingsDataStore(context) }
    
    // Navbar auto-hide state with DataStore persistence
    var navbarAutoHideEnabled by remember { mutableStateOf(true) }
    var isNavbarVisible by remember { mutableStateOf(true) }
    val isScrolling by remember { derivedStateOf { listState.isScrollInProgress } }
    
    // Load navbar auto-hide setting from DataStore
    LaunchedEffect(Unit) {
        settingsStore.navbarAutoHideFlow.collect { enabled ->
            navbarAutoHideEnabled = enabled
        }
    }
    
    // Improved auto-hide logic: hide when scrolling, show when not scrolling
    LaunchedEffect(isScrolling, navbarAutoHideEnabled) {
        if (navbarAutoHideEnabled) {
            isNavbarVisible = !isScrolling
        } else {
            isNavbarVisible = true
        }
    }
    
    LaunchedEffect(Unit) {
        settingsStore.darkLevelFlow.collect { persistedLevel ->
            darkLevel = persistedLevel
        }
    }
    // hapticsEnabled persisted in DataStore via ViewModel; no local state needed
    
    // Initialize app on first composition
    LaunchedEffect(Unit) {
        viewModel.initializeApp(context)
        
        // Perform cache scan once on app startup
        val settingsStore = SettingsDataStore(context)
        try {
            val cacheSize = StorageUtils.getCacheSize(context)
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            
            // Store cache data in DataStore
            settingsStore.setCacheSize(cacheSize)
            settingsStore.setCacheLastScan(timestamp)
            
            android.widget.Toast.makeText(context, "Cache scan completed", android.widget.Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Cache scan failed", android.widget.Toast.LENGTH_SHORT).show()
        }
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
            .imePadding()
    ) {
        // Animated Background
        val isScrolling by remember { derivedStateOf { listState.isScrollInProgress } }
        AnimatedBackground(isScrolling = isScrolling, isFullscreen = isFullscreenMode, darkLevel = darkLevel)
        
        // Removed duplicate backdrop overlay to reduce overdraw (AnimatedBackground already overlays a gradient)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isFullscreenMode) Modifier
                    else Modifier.statusBarsPadding()
                ),
            contentPadding = PaddingValues(
                top = if (isFullscreenMode) 40.dp else 20.dp, 
                start = 16.dp, 
                end = 16.dp, 
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Container 0 : Header with Circle Logo and Currency Converter Branding
            item(key = "header", contentType = "header") {
                HeaderContainer()
            }
            
            // Container 1: Calculator Converter (Elegant card)
            item(key = "calc_container", contentType = "calc") {
                // Semi-dark backdrop behind container 1 to dim wallpaper
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
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
                        Icon(imageVector = Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, tint = Color(0xFF93C5FD))
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
                WhatIsCurrencyConverterContainer()
            }

            // Container 5: About the app (bundled details)
            item(key = "about_app", contentType = "about") {
                ElegantInfoCard(
                    title = "About the app",
                    titleIcon = { Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = Color(0xFF93C5FD)) }
                ) {
                    Text(
                        text = "Kconvert is a modern, elegant currency converter designed for speed, clarity, and security.",
                        style = MaterialTheme.typography.body2.copy(
                            color = Color(0xFFE5E7EB)
                        ),
                        textAlign = TextAlign.Start
                    )
                    Spacer(Modifier.height(12.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Archive, contentDescription = null, tint = Color(0xFF32EF12))
                            Spacer(Modifier.width(8.dp))
                            Text("Version: ${AppVersion.VERSION_NAME}", style = MaterialTheme.typography.body2.copy(color = Color(0xFFE5E7EB)))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = Color(0xFF32EF12))
                            Spacer(Modifier.width(8.dp))
                            Text("Type: Open Source Project", style = MaterialTheme.typography.body2.copy(color = Color(0xFFE5E7EB)))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = Color(0xFF32EF12))
                            Spacer(Modifier.width(8.dp))
                            Text("Status: Maintenance", style = MaterialTheme.typography.body2.copy(color = Color(0xFFE5E7EB)))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = Color(0xFF32EF12))
                            Spacer(Modifier.width(8.dp))
                            Text("License: MIT", style = MaterialTheme.typography.body2.copy(color = Color(0xFFE5E7EB)))
                        }
                    }
                }
            }
            
            // Container 6: Footer spacer for navbar
            item(key = "footer_spacer", contentType = "footer") {
                Spacer(modifier = Modifier.height(120.dp)) // Increased space for larger iOS navbar
            }
        }
        
        // Removed top-right settings button - now in container 0 and bottom navbar

        // Floating notifications and dialogs - temporarily removed due to import issues
        
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
        
        // Bottom Navigation Bar
        BottomNavBar(
            isVisible = isNavbarVisible,
            onExitClick = {
                viewModel.showConfirmationDialog(ConfirmationType.EXIT_APP)
            },
            onSettingsClick = {
                showSettingsPanel = true
            },
            onResetAppClick = {
                // Reset app to default state and scroll to top
                if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.resetAppToDefault()
                coroutineScope.launch {
                    listState.animateScrollToItem(
                        index = 0,
                        scrollOffset = 0
                    )
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        
        // Settings Panel
        BottomSheetSettingsPanel(
            onDismiss = { showSettingsPanel = false },
            hapticsEnabled = hapticsEnabled,
            onToggleHaptics = { enabled -> viewModel.setHapticsEnabled(enabled) },
            isFullscreenMode = isFullscreenMode,
            onToggleFullscreen = { enabled -> isFullscreenMode = enabled },
            darkLevel = darkLevel,
            onDarkLevelChange = { level -> darkLevel = level },
            navbarAutoHideEnabled = navbarAutoHideEnabled,
            onToggleNavbarAutoHide = { enabled -> navbarAutoHideEnabled = enabled },
            isVisible = showSettingsPanel
        )
    }
}



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
    backgroundBrush: Brush? = null,
    contentColor: Color = Color.White,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val interaction = remember { MutableInteractionSource() }
    val finalModifier = if (backgroundBrush != null) {
        modifier
            .then(bouncyPress(interaction))
            .background(
                brush = backgroundBrush,
                shape = RoundedCornerShape(12.dp)
            )
    } else {
        modifier.then(bouncyPress(interaction))
    }
    
    Button(
        onClick = onClick,
        modifier = finalModifier,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (backgroundBrush != null) Color.Transparent else backgroundColor.copy(alpha = if (enabled) 0.9f else 0.5f),
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
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        interactionSource = interaction
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
            
            // Amount input with iOS-like design
            // Display empty when underlying value is "0" so placeholder shows
            val displayAmount = if (uiState.amount == "0") "" else uiState.amount
            OutlinedTextField(
                value = displayAmount,
                onValueChange = { newValue ->
                    // If user clears, propagate empty; ViewModel can treat empty as zero lazily
                    onAmountChange(newValue)
                },
                placeholder = { Text("Enter amount first", color = Color(0xFF64748B)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        // Hide keyboard when Done is pressed
                        // Note: KeyboardController access moved to remember block
                    }
                ),
                singleLine = true,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    cursorColor = Color(0xFF06B6D4),
                    focusedBorderColor = Color(0xFF334155).copy(alpha = 0.6f),
                    unfocusedBorderColor = Color(0xFF475569).copy(alpha = 0.4f),
                    backgroundColor = Color(0xFF1E293B).copy(alpha = 0.3f)
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
            
            // Convert button with iOS-like design
            Button(
                onClick = onConvert,
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
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Convert",
                        style = MaterialTheme.typography.button.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        color = Color.White
                    )
                }
            }
            
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
    onDeleteData: () -> Unit
) {
    Column {
        // Auto-update status indicator (styled same as data indicator)
        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF374151),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = if (autoUpdateEnabled) "auto update is on" else "auto update is off",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.caption.copy(
                    color = Color(0xFF94A3B8)
                )
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Data status indicator
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
        
        // Refresh data button with iOS-like design
        Button(
            onClick = onRefreshData,
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
            enabled = !isRefreshing
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Refresh Data of Price",
                    style = MaterialTheme.typography.button.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Delete data button with iOS-like design
        Button(
            onClick = onDeleteData,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFFDC2626).copy(alpha = 0.8f),
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
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Clear All Data",
                    style = MaterialTheme.typography.button.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = Color.White
                )
            }
        }
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
    // Use centralized global floating window
    CurrencyPickerWindow(
        visible = true,
        title = "Select Currency",
        currencies = currencies,
        onCurrencySelected = onCurrencySelected,
        onDismiss = onDismiss
    )
}
