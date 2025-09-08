package com.oxyzenq.kconvert.utils

import android.content.Context
import java.io.File
import kotlin.math.log10
import kotlin.math.pow

/**
 * Utility functions for calculating storage usage
 */
object StorageUtils {
    
    /**
     * Calculate total cache size for the app (fast scan)
     */
    fun getCacheSize(context: Context): Long {
        return try {
            var totalSize = 0L
            
            // Internal cache directory (most important)
            context.cacheDir?.let { cacheDir ->
                if (cacheDir.exists()) {
                    totalSize += getFolderSizeFast(cacheDir)
                }
            }
            
            // External cache directory (if available)
            context.externalCacheDir?.let { externalCacheDir ->
                if (externalCacheDir.exists()) {
                    totalSize += getFolderSizeFast(externalCacheDir)
                }
            }
            
            totalSize
        } catch (e: Exception) {
            0L // Return 0 if any error occurs
        }
    }
    
    /**
     * Calculate app data size (excluding cache)
     */
    fun getAppDataSize(context: Context): Long {
        var totalSize = 0L
        
        // Internal files directory
        context.filesDir?.let { filesDir ->
            totalSize += getFolderSize(filesDir)
        }
        
        // External files directory (if available)
        context.getExternalFilesDir(null)?.let { externalFilesDir ->
            totalSize += getFolderSize(externalFilesDir)
        }
        
        // Databases
        context.databaseList().forEach { dbName ->
            context.getDatabasePath(dbName)?.let { dbFile ->
                if (dbFile.exists()) {
                    totalSize += dbFile.length()
                }
            }
        }
        
        // Shared preferences
        val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
        if (prefsDir.exists()) {
            totalSize += getFolderSize(prefsDir)
        }
        
        return totalSize
    }
    
    /**
     * Calculate total app storage usage (cache + data)
     */
    fun getTotalAppSize(context: Context): Long {
        return getCacheSize(context) + getAppDataSize(context)
    }
    
    /**
     * Fast folder size calculation (non-recursive for speed)
     */
    private fun getFolderSizeFast(folder: File): Long {
        return try {
            if (!folder.exists() || !folder.isDirectory) return 0L
            
            var size = 0L
            folder.listFiles()?.forEach { file ->
                size += if (file.isFile) file.length() else 0L
            }
            size
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Recursively calculate folder size (for detailed scans)
     */
    private fun getFolderSize(folder: File): Long {
        var size = 0L
        
        try {
            if (!folder.exists() || !folder.isDirectory) {
                return 0L
            }
            
            folder.listFiles()?.forEach { file ->
                size += if (file.isDirectory) {
                    getFolderSize(file)
                } else {
                    file.length()
                }
            }
        } catch (e: Exception) {
            // Handle permission issues or other errors gracefully
            return 0L
        }
        
        return size
    }
    
    /**
     * Format bytes to human readable format
     */
    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
        
        val size = bytes / 1024.0.pow(digitGroups.toDouble())
        
        return String.format("%.1f %s", size, units[digitGroups])
    }
    
    /**
     * Clear app cache
     */
    fun clearCache(context: Context): Boolean {
        return try {
            var cleared = true
            
            // Clear internal cache
            context.cacheDir?.let { cacheDir ->
                cleared = cleared && deleteRecursively(cacheDir)
            }
            
            // Clear external cache
            context.externalCacheDir?.let { externalCacheDir ->
                cleared = cleared && deleteRecursively(externalCacheDir)
            }
            
            // Clear code cache
            try {
                context.codeCacheDir?.let { codeCacheDir ->
                    cleared = cleared && deleteRecursively(codeCacheDir)
                }
            } catch (e: Exception) {
                // Ignore if not available
            }
            
            cleared
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Delete directory contents recursively
     */
    private fun deleteRecursively(file: File): Boolean {
        return try {
            if (file.isDirectory) {
                file.listFiles()?.forEach { child ->
                    deleteRecursively(child)
                }
            }
            // Don't delete the cache directory itself, just its contents
            if (file.name == "cache" || file.name == "code_cache") {
                file.listFiles()?.forEach { it.delete() }
                true
            } else {
                file.delete()
            }
        } catch (e: Exception) {
            false
        }
    }
}
