/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.screen

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.oxyzenq.kconvert.R
import com.oxyzenq.kconvert.data.model.Currency
import com.oxyzenq.kconvert.presentation.components.ConfirmationDialog
import com.oxyzenq.kconvert.presentation.components.CurrencyStrengthGauge
import com.oxyzenq.kconvert.presentation.components.FloatingNotification
import com.oxyzenq.kconvert.presentation.components.BottomSheetSettingsPanel
import com.oxyzenq.kconvert.presentation.viewmodel.ConfirmationType
import com.oxyzenq.kconvert.presentation.viewmodel.KconvertViewModel
import kotlinx.coroutines.launch

/**
 * Animated background component with smooth transitions
 */
@Composable
fun AnimatedBackground() {
    val infiniteTransition = rememberInfiniteTransition()
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .scale(scale)
    ) {
        // Stellar HDR wallpaper as background
        Image(
            painter = painterResource(id = R.drawable.hdr_stellar_edition_v2),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Subtle cool-toned gradient overlay to ensure contrast and harmony
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A).copy(alpha = 0.30f), // slate-900 tint top
                            Color(0xFF0B1220).copy(alpha = 0.46f), // deep space mid
                            Color(0xFF0B1220).copy(alpha = 0.66f)  // deeper bottom for content contrast
                        )
                    )
                )
        )
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
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Settings panel state
    var showSettingsPanel by remember { mutableStateOf(false) }
    
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
        AnimatedBackground()
        
        // Enhanced backdrop with blur effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.15f),
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.55f)
                        )
                    )
                )
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Circle Logo and Settings Button
            item {
                GlassmorphismContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Settings button (top-right)
                        IconButton(
                            onClick = { showSettingsPanel = true },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // Center content
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Circle Logo with new centered Kconvert logo
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFF3B82F6),
                                                Color(0xFF1E40AF)
                                            )
                                        ),
                                        RoundedCornerShape(50)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.kconvert_logo_new),
                                    contentDescription = "Kconvert Logo",
                                    modifier = Modifier.size(56.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Kconvert",
                                style = MaterialTheme.typography.h4.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Currency Converter",
                                style = MaterialTheme.typography.subtitle1.copy(
                                    color = Color(0xFF94A3B8)
                                )
                            )
                        }
                    }
                }
            }
            
            // Container 1: Calculator Converter
            item {
                CalculatorConverterContainer(
                    uiState = uiState,
                    currencies = currencies,
                    onAmountChange = viewModel::updateAmount,
                    onSourceCurrencySelect = viewModel::selectSourceCurrency,
                    onTargetCurrencySelect = viewModel::selectTargetCurrency,
                    onConvert = viewModel::convertCurrency
                )
            }
            
            // Container 2: Full Control Panel
            item {
                FullControlPanelContainer(
                    dataIndicator = uiState.dataIndicator,
                    autoUpdateEnabled = autoUpdateEnabled,
                    isRefreshing = uiState.isRefreshing,
                    onRefreshData = { viewModel.showConfirmationDialog(ConfirmationType.REFRESH_DATA) },
                    onDeleteData = { viewModel.showConfirmationDialog(ConfirmationType.DELETE_DATA) },
                    onToggleAutoUpdate = viewModel::toggleAutoUpdate,
                    onRefreshApp = viewModel::refreshApp
                )
            }
            
            // Container 3: Chart / Gauge
            item {
                ChartGaugeContainer(
                    sourceCurrency = uiState.sourceCurrency?.code,
                    targetCurrency = uiState.targetCurrency?.code,
                    exchangeRate = uiState.conversionResult?.exchangeRate
                )
            }
            
            // Container 4: What is currency converter?
            item {
                WhatIsCurrencyConverterContainer()
            }
            
            // Container 5: About
            item {
                AboutContainer()
            }
            
            // GitHub Footer
            item {
                GlassmorphismContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Open Source Project",
                            style = MaterialTheme.typography.caption.copy(
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "com.oxyzenq.kconvert",
                            style = MaterialTheme.typography.caption.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⭐ Star on GitHub",
                            style = MaterialTheme.typography.caption.copy(
                                color = Color(0xFF3B82F6)
                            )
                        )
                    }
                }
            }
            
            // Exit Button
            item {
                GlassmorphismContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            viewModel.showConfirmationDialog(ConfirmationType.EXIT_APP)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Exit App",
                            tint = Color(0xFFDC2626)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Exit Kconvert",
                            style = MaterialTheme.typography.body1.copy(
                                color = Color(0xFFDC2626),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
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
            onDismiss = { showSettingsPanel = false }
        )
    }
}

/**
 * Container 1: Calculator Converter
 */
@Composable
private fun CalculatorConverterContainer(
    uiState: com.oxyzenq.kconvert.presentation.viewmodel.KconvertUiState,
    currencies: List<Currency>,
    onAmountChange: (String) -> Unit,
    onSourceCurrencySelect: (Currency) -> Unit,
    onTargetCurrencySelect: (Currency) -> Unit,
    onConvert: () -> Unit
) {
    var showSourceCurrencyPicker by remember { mutableStateOf(false) }
    var showTargetCurrencyPicker by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF1E293B),
        elevation = 4.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Calculator Converter",
                style = MaterialTheme.typography.h6.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
            Button(
                onClick = onConvert,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF7C3AED)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isConverting
            ) {
                if (uiState.isConverting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Calculate Conversion",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
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
 * Container 2: Full Control Panel
 */
@Composable
private fun FullControlPanelContainer(
    dataIndicator: String,
    autoUpdateEnabled: Boolean,
    isRefreshing: Boolean,
    onRefreshData: () -> Unit,
    onDeleteData: () -> Unit,
    onToggleAutoUpdate: () -> Unit,
    onRefreshApp: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF1E293B),
        elevation = 4.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Full Control Panel",
                style = MaterialTheme.typography.h6.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
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
            
            // Control buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRefreshData,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF059669)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isRefreshing
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (isRefreshing) "Refreshing..." else "refresh data of price",
                        color = Color.White
                    )
                }
                
                Button(
                    onClick = onDeleteData,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFFDC2626)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "delete old data of price",
                        color = Color.White
                    )
                }
                
                Button(
                    onClick = onToggleAutoUpdate,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (autoUpdateEnabled) Color(0xFFF59E0B) else Color(0xFF6B7280)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "auto update on launch (${if (autoUpdateEnabled) "on" else "off"})",
                        color = Color.White
                    )
                }
                
                Button(
                    onClick = onRefreshApp,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF7C3AED)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "refresh app",
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Container 3: Chart / Gauge
 */
@Composable
private fun ChartGaugeContainer(
    sourceCurrency: String?,
    targetCurrency: String?,
    exchangeRate: Double?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF1E293B),
        elevation = 4.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            CurrencyStrengthGauge(
                fromCurrency = sourceCurrency,
                toCurrency = targetCurrency,
                exchangeRate = exchangeRate
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
        elevation = 4.dp,
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
        elevation = 4.dp,
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
