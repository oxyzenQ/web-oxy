/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.utils

import android.content.Context
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

/**
 * Utility functions for locale-based formatting and detection
 */
object LocaleUtils {
    
    /**
     * Get the current user's locale
     */
    fun getCurrentLocale(context: Context): Locale {
        return context.resources.configuration.locales[0] ?: Locale.getDefault()
    }
    
    /**
     * Detect if user is in US/Imperial system countries
     */
    fun isImperialSystem(context: Context): Boolean {
        val locale = getCurrentLocale(context)
        val country = locale.country.uppercase()
        
        // Countries that use Imperial system for weight/height
        val imperialCountries = setOf(
            "US", // United States
            "LR", // Liberia
            "MM"  // Myanmar (partially)
        )
        
        return country in imperialCountries
    }
    
    /**
     * Get BMI unit system based on locale
     */
    data class BMIUnits(
        val weightUnit: String,
        val heightUnit: String,
        val weightLabel: String,
        val heightLabel: String
    )
    
    fun getBMIUnits(context: Context): BMIUnits {
        return if (isImperialSystem(context)) {
            BMIUnits(
                weightUnit = "lbs",
                heightUnit = "ft/in",
                weightLabel = "Weight (pounds)",
                heightLabel = "Height (feet & inches)"
            )
        } else {
            BMIUnits(
                weightUnit = "kg",
                heightUnit = "cm",
                weightLabel = "Weight (kilograms)",
                heightLabel = "Height (centimeters)"
            )
        }
    }
    
    /**
     * Format currency amount based on locale
     */
    fun formatCurrency(amount: Double, context: Context): String {
        val locale = getCurrentLocale(context)
        val format = NumberFormat.getCurrencyInstance(locale)
        format.maximumFractionDigits = 2
        format.minimumFractionDigits = 2
        return format.format(amount)
    }
    
    /**
     * Format number with proper decimal separators based on locale
     */
    fun formatNumber(number: Double, context: Context, decimalPlaces: Int = 2): String {
        val locale = getCurrentLocale(context)
        val format = DecimalFormat.getInstance(locale) as DecimalFormat
        format.maximumFractionDigits = decimalPlaces
        format.minimumFractionDigits = decimalPlaces
        return format.format(number)
    }
    
    /**
     * Get decimal separator for current locale
     */
    fun getDecimalSeparator(context: Context): Char {
        val locale = getCurrentLocale(context)
        val format = DecimalFormat.getInstance(locale) as DecimalFormat
        return format.decimalFormatSymbols.decimalSeparator
    }
    
    /**
     * Get thousands separator for current locale
     */
    fun getThousandsSeparator(context: Context): Char {
        val locale = getCurrentLocale(context)
        val format = DecimalFormat.getInstance(locale) as DecimalFormat
        return format.decimalFormatSymbols.groupingSeparator
    }
    
    /**
     * Parse currency input respecting locale format
     */
    fun parseCurrencyInput(input: String, context: Context): Double? {
        return try {
            val locale = getCurrentLocale(context)
            val format = DecimalFormat.getInstance(locale) as DecimalFormat
            
            // Remove currency symbols and spaces
            val cleanInput = input.replace(Regex("[^0-9${format.decimalFormatSymbols.decimalSeparator}${format.decimalFormatSymbols.groupingSeparator}]"), "")
            
            format.parse(cleanInput)?.toDouble()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get currency input validation regex for current locale
     */
    fun getCurrencyInputRegex(context: Context): Regex {
        val decimalSep = getDecimalSeparator(context)
        val thousandsSep = getThousandsSeparator(context)
        
        // Allow numbers with proper separators and up to 2 decimal places
        return Regex("^[0-9]{1,3}(?:[$thousandsSep][0-9]{3})*(?:[$decimalSep][0-9]{0,2})?$")
    }
    
    /**
     * Format currency input as user types (live formatting)
     */
    fun formatCurrencyInput(input: String, context: Context): String {
        val number = parseCurrencyInput(input, context) ?: return input
        return formatNumber(number, context, 2)
    }
}
