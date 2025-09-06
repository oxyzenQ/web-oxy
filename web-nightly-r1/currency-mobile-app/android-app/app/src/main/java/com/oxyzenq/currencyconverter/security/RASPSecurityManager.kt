/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.currencyconverter.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runtime Application Self-Protection (RASP) Security Manager
 * Provides real-time threat detection and response
 */
@Singleton
class RASPSecurityManager @Inject constructor() {
    
    companion object {
        private const val TAG = "RASPSecurity"
        private const val SECURITY_CHECK_INTERVAL = 300_000L // 5 minutes
        
        // Expected app signature hash (replace with your actual signature)
        private const val EXPECTED_SIGNATURE_HASH = "SHA256:A1B2C3D4E5F6789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789A"
    }
    
    private var lastSecurityCheck = 0L
    private var securityStatus = SecurityStatus.UNKNOWN
    private val securityChecks: List<(Context) -> SecurityCheckResult> = listOf(
        ::checkRootDetection,
        ::checkHookDetection,
        ::checkEmulatorDetection,
        ::checkTamperDetection,
        ::checkDebuggingDetection,
        ::checkAppIntegrity
    )
    
    enum class SecurityStatus {
        SECURE,
        COMPROMISED,
        UNKNOWN
    }
    
    enum class ThreatLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
    
    data class SecurityThreat(
        val type: String,
        val level: ThreatLevel,
        val description: String,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    private val detectedThreats = mutableListOf<SecurityThreat>()
    
    /**
     * Perform comprehensive security assessment
     */
    suspend fun performSecurityAssessment(context: Context): SecurityStatus = withContext(Dispatchers.IO) {
        val currentTime = System.currentTimeMillis()
        
        // Skip if recently checked
        if (currentTime - lastSecurityCheck < SECURITY_CHECK_INTERVAL && securityStatus != SecurityStatus.UNKNOWN) {
            return@withContext securityStatus
        }
        
        lastSecurityCheck = currentTime
        detectedThreats.clear()
        
        try {
            var allChecksPassed = true
            
            // Run all security checks
            for (check in securityChecks) {
                val result = check(context)
                if (!result.passed) {
                    allChecksPassed = false
                    result.threat?.let { threat ->
                        detectedThreats.add(threat)
                        
                        // Log threat for debugging (remove in production)
                        Log.w(TAG, "Security threat detected: ${threat.type} - ${threat.description}")
                        
                        // Critical threats immediately fail
                        if (threat.level == ThreatLevel.CRITICAL) {
                            securityStatus = SecurityStatus.COMPROMISED
                            return@withContext SecurityStatus.COMPROMISED
                        }
                    }
                }
            }
            
            securityStatus = if (allChecksPassed) {
                SecurityStatus.SECURE
            } else {
                // Evaluate threat severity
                val criticalThreats = detectedThreats.count { it.level == ThreatLevel.CRITICAL }
                val highThreats = detectedThreats.count { it.level == ThreatLevel.HIGH }
                
                when {
                    criticalThreats > 0 -> SecurityStatus.COMPROMISED
                    highThreats > 1 -> SecurityStatus.COMPROMISED
                    else -> SecurityStatus.SECURE // Allow low/medium threats
                }
            }
            
            securityStatus
        } catch (e: Exception) {
            Log.e(TAG, "Security assessment failed", e)
            securityStatus = SecurityStatus.COMPROMISED // Fail secure
            securityStatus
        }
    }
    
    /**
     * Check if device is rooted
     */
    private fun checkRootDetection(context: Context): SecurityCheckResult {
        val rootIndicators = arrayOf(
            // Common su binary locations
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su",
            
            // Root management apps
            "/system/app/SuperSU.apk",
            "/system/app/Kinguser.apk",
            "/data/data/eu.chainfire.supersu",
            "/data/data/com.noshufou.android.su",
            "/data/data/com.koushikdutta.superuser",
            "/data/data/com.thirdparty.superuser",
            "/data/data/com.yellowes.su",
            
            // Busybox
            "/system/xbin/busybox",
            "/system/bin/busybox"
        )
        
        val rootFound = rootIndicators.any { File(it).exists() }
        
        // Additional checks
        val buildTags = Build.TAGS?.contains("test-keys") == true
        val suProcess = try {
            Runtime.getRuntime().exec("su").waitFor() == 0
        } catch (e: Exception) {
            false
        }
        
        val isRooted = rootFound || buildTags || suProcess
        
        return if (isRooted) {
            SecurityCheckResult(
                false,
                SecurityThreat("ROOT_DETECTION", ThreatLevel.HIGH, "Device appears to be rooted")
            )
        } else {
            SecurityCheckResult(true, null)
        }
    }
    
    /**
     * Detect hooking frameworks (Xposed, Frida, etc.)
     */
    private fun checkHookDetection(context: Context): SecurityCheckResult {
        val threats = mutableListOf<String>()
        
        // Check for Xposed Framework
        try {
            Class.forName("de.robv.android.xposed.XposedHelpers")
            threats.add("Xposed Framework detected")
        } catch (e: ClassNotFoundException) {
            // Good, no Xposed
        }
        
        try {
            Class.forName("de.robv.android.xposed.XposedBridge")
            threats.add("Xposed Bridge detected")
        } catch (e: ClassNotFoundException) {
            // Good, no Xposed Bridge
        }
        
        // Check for Frida
        val fridaIndicators = arrayOf(
            "/data/local/tmp/frida-server",
            "/data/local/tmp/re.frida.server",
            "/sdcard/frida-server",
            "/data/data/re.frida.server"
        )
        
        if (fridaIndicators.any { File(it).exists() }) {
            threats.add("Frida framework detected")
        }
        
        // Check for Substrate (Cydia Substrate)
        try {
            Class.forName("com.saurik.substrate.MS")
            threats.add("Substrate framework detected")
        } catch (e: ClassNotFoundException) {
            // Good, no Substrate
        }
        
        return if (threats.isNotEmpty()) {
            SecurityCheckResult(
                false,
                SecurityThreat("HOOK_DETECTION", ThreatLevel.CRITICAL, threats.joinToString(", "))
            )
        } else {
            SecurityCheckResult(true, null)
        }
    }
    
    /**
     * Detect if running in emulator
     */
    private fun checkEmulatorDetection(context: Context): SecurityCheckResult {
        val emulatorIndicators = listOf(
            Build.FINGERPRINT.contains("generic"),
            Build.FINGERPRINT.contains("unknown"),
            Build.FINGERPRINT.contains("emulator"),
            Build.MODEL.contains("google_sdk"),
            Build.MODEL.contains("Emulator"),
            Build.MODEL.contains("Android SDK built for x86"),
            Build.MANUFACTURER.contains("Genymotion"),
            Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"),
            Build.DEVICE.contains("generic"),
            Build.PRODUCT.contains("sdk"),
            Build.HARDWARE.contains("goldfish"),
            Build.HARDWARE.contains("ranchu"),
            "1" == Settings.Secure.getString(context.contentResolver, Settings.Secure.ADB_ENABLED)
        )
        
        val emulatorDetected = emulatorIndicators.any { it }
        
        return if (emulatorDetected) {
            SecurityCheckResult(
                false,
                SecurityThreat("EMULATOR_DETECTION", ThreatLevel.MEDIUM, "Running in emulator environment")
            )
        } else {
            SecurityCheckResult(true, null)
        }
    }
    
    /**
     * Check for app tampering
     */
    private fun checkTamperDetection(context: Context): SecurityCheckResult {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
            
            val signatures = packageInfo.signatures
            if (signatures.isEmpty()) {
                return SecurityCheckResult(
                    false,
                    SecurityThreat("TAMPER_DETECTION", ThreatLevel.CRITICAL, "No app signature found")
                )
            }
            
            // Calculate signature hash
            val signature = signatures[0]
            val md = MessageDigest.getInstance("SHA-256")
            val hash = md.digest(signature.toByteArray())
            val hashString = "SHA256:" + hash.joinToString("") { "%02X".format(it) }
            
            // Compare with expected signature (in production, use your actual signature)
            val signatureValid = hashString == EXPECTED_SIGNATURE_HASH || 
                                Build.TYPE == "eng" || // Allow debug builds
                                Build.TYPE == "userdebug"
            
            if (!signatureValid) {
                SecurityCheckResult(
                    false,
                    SecurityThreat("TAMPER_DETECTION", ThreatLevel.CRITICAL, "App signature mismatch")
                )
            } else {
                SecurityCheckResult(true, null)
            }
        } catch (e: Exception) {
            SecurityCheckResult(
                false,
                SecurityThreat("TAMPER_DETECTION", ThreatLevel.HIGH, "Signature verification failed: ${e.message}")
            )
        }
    }
    
