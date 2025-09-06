package com.oxyzenq.currencyconverter.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.oxyzenq.currencyconverter.data.model.Currency
import com.oxyzenq.currencyconverter.presentation.components.ConfirmationDialog
import com.oxyzenq.currencyconverter.presentation.components.CurrencyStrengthGauge
import com.oxyzenq.currencyconverter.presentation.components.FloatingNotification
import com.oxyzenq.currencyconverter.presentation.viewmodel.ConfirmationType
import com.oxyzenq.currencyconverter.presentation.viewmodel.KconvertViewModel
import kotlinx.coroutines.launch

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
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B),
                        Color(0xFF334155)
                    )
                )
            )
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
            onConfirm = { viewModel.onConfirmationResult(true) },
            onDismiss = { viewModel.onConfirmationResult(false) }
        )
        
        // Error handling
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                // Auto clear error after showing
                kotlinx.coroutines.delay(5000)
                viewModel.clearError()
            }
        }
    }
}

/**
 * Container 1: Calculator Converter
 */
@Composable
private fun CalculatorConverterContainer(
    uiState: com.oxyzenq.currencyconverter.presentation.viewmodel.KconvertUiState,
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
                text = "Kconvert — version com.oxyzenq.currencyconverter",
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
