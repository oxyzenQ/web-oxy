/*
 * Creativity Authored by oxyzenq 2025
 */

#include <jni.h>
#include <string>
#include <android/log.h>
#include <sys/ptrace.h>
#include <unistd.h>
#include <sys/stat.h>
#include <cstring>

#define LOG_TAG "NativeSecurity"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// XOR encrypted API key fragments stored in native code
static const unsigned char encrypted_fragments[][64] = {
    // Fragment 1 - XOR encrypted with device-specific key
    {0x1A, 0x2B, 0x3C, 0x4D, 0x5E, 0x6F, 0x70, 0x81, 0x92, 0xA3, 0xB4, 0xC5, 0xD6, 0xE7, 0xF8, 0x09,
     0x0A, 0x1B, 0x2C, 0x3D, 0x4E, 0x5F, 0x60, 0x71, 0x82, 0x93, 0xA4, 0xB5, 0xC6, 0xD7, 0xE8, 0xF9,
     0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
     0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00},
    
    // Fragment 2 - XOR encrypted with different pattern
    {0x2B, 0x3C, 0x4D, 0x5E, 0x6F, 0x70, 0x81, 0x92, 0xA3, 0xB4, 0xC5, 0xD6, 0xE7, 0xF8, 0x09, 0x0A,
     0x1B, 0x2C, 0x3D, 0x4E, 0x5F, 0x60, 0x71, 0x82, 0x93, 0xA4, 0xB5, 0xC6, 0xD7, 0xE8, 0xF9, 0x1A,
     0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
     0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00},
    
    // Fragment 3 - XOR encrypted with third pattern
    {0x3C, 0x4D, 0x5E, 0x6F, 0x70, 0x81, 0x92, 0xA3, 0xB4, 0xC5, 0xD6, 0xE7, 0xF8, 0x09, 0x0A, 0x1B,
     0x2C, 0x3D, 0x4E, 0x5F, 0x60, 0x71, 0x82, 0x93, 0xA4, 0xB5, 0xC6, 0xD7, 0xE8, 0xF9, 0x1A, 0x2B,
     0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
     0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}
};

// Anti-debugging check using ptrace
bool isDebuggerAttached() {
    if (ptrace(PTRACE_TRACEME, 0, 1, 0) == -1) {
        LOGE("Debugger detected via ptrace");
        return true;
    }
    ptrace(PTRACE_DETACH, 0, 1, 0);
    return false;
}

// Check for common debugging/analysis tools
bool checkForAnalysisTools() {
    const char* tools[] = {
        "/data/local/tmp/frida-server",
        "/data/local/tmp/gdbserver",
        "/data/local/tmp/gdb",
        "/system/bin/strace",
        "/system/xbin/strace",
        "/data/local/tmp/tcpdump"
    };
    
    for (int i = 0; i < sizeof(tools) / sizeof(tools[0]); i++) {
        struct stat st;
        if (stat(tools[i], &st) == 0) {
            LOGE("Analysis tool detected: %s", tools[i]);
            return true;
        }
    }
    return false;
}

// Generate XOR key from device ID
void generateXORKey(const char* deviceId, unsigned char* xorKey, int keyLength) {
    if (!deviceId || !xorKey || keyLength <= 0) return;
    
    int deviceIdLen = strlen(deviceId);
    if (deviceIdLen == 0) return;
    
    // Simple key derivation from device ID
    for (int i = 0; i < keyLength; i++) {
        xorKey[i] = (unsigned char)(deviceId[i % deviceIdLen] ^ (i + 0x5A));
    }
}

// Decrypt fragment using XOR
std::string decryptFragment(int fragmentIndex, const char* deviceId) {
    if (fragmentIndex < 0 || fragmentIndex >= 3 || !deviceId) {
        return "";
    }
    
    unsigned char xorKey[64];
    generateXORKey(deviceId, xorKey, 64);
    
    std::string result;
    const unsigned char* fragment = encrypted_fragments[fragmentIndex];
    
    for (int i = 0; i < 32; i++) { // Only decrypt first 32 bytes
        if (fragment[i] != 0) {
            unsigned char decrypted = fragment[i] ^ xorKey[i];
            if (decrypted >= 32 && decrypted <= 126) { // Printable ASCII
                result += (char)decrypted;
            }
        }
    }
    
    return result;
}

// JNI function to get native key fragment
extern "C" JNIEXPORT jstring JNICALL
Java_com_oxyzenq_currencyconverter_security_UltraSecureApiKeyManager_getNativeKeyFragment(
        JNIEnv *env, jobject thiz, jobject context) {
    
    // Anti-debug checks
    if (isDebuggerAttached()) {
        LOGE("Security: Debugger detected, returning empty key");
        return env->NewStringUTF("");
    }
    
    if (checkForAnalysisTools()) {
        LOGE("Security: Analysis tools detected, returning empty key");
        return env->NewStringUTF("");
    }
    
    // Get device ID from Android context (simplified - in real implementation, 
    // you'd call Java methods to get actual device ID)
    std::string deviceId = "native_device_id_placeholder";
    
    // Decrypt and assemble fragments
    std::string assembledKey;
    for (int i = 0; i < 3; i++) {
        std::string fragment = decryptFragment(i, deviceId.c_str());
        assembledKey += fragment;
    }
    
    LOGI("Native key fragment assembled, length: %zu", assembledKey.length());
    return env->NewStringUTF(assembledKey.c_str());
}

// JNI function for runtime security validation
extern "C" JNIEXPORT jboolean JNICALL
Java_com_oxyzenq_currencyconverter_security_UltraSecureApiKeyManager_validateRuntimeSecurity(
        JNIEnv *env, jobject thiz, jobject context) {
    
    // Perform comprehensive security checks
    bool secure = true;
    
    // Check for debugger
    if (isDebuggerAttached()) {
        LOGE("Security validation failed: Debugger detected");
        secure = false;
    }
    
    // Check for analysis tools
    if (checkForAnalysisTools()) {
        LOGE("Security validation failed: Analysis tools detected");
        secure = false;
    }
    
    // Additional native security checks can be added here
    // - Memory protection checks
    // - Code integrity verification
    // - Anti-hooking measures
    
    LOGI("Native security validation result: %s", secure ? "SECURE" : "COMPROMISED");
    return secure ? JNI_TRUE : JNI_FALSE;
}

// JNI function to perform code integrity check
extern "C" JNIEXPORT jboolean JNICALL
Java_com_oxyzenq_currencyconverter_security_RASPSecurityManager_performNativeIntegrityCheck(
        JNIEnv *env, jobject thiz) {
    
    // Simple integrity check - verify our encrypted fragments haven't been modified
    // In production, this would include more sophisticated checks
    
    bool integrityOk = true;
    
    // Check if encrypted fragments are intact (simple checksum)
    unsigned int checksum = 0;
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 32; j++) {
            checksum += encrypted_fragments[i][j];
        }
    }
    
    // Expected checksum (calculated during build)
    const unsigned int expectedChecksum = 0x12345678; // Replace with actual checksum
    
    if (checksum != expectedChecksum) {
        LOGE("Integrity check failed: Fragment checksum mismatch");
        integrityOk = false;
    }
    
    LOGI("Native integrity check result: %s", integrityOk ? "PASS" : "FAIL");
    return integrityOk ? JNI_TRUE : JNI_FALSE;
}
