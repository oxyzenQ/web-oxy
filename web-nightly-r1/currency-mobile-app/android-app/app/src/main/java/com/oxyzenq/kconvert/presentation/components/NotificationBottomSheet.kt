/*
 * Premium Notification Bottom Sheet with Glassmorphism
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.oxyzenq.kconvert.data.local.entity.NotifyMessage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NotificationBottomSheet(
    notifications: List<NotifyMessage>,
    onDismiss: () -> Unit,
    onDeleteMessage: (Long) -> Unit,
    onManualRefresh: () -> Unit = {},
    hapticsEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // Sheet state management
    var sheetHeight by remember { mutableStateOf(screenHeight * 0.5f) }
    var isDragging by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(true) }
    val minHeight = screenHeight * 0.3f
    val maxHeight = screenHeight * 0.95f

    // Animation states
    val animatedHeight by animateDpAsState(
        targetValue = if (isVisible) sheetHeight else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    val backdropAlpha by animateFloatAsState(
        targetValue = if (isVisible) 0.6f else 0f,
        animationSpec = tween(300)
    )

    if (isVisible || animatedHeight > 0.dp) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1000f)
        ) {
            // Backdrop
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = backdropAlpha))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (!isDragging) onDismiss()
                    }
            )

            // Bottom Sheet
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(animatedHeight)
                    .align(Alignment.BottomCenter)
                    .offset { IntOffset(0, 0) }
            ) {
                NotificationSheetContent(
                    messages = notifications,
                    isFullScreen = sheetHeight >= maxHeight * 0.9f,
                    onClose = onDismiss,
                    onDeleteMessage = onDeleteMessage,
                    onManualRefresh = onManualRefresh,
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false },
                    onHeightChange = { newHeight ->
                        sheetHeight = newHeight.coerceIn(minHeight, maxHeight)
                        if (newHeight < minHeight * 0.7f) {
                            scope.launch { onDismiss() }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun NotificationSheetContent(
    messages: List<NotifyMessage>,
    isFullScreen: Boolean,
    onClose: () -> Unit,
    onDeleteMessage: (Long) -> Unit,
    onManualRefresh: () -> Unit,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onHeightChange: (dp: Dp) -> Unit
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E1B4B).copy(alpha = 0.95f),
                        Color(0xFF312E81).copy(alpha = 0.90f),
                        Color(0xFF1E1B4B).copy(alpha = 0.95f)
                    )
                ),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.2f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        // Drag handle and header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { onDragStart() },
                        onDragEnd = { onDragEnd() }
                    ) { _, dragAmount ->
                        val currentHeight = screenHeight * 0.5f // This should be passed as parameter
                        val newHeight = currentHeight - with(density) { dragAmount.y.toDp() }
                        onHeightChange(newHeight)
                    }
                }
                .padding(vertical = 12.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        Color.White.copy(alpha = 0.3f),
                        RoundedCornerShape(2.dp)
                    )
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Header with title and close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.h6.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                if (isFullScreen) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(32.dp))
                }
            }

            // Divider
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                color = Color.White.copy(alpha = 0.2f),
                thickness = 1.dp
            )
        }

        // Message list
        if (messages.isEmpty()) {
            EmptyNotificationState(onManualRefresh = onManualRefresh)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    NotificationMessageRow(
                        message = message,
                        onDelete = { onDeleteMessage(message.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyNotificationState(
    onManualRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.4f),
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No notifications",
            style = MaterialTheme.typography.h6.copy(
                color = Color.White.copy(alpha = 0.6f)
            )
        )
        
        Text(
            text = "You're all caught up!",
            style = MaterialTheme.typography.body2.copy(
                color = Color.White.copy(alpha = 0.4f)
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onManualRefresh,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF8B5CF6).copy(alpha = 0.8f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Check for Updates",
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun NotificationMessageRow(
    message: NotifyMessage,
    onDelete: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color.White.copy(alpha = 0.1f),
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.15f)
        ),
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Timestamp
            Text(
                text = formatTimestamp(message.timestamp),
                style = MaterialTheme.typography.caption.copy(
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Message content
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Title if exists
                    message.title?.let { title ->
                        Text(
                            text = title,
                            style = MaterialTheme.typography.subtitle2.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = when (message.messageType) {
                                    "UPDATE" -> Color(0xFF10B981)
                                    "ERROR" -> Color(0xFFEF4444)
                                    else -> Color.White
                                }
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Body with gradient fade
                    Text(
                        text = message.body,
                        style = MaterialTheme.typography.body2.copy(
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 18.sp
                        ),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.drawWithContent {
                            drawContent()
                            // Gradient fade at the end
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF1E1B4B).copy(alpha = 0.3f)
                                    ),
                                    startX = size.width * 0.8f,
                                    endX = size.width
                                )
                            )
                        }
                    )
                }

                // Delete button
                IconButton(
                    onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showDeleteDialog = true 
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            backgroundColor = Color(0xFF1E1B4B).copy(alpha = 0.95f),
            border = BorderStroke(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Delete Notification",
                    style = MaterialTheme.typography.h6.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Are you sure you want to delete this notification?",
                    style = MaterialTheme.typography.body2.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.White.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", color = Color.White)
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Delete", color = Color.White)
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm • dd MMM", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
