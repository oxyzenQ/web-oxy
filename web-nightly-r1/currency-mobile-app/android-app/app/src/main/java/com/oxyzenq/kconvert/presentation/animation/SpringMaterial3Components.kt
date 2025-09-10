/*
 * Creativity Authored by oxyzenq 2025
 * 
 * Spring-Enhanced Material 3 Components
 * Auto-animated Material Design components with global spring system
 * Drop-in replacements with premium bouncy interactions
 */

package com.oxyzenq.kconvert.presentation.animation

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * Spring-Enhanced Button
 * Automatic bouncy press animation with Material 3 styling
 */
@Composable
fun SpringButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: androidx.compose.foundation.BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    pressScale: Float = 0.95f,
    springOverride: androidx.compose.animation.core.SpringSpec<Float>? = null,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.globalSpringClick(
            onClick = {},
            enabled = enabled,
            pressScale = pressScale,
            springOverride = springOverride,
            interactionSource = interactionSource
        ),
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * Spring-Enhanced FloatingActionButton
 * Bouncy FAB with enhanced press feedback
 */
@Composable
fun SpringFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = FloatingActionButtonDefaults.shape,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    pressScale: Float = 0.9f,
    hoverScale: Float = 1.1f,
    springOverride: androidx.compose.animation.core.SpringSpec<Float>? = null,
    content: @Composable () -> Unit,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.globalSpringPress(
            onPress = {},
            onRelease = {},
            pressScale = pressScale,
            hoverScale = hoverScale,
            springOverride = springOverride
        ),
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = elevation,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * Spring-Enhanced Card
 * Animated card with hover and press effects
 */
@Composable
fun SpringCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CardDefaults.shape,
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(),
    border: androidx.compose.foundation.BorderStroke? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    pressScale: Float = 0.98f,
    hoverScale: Float = 1.02f,
    springOverride: androidx.compose.animation.core.SpringSpec<Float>? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier.globalSpringPress(
            onPress = {},
            onRelease = {},
            pressScale = pressScale,
            hoverScale = hoverScale,
            springOverride = springOverride
        )
    } else {
        modifier
    }
    
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = cardModifier,
            enabled = enabled,
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = border,
            interactionSource = interactionSource,
            content = content
        )
    } else {
        Card(
            modifier = cardModifier,
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = border,
            content = content
        )
    }
}

/**
 * Spring-Enhanced IconButton
 * Bouncy icon button with ripple effect
 */
@Composable
fun SpringIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    pressScale: Float = 0.9f,
    springOverride: androidx.compose.animation.core.SpringSpec<Float>? = null,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.globalSpringClick(
            onClick = {},
            enabled = enabled,
            pressScale = pressScale,
            springOverride = springOverride,
            interactionSource = interactionSource
        ),
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * Spring-Enhanced Switch
 * Animated switch with bouncy toggle effect
 */
@Composable
fun SpringSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    thumbContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    springOverride: androidx.compose.animation.core.SpringSpec<Float>? = null
) {
    val theme = LocalSpringAnimationTheme.current
    val animationManager = remember { GlobalSpringAnimationManager.getInstance() }
    
    val scale = remember { androidx.compose.animation.core.Animatable(1f) }
    val springSpec = springOverride ?: animationManager.getOptimizedSpring(theme, AnimationType.CLICK)
    
    LaunchedEffect(checked) {
        // Bounce animation when state changes
        scale.animateTo(0.9f, springSpec)
        scale.animateTo(1f, springSpec)
    }
    
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.globalSpringScale(scale.value),
        thumbContent = thumbContent,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource
    )
}

/**
 * Spring-Enhanced Checkbox
 * Bouncy checkbox with spring check animation
 */
@Composable
fun SpringCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CheckboxColors = CheckboxDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    springOverride: androidx.compose.animation.core.SpringSpec<Float>? = null
) {
    val theme = LocalSpringAnimationTheme.current
    val animationManager = remember { GlobalSpringAnimationManager.getInstance() }
    
    val scale = remember { androidx.compose.animation.core.Animatable(1f) }
    val springSpec = springOverride ?: animationManager.getOptimizedSpring(theme, AnimationType.CLICK)
    
    LaunchedEffect(checked) {
        // Bounce animation when checked state changes
        scale.animateTo(0.8f, springSpec)
        scale.animateTo(1f, springSpec)
    }
    
    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.globalSpringScale(scale.value),
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource
    )
}

/**
 * Spring-Enhanced Slider
 * Smooth spring-based value changes
 */
@Composable
fun SpringSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    springOverride: androidx.compose.animation.core.SpringSpec<Float>? = null
) {
    val theme = LocalSpringAnimationTheme.current
    val animationManager = remember { GlobalSpringAnimationManager.getInstance() }
    
    val animatedValue = remember { androidx.compose.animation.core.Animatable(value) }
    val springSpec = springOverride ?: animationManager.getOptimizedSpring(theme, AnimationType.SCALE)
    
    LaunchedEffect(value) {
        animatedValue.animateTo(value, springSpec)
    }
    
    Slider(
        value = animatedValue.value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        interactionSource = interactionSource
    )
}

/**
 * Spring-Enhanced TextField
 * Animated text field with focus spring effects
 */
@Composable
fun SpringTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    keyboardActions: androidx.compose.foundation.text.KeyboardActions = androidx.compose.foundation.text.KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    focusScale: Float = 1.02f,
    springOverride: androidx.compose.animation.core.SpringSpec<Float>? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .globalSpringScale(if (isFocused) focusScale else 1f, springOverride)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            },
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        interactionSource = interactionSource,
        shape = shape,
        colors = colors
    )
}

/**
 * Spring-Enhanced BottomSheet
 * Bouncy bottom sheet with elastic drag behavior
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpringBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    shape: Shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: androidx.compose.ui.unit.Dp = BottomSheetDefaults.Elevation,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    windowInsets: WindowInsets = BottomSheetDefaults.windowInsets,
    springOverride: androidx.compose.animation.core.SpringSpec<Float>? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val theme = LocalSpringAnimationTheme.current
    val animationManager = remember { GlobalSpringAnimationManager.getInstance() }
    
    val springSpec = springOverride ?: animationManager.getOptimizedSpring(theme, AnimationType.OFFSET)
    
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier.globalSpringEntrance(
            visible = sheetState.isVisible,
            enterScale = 0.95f,
            springOverride = springSpec
        ),
        sheetState = sheetState,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        scrimColor = scrimColor,
        dragHandle = dragHandle,
        windowInsets = windowInsets,
        content = content
    )
}

// Extension function removed - using direct import instead
