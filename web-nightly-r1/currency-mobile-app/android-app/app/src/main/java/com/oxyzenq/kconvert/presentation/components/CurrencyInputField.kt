/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.oxyzenq.kconvert.utils.LocaleUtils

/**
 * Enhanced currency input field with locale-aware formatting
 */
@Composable
fun CurrencyInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Amount",
    placeholder: String = "Enter amount",
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Get locale-specific formatting
    val decimalSeparator = LocaleUtils.getDecimalSeparator(context)
    val thousandsSeparator = LocaleUtils.getThousandsSeparator(context)
    
    // Format placeholder based on locale
    val localizedPlaceholder = remember(context) {
        val bmiUnits = LocaleUtils.getBMIUnits(context)
        if (LocaleUtils.isImperialSystem(context)) {
            "e.g. 1${thousandsSeparator}000${decimalSeparator}00"
        } else {
            "e.g. 1${thousandsSeparator}000${decimalSeparator}00"
        }
    }
    
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Validate input based on locale
            val regex = LocaleUtils.getCurrencyInputRegex(context)
            if (newValue.isEmpty() || regex.matches(newValue)) {
                onValueChange(newValue)
            }
        },
        label = { 
            Text(
                text = label,
                color = Color(0xFF94A3B8)
            )
        },
        placeholder = { 
            Text(
                text = localizedPlaceholder,
                color = Color(0xFF64748B)
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
            }
        ),
        singleLine = true,
        maxLines = 1,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1E40AF), // Deep blue
                        Color(0xFF3B82F6), // Blue
                        Color(0xFF06B6D4), // Cyan
                        Color(0xFF8B5CF6), // Purple
                        Color(0xFF1E293B)  // Dark slate
                    )
                ),
                shape = RoundedCornerShape(4.dp)
            ),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = Color.White,
            cursorColor = Color(0xFF06B6D4),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent
        )
    )
}

/**
 * BMI Input Field with Imperial/Metric detection
 */
@Composable
fun BMIInputField(
    value: String,
    onValueChange: (String) -> Unit,
    fieldType: BMIFieldType,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val bmiUnits = LocaleUtils.getBMIUnits(context)
    
    val (label, placeholder, unit) = when (fieldType) {
        BMIFieldType.WEIGHT -> Triple(
            bmiUnits.weightLabel,
            if (LocaleUtils.isImperialSystem(context)) "e.g. 150" else "e.g. 70",
            bmiUnits.weightUnit
        )
        BMIFieldType.HEIGHT -> Triple(
            bmiUnits.heightLabel,
            if (LocaleUtils.isImperialSystem(context)) "e.g. 5'8\"" else "e.g. 175",
            bmiUnits.heightUnit
        )
    }
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { 
            Text(
                text = "$label ($unit)",
                color = Color(0xFF94A3B8)
            )
        },
        placeholder = { 
            Text(
                text = placeholder,
                color = Color(0xFF64748B)
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (fieldType == BMIFieldType.HEIGHT && LocaleUtils.isImperialSystem(context)) {
                KeyboardType.Text // For feet'inches" format
            } else {
                KeyboardType.Decimal
            },
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                // Move to next field or hide keyboard
            }
        ),
        singleLine = true,
        maxLines = 1,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .imePadding(),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = Color.White,
            cursorColor = Color(0xFF06B6D4),
            focusedBorderColor = Color(0xFF059669),
            unfocusedBorderColor = Color(0xFF475569)
        )
    )
}

enum class BMIFieldType {
    WEIGHT,
    HEIGHT
}