    /**
     * Check for debugging
     */
    private fun checkDebuggingDetection(context: Context): SecurityCheckResult {
        val debuggingIndicators = listOf(
            android.os.Debug.isDebuggerConnected(),
            (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0,
            Settings.Secure.getInt(context.contentResolver, Settings.Secure.ADB_ENABLED, 0) == 1
        )
        
        val debuggingDetected = debuggingIndicators.any { it }
        
        return if (debuggingDetected && Build.TYPE != "eng" && Build.TYPE != "userdebug") {
            SecurityCheckResult(
                false,
                SecurityThreat("DEBUG_DETECTION", ThreatLevel.HIGH, "Debugging environment detected")
            )
        } else {
            SecurityCheckResult(true, null)
        }
    }
    
    /**
     * Check app integrity
     */
    private fun checkAppIntegrity(context: Context): SecurityCheckResult {
        return try {
            // Check if app is installed from trusted source
            val installer = context.packageManager.getInstallerPackageName(context.packageName)
            val trustedInstallers = setOf(
                "com.android.vending", // Google Play Store
                "com.amazon.venezia",  // Amazon Appstore
                "com.sec.android.app.samsungapps", // Samsung Galaxy Store
                null // Direct install (for development)
            )
            
            val installerTrusted = installer in trustedInstallers
            
            if (!installerTrusted) {
                SecurityCheckResult(
                    false,
                    SecurityThreat("INTEGRITY_CHECK", ThreatLevel.MEDIUM, "App installed from untrusted source: $installer")
                )
            } else {
                SecurityCheckResult(true, null)
            }
        } catch (e: Exception) {
            SecurityCheckResult(
                false,
                SecurityThreat("INTEGRITY_CHECK", ThreatLevel.LOW, "Integrity check failed: ${e.message}")
            )
        }
    }
    
    /**
     * Get current security status
     */
    fun getSecurityStatus(): SecurityStatus = securityStatus
    
    /**
     * Get detected threats
     */
    fun getDetectedThreats(): List<SecurityThreat> = detectedThreats.toList()
    
    /**
     * Check if API access should be allowed
     */
    fun isApiAccessAllowed(): Boolean {
        return securityStatus == SecurityStatus.SECURE
    }
    
    /**
     * Reset security state (for testing)
     */
    fun resetSecurityState() {
        securityStatus = SecurityStatus.UNKNOWN
        lastSecurityCheck = 0L
        detectedThreats.clear()
    }
    
    private data class SecurityCheckResult(
        val passed: Boolean,
        val threat: SecurityThreat?
    )
    
}
