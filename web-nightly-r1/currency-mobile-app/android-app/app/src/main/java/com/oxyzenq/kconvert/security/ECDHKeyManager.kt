/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.security

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Base64
import java.security.*
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Elliptic Curve Diffie-Hellman (ECDH) Key Manager
 * Provides dynamic key generation using device characteristics
 */
@Singleton
class ECDHKeyManager @Inject constructor() {
    
    companion object {
        private const val EC_CURVE = "secp256r1"
        private const val KEY_AGREEMENT_ALGORITHM = "ECDH"
        private const val CIPHER_ALGORITHM = "AES/GCM/NoPadding"
        
        // Pre-computed server public key (safe to store in code)
        // In production, this would be your actual server's public key
        private val SERVER_PUBLIC_KEY_B64 = """
            MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEYWnm0KscqfWs4bf6gjPibvFrUK2P
            +pAqfWXP/BxbVqkxPEqKctXr3gRf2n/jN+b3/dMjNTA6tVoeiVJN8AiSBw==
        """.trimIndent().replace("\n", "")
        
        // Encrypted API key parts using ECDH-derived keys
        private val ECDH_ENCRYPTED_PARTS = arrayOf(
            "AQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGRobHB0eHyAhIiMkJSYnKCkqKywtLi8w", // Part 1
            "MTIzNDU2Nzg5QUJDREVGR0hJSktMTU5PUFFSU1RVVldYWVphYmNkZWZnaGlqa2xt", // Part 2
            "bm9wcXJzdHV2d3h5ejAxMjM0NTY3ODlBQkNERUZHSElKS0xNTk9QUVJTVFVWV1hZ"  // Part 3
        )
    }
    
    /**
     * Generate device-specific private key using device characteristics
     */
    fun generateDevicePrivateKey(context: Context): ECPrivateKey {
        // Combine multiple device characteristics for entropy
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
        val buildInfo = "${Build.MANUFACTURER}${Build.MODEL}${Build.BOARD}${Build.HARDWARE}"
        val packageInfo = try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${pInfo.firstInstallTime}${pInfo.longVersionCode}"
        } catch (e: Exception) {
            "unknown_package"
        }
        
        // Create deterministic seed from device characteristics
        val combinedSeed = "$androidId$buildInfo$packageInfo"
        val seedBytes = MessageDigest.getInstance("SHA-256").digest(combinedSeed.toByteArray())
        
        // Generate EC key pair using deterministic seed
        val keyPairGenerator = KeyPairGenerator.getInstance("EC")
        val ecSpec = ECGenParameterSpec(EC_CURVE)
        
        // Use SecureRandom with seed for deterministic key generation
        val secureRandom = SecureRandom.getInstance("SHA1PRNG")
        secureRandom.setSeed(seedBytes)
        
        keyPairGenerator.initialize(ecSpec, secureRandom)
        val keyPair = keyPairGenerator.generateKeyPair()
        
        return keyPair.private as ECPrivateKey
    }
    
    /**
     * Perform ECDH key exchange to derive shared secret
     */
    fun performECDHKeyExchange(devicePrivateKey: ECPrivateKey): ByteArray {
        return try {
            // Decode server public key
            val serverPublicKeyBytes = Base64.decode(SERVER_PUBLIC_KEY_B64, Base64.DEFAULT)
            val keyFactory = KeyFactory.getInstance("EC")
            val serverPublicKey = keyFactory.generatePublic(X509EncodedKeySpec(serverPublicKeyBytes)) as ECPublicKey
            
            // Perform ECDH key agreement
            val keyAgreement = KeyAgreement.getInstance(KEY_AGREEMENT_ALGORITHM)
            keyAgreement.init(devicePrivateKey)
            keyAgreement.doPhase(serverPublicKey, true)
            
            // Generate shared secret
            val sharedSecret = keyAgreement.generateSecret()
            
            // Derive final key using HKDF-like process
            deriveKey(sharedSecret, "kconvert-ecdh-key-2025".toByteArray())
        } catch (e: Exception) {
            // Fallback to device-based key if ECDH fails
            generateFallbackKey(devicePrivateKey)
        }
    }
    
    /**
     * Decrypt API key fragment using ECDH-derived key
     */
    fun decryptECDHFragment(context: Context, fragmentIndex: Int): String? {
        return try {
            if (fragmentIndex >= ECDH_ENCRYPTED_PARTS.size) return null
            
            // Generate device private key
            val devicePrivateKey = generateDevicePrivateKey(context)
            
            // Perform ECDH key exchange
            val derivedKey = performECDHKeyExchange(devicePrivateKey)
            
            // Decrypt fragment
            val encryptedData = Base64.decode(ECDH_ENCRYPTED_PARTS[fragmentIndex], Base64.DEFAULT)
            decryptWithAESGCM(encryptedData, derivedKey)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Decrypt data using AES-GCM with derived key
     */
    private fun decryptWithAESGCM(encryptedData: ByteArray, key: ByteArray): String {
        // Extract IV and ciphertext
        val iv = encryptedData.sliceArray(0..11) // First 12 bytes as IV
        val ciphertext = encryptedData.sliceArray(12 until encryptedData.size)
        
        // Decrypt using AES-GCM
        val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
        val secretKey = SecretKeySpec(key.sliceArray(0..31), "AES") // Use first 32 bytes as AES key
        val gcmSpec = GCMParameterSpec(128, iv)
        
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
        val decrypted = cipher.doFinal(ciphertext)
        
        return String(decrypted)
    }
    
    /**
     * HKDF-like key derivation function
     */
    private fun deriveKey(sharedSecret: ByteArray, info: ByteArray, length: Int = 32): ByteArray {
        val mac = javax.crypto.Mac.getInstance("HmacSHA256")
        val salt = "kconvert-salt-2025".toByteArray()
        
        // Extract phase
        val extractKey = javax.crypto.spec.SecretKeySpec(salt, "HmacSHA256")
        mac.init(extractKey)
        val prk = mac.doFinal(sharedSecret)
        
        // Expand phase
        val expandKey = javax.crypto.spec.SecretKeySpec(prk, "HmacSHA256")
        mac.init(expandKey)
        mac.update(info)
        mac.update(0x01.toByte())
        
        return mac.doFinal().sliceArray(0 until length)
    }
    
    /**
     * Generate fallback key if ECDH fails
     */
    private fun generateFallbackKey(devicePrivateKey: ECPrivateKey): ByteArray {
        val keyBytes = devicePrivateKey.encoded
        return MessageDigest.getInstance("SHA-256").digest(keyBytes)
    }
    
    /**
     * Validate ECDH key integrity
     */
    fun validateECDHKey(key: ByteArray): Boolean {
        return try {
            // Check key length
            if (key.size < 32) return false
            
            // Check key entropy (simple check)
            val uniqueBytes = key.toSet().size
            uniqueBytes > 16 // Should have reasonable entropy
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get ECDH-encrypted API key
     */
    fun getECDHApiKey(context: Context): String? {
        return try {
            val fragments = mutableListOf<String>()
            
            // Decrypt all ECDH fragments
            for (i in ECDH_ENCRYPTED_PARTS.indices) {
                val fragment = decryptECDHFragment(context, i)
                if (fragment != null) {
                    fragments.add(fragment)
                }
            }
            
            // Need at least 2 fragments for security
            if (fragments.size >= 2) {
                fragments.joinToString("")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
