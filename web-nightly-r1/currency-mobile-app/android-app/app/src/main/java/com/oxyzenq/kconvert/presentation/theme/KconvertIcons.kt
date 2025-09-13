/*
 * Creativity Authored by oxyzenq 2025
 * 
 * Global Material Icons Configuration
 * Centralized icon management for easy maintenance and consistency
 */

package com.oxyzenq.kconvert.presentation.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Centralized Material Icons Configuration
 * All icons used throughout the app are defined here for easy maintenance
 */
object KconvertIcons {
    
    // Navigation & UI Icons
    val ArrowDropDown: ImageVector = Icons.Default.ArrowDropDown
    val ArrowBack = Icons.AutoMirrored.Filled.ArrowBack
    val Menu: ImageVector = Icons.Default.Menu
    val Close: ImageVector = Icons.Default.Close
    val MoreVert: ImageVector = Icons.Default.MoreVert
    
    // Security & System Icons
    val Lock: ImageVector = Icons.Default.Lock
    val CheckCircle: ImageVector = Icons.Default.CheckCircle
    val Warning: ImageVector = Icons.Default.Warning
    val Refresh: ImageVector = Icons.Default.Refresh
    val Shield: ImageVector = Icons.Default.Security
    
    // Action Icons
    val ContentCopy: ImageVector = Icons.Default.ContentCopy
    val Download: ImageVector = Icons.Default.Download
    val Delete: ImageVector = Icons.Default.Delete
    val Share: ImageVector = Icons.Default.Share
    val Edit: ImageVector = Icons.Default.Edit
    val Save: ImageVector = Icons.Default.Save
    
    // Information & Help Icons
    val Info: ImageVector = Icons.Default.Info
    val Help: ImageVector = Icons.AutoMirrored.Filled.Help
    val HelpOutline: ImageVector = Icons.AutoMirrored.Filled.HelpOutline
    val Star: ImageVector = Icons.Default.Star
    val Favorite: ImageVector = Icons.Default.Favorite
    
    // Settings & Configuration Icons
    val Settings: ImageVector = Icons.Default.Settings
    val Build: ImageVector = Icons.Default.Build
    val Brightness6: ImageVector = Icons.Default.Brightness6
    val Tune: ImageVector = Icons.Default.Tune
    val Palette: ImageVector = Icons.Default.Palette
    
    // Update & System Icons
    val SystemUpdate: ImageVector = Icons.Default.SystemUpdate
    val GetApp: ImageVector = Icons.Default.GetApp
    val OpenInBrowser: ImageVector = Icons.Default.OpenInBrowser
    val CloudDownload: ImageVector = Icons.Default.CloudDownload
    
    // Currency & Finance Icons
    val AttachMoney: ImageVector = Icons.Default.AttachMoney
    val MonetizationOn: ImageVector = Icons.Default.MonetizationOn
    val TrendingUp: ImageVector = Icons.AutoMirrored.Filled.TrendingUp
    val TrendingDown: ImageVector = Icons.AutoMirrored.Filled.TrendingDown
    val AccountBalance: ImageVector = Icons.Default.AccountBalance
    val CurrencyExchange: ImageVector = Icons.Default.CurrencyExchange
    val TimerArrowUp: ImageVector = Icons.Default.Schedule // Using Schedule as placeholder for timer_arrow_up_24
    val Cognition: ImageVector = Icons.Default.Psychology // Using Psychology as placeholder for cognition_2_24
    val ShieldLock: ImageVector = Icons.Default.Security // Using Security as placeholder for shield_lock_24
    
    // Notification & Status Icons
    val Notifications: ImageVector = Icons.Default.Notifications
    val NotificationsOff: ImageVector = Icons.Default.NotificationsOff
    val Check: ImageVector = Icons.Default.Check
    val Error: ImageVector = Icons.Default.Error
    val ErrorOutline: ImageVector = Icons.Default.ErrorOutline
    
    // Home & Navigation Icons
    val Home: ImageVector = Icons.Default.Home
    val Dashboard: ImageVector = Icons.Default.Dashboard
    val AccountCircle: ImageVector = Icons.Default.AccountCircle
    val Person: ImageVector = Icons.Default.Person
    
    // Visibility & Display Icons
    val Visibility: ImageVector = Icons.Default.Visibility
    val VisibilityOff: ImageVector = Icons.Default.VisibilityOff
    val Fullscreen: ImageVector = Icons.Default.Fullscreen
    val FullscreenExit: ImageVector = Icons.Default.FullscreenExit
    
    // Data & Storage Icons
    val Storage: ImageVector = Icons.Default.Storage
    val Folder: ImageVector = Icons.Default.Folder
    val InsertDriveFile: ImageVector = Icons.AutoMirrored.Filled.InsertDriveFile
    val CloudUpload: ImageVector = Icons.Default.CloudUpload
    
    // Communication Icons
    val Email: ImageVector = Icons.Default.Email
    val Phone: ImageVector = Icons.Default.Phone
    val Message: ImageVector = Icons.AutoMirrored.Filled.Message
    val Send: ImageVector = Icons.AutoMirrored.Filled.Send
}

/**
 * Icon Categories for Organized Access
 * Use these when you need to group icons by functionality
 */
object IconCategories {
    
    val navigation = listOf(
        KconvertIcons.ArrowDropDown,
        KconvertIcons.ArrowBack,
        KconvertIcons.Menu,
        KconvertIcons.Close
    )
    
    val security = listOf(
        KconvertIcons.Lock,
        KconvertIcons.CheckCircle,
        KconvertIcons.Warning,
        KconvertIcons.Shield
    )
    
    val currency = listOf(
        KconvertIcons.AttachMoney,
        KconvertIcons.MonetizationOn,
        KconvertIcons.TrendingUp,
        KconvertIcons.TrendingDown,
        KconvertIcons.Refresh,
        KconvertIcons.CurrencyExchange
    )
    
    val settings = listOf(
        KconvertIcons.Settings,
        KconvertIcons.Build,
        KconvertIcons.Brightness6,
        KconvertIcons.Tune,
        KconvertIcons.Palette
    )
}

/**
 * Quick Access Aliases for Commonly Used Icons
 * Use these for frequently accessed icons throughout the app
 */
val AppIcons = KconvertIcons
