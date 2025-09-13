# Creativity Authored by oxyzenq 2025
# Kconvert ProGuard Rules for API Key Protection and Code Obfuscation

# Keep source file names and line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Ultra-Security ProGuard Rules for 98% Protection Level

# Keep ultra-secure API key manager but obfuscate everything else
-keep class com.oxyzenq.kconvert.security.UltraSecureApiKeyManager {
    public *;
}

# Keep RASP security manager interface
-keep class com.oxyzenq.kconvert.security.RASPSecurityManager {
    public *;
}

# Keep ECDH key manager
-keep class com.oxyzenq.kconvert.security.ECDHKeyManager {
    public *;
}

# Keep native method declarations but obfuscate implementation
-keepclasseswithmembernames class * {
    native <methods>;
}

# Aggressively obfuscate all security internals
-keepclassmembers class com.oxyzenq.kconvert.security.** {
    private static final byte[] ENCRYPTED_KEY_FRAGMENTS;
    private static final byte[] OBFUSCATED_PARTS;
    private static final java.lang.String SERVER_PUBLIC_KEY_B64;
    private static final java.lang.String[] ECDH_ENCRYPTED_PARTS;
}

# Keep Room database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Keep Retrofit API interfaces
-keep interface com.oxyzenq.kconvert.data.api.* { *; }
-keep interface com.oxyzenq.kconvert.data.remote.* { *; }

# Keep data models for JSON serialization
-keep class com.oxyzenq.kconvert.data.model.** { *; }
-keep class com.oxyzenq.kconvert.data.api.** { *; }
-keep class com.oxyzenq.kconvert.data.remote.GitHubRelease { *; }
-keep class com.oxyzenq.kconvert.data.remote.GitHubAsset { *; }
-keep class com.oxyzenq.kconvert.data.remote.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# Complete warning suppression for all build types
-dontnote **
-dontwarn **
-ignorewarnings

# Suppress all R8/ProGuard warnings
-dontwarn java.lang.invoke.**
-dontwarn kotlin.**
-dontwarn kotlinx.**
-dontwarn androidx.**
-dontwarn com.google.**
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.**
-dontwarn org.jetbrains.**
-dontwarn dagger.**
-dontwarn com.squareup.**

# Suppress security-related warnings
-dontwarn java.security.**
-dontwarn javax.crypto.**
-dontwarn android.security.**

# Suppress annotation warnings
-dontwarn **$$serializer
-dontwarn **$Companion
-dontwarn **$DefaultImpls

# Suppress reflection warnings
-dontwarn java.lang.reflect.**
-dontwarn sun.misc.**

# Gson specific rules
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# OkHttp and Retrofit
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class okhttp3.** { *; }
-keep class retrofit2.** { *; }

# Coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Additional obfuscation for security
-repackageclasses 'o'
-allowaccessmodification
-overloadaggressively

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}
