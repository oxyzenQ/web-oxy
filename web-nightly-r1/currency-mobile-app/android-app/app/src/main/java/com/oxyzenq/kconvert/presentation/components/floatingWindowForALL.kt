/*
 * Global Floating Window components
 * Centralizes layout, bones, and styles for all floating windows.
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oxyzenq.kconvert.data.model.Currency

/**
 * Basic wrappers built on FloatingModal for common message types
 */
@Composable
fun InfoWindow(
    visible: Boolean,
    title: String,
    subtitle: String? = null,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit = {}

/**
 * Currency picker window using global floating modal style.
 */
@Composable
fun CurrencyPickerWindow(
    visible: Boolean,
    title: String = "Select Currency",
    currencies: List<Currency>,
    onCurrencySelected: (Currency) -> Unit,
    onDismiss: () -> Unit
) {
    FloatingModal(
        visible = visible,
        onDismiss = onDismiss,
        width = 320.dp,
        header = {
            FloatingModalHeader(
                title = title,
                subtitle = null,
                icon = null,
                iconTint = Color.White
            )
        }
    ) {
        // Scrollable list with height cap to avoid tall modal
        Box(modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp, max = 360.dp)) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(currencies) { currency ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCurrencySelected(currency)
                                onDismiss()
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = currency.code,
                                style = MaterialTheme.typography.subtitle1.copy(
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
        // Close button
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF374151).copy(alpha = 0.7f),
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.elevation(0.dp)
        ) {
            Text(text = "Close", style = MaterialTheme.typography.button)
        }
    }
}
) {
    FloatingModal(
        visible = visible,
        onDismiss = onDismiss,
        width = 280.dp,
        header = {
            FloatingModalHeader(
                title = title,
                subtitle = subtitle,
                icon = Icons.Default.CheckCircle,
                iconTint = Color(0xFF10B981)
            )
        }
    ) { content() }
}

@Composable
fun ErrorWindow(
    visible: Boolean,
    title: String,
    subtitle: String? = null,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    FloatingModal(
        visible = visible,
        onDismiss = onDismiss,
        width = 280.dp,
        header = {
            FloatingModalHeader(
                title = title,
                subtitle = subtitle,
                icon = Icons.Default.Warning,
                iconTint = Color(0xFFF59E0B)
            )
        }
    ) { content() }
}

@Composable
fun SuccessWindow(
    visible: Boolean,
    title: String,
    subtitle: String? = null,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    FloatingModal(
        visible = visible,
        onDismiss = onDismiss,
        width = 280.dp,
        header = {
            FloatingModalHeader(
                title = title,
                subtitle = subtitle,
                icon = Icons.Default.CheckCircle,
                iconTint = Color(0xFF10B981)
            )
        }
    ) { content() }
}

/**
 * Update result window used by Maintenance section.
 */
@Composable
fun UpdateResultWindow(
    visible: Boolean,
    updateError: Boolean,
    isOutdated: Boolean,
    latestVersion: String,
    updateMessage: String,
    currentVersion: String,
    onDismiss: () -> Unit,
    onOpenLatest: () -> Unit,
    onOpenReleases: () -> Unit
) {
    val icon = when {
        updateError -> Icons.Default.Warning
        isOutdated -> Icons.AutoMirrored.Filled.OpenInNew // will be shown on button; header tint below
        else -> Icons.Default.CheckCircle
    }
    val tint = when {
        updateError -> Color(0xFFF59E0B)
        isOutdated -> Color(0xFF60A5FA)
        else -> Color(0xFF10B981)
    }

    FloatingModal(
        visible = visible,
        onDismiss = onDismiss,
        width = 280.dp,
        header = {
            FloatingModalHeader(
                title = updateMessage,
                subtitle = if (!isOutdated && latestVersion.isNotEmpty()) "Current version: $currentVersion" else null,
                icon = if (updateError) Icons.Default.Warning else if (isOutdated) Icons.Default.CheckCircle else Icons.Default.CheckCircle,
                iconTint = tint
            )
        }
    ) {
        if (isOutdated) {
            Button(
                onClick = onOpenLatest,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF3B82F6),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.elevation(0.dp)
            ) {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open GitHub", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Get Update on GitHub", style = MaterialTheme.typography.button.copy(fontSize = 16.sp))
                }
            }
        } else if (latestVersion.isEmpty()) {
            Button(
                onClick = onOpenReleases,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFFF59E0B),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.elevation(0.dp)
            ) {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Launch, contentDescription = "Visit GitHub", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Visit GitHub Releases", style = MaterialTheme.typography.button.copy(fontSize = 16.sp))
                }
            }
        }

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF374151).copy(alpha = 0.7f),
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.elevation(0.dp)
        ) {
            Text(text = "Close", style = MaterialTheme.typography.button.copy(fontSize = 16.sp))
        }
    }
}

/**
 * Cache management window: scan + clear + cancel.
 * Host screen should handle the actual scan/clear work via callbacks.
 */
@Composable
fun CacheManagementWindow(
    visible: Boolean,
    cacheSizeText: String,
    isScanning: Boolean,
    isClearing: Boolean,
    onScan: () -> Unit,
    onRequestClear: () -> Unit,
    onDismiss: () -> Unit
) {
    FloatingModal(
        visible = visible,
        onDismiss = onDismiss,
        width = 280.dp,
        header = {
            FloatingModalHeader(
                title = "Cache Management",
                subtitle = "Current size: $cacheSizeText",
                icon = Icons.Default.Storage,
                iconTint = Color(0xFF10B981)
            )
        }
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onScan,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF3B82F6).copy(alpha = 0.85f),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.elevation(0.dp),
                enabled = !isScanning && !isClearing
            ) {
                Text(text = if (isScanning) "Scanning..." else "Scan", style = MaterialTheme.typography.button)
            }
            Button(
                onClick = onRequestClear,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFFEF4444).copy(alpha = 0.85f),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.elevation(0.dp),
                enabled = !isScanning && !isClearing
            ) {
                Text(text = if (isClearing) "Clearing..." else "Clear", style = MaterialTheme.typography.button)
            }
        }
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF374151).copy(alpha = 0.7f),
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.elevation(0.dp)
        ) {
            Text(text = "Cancel", style = MaterialTheme.typography.button)
        }
    }
}
