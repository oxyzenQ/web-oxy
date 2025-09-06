# Creativity Authored by oxyzenq 2025
# Ultra-aggressive optimization for Kconvert
# 🚀 Maximum Performance ProGuard Rules

# Optimization passes for maximum compression
-optimizationpasses 5
-allowaccessmodification
-repackageclasses 'o'
-overloadaggressively

# Remove all logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
    public static int wtf(...);
}

# Optimize Kotlin intrinsics
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
    static void checkNotNullParameter(java.lang.Object, java.lang.String);
    static void checkExpressionValueIsNotNull(java.lang.Object, java.lang.String);
    static void checkNotNullExpressionValue(java.lang.Object, java.lang.String);
    static void checkReturnedValueIsNotNull(java.lang.Object, java.lang.String);
    static void checkFieldIsNotNull(java.lang.Object, java.lang.String);
}

# Remove debug assertions
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(...);
    static void throwUninitializedPropertyAccessException(...);
}

# Currency converter specific optimizations
-keep class com.oxyzenq.currencyconverter.data.model.** { *; }
-keep class com.oxyzenq.kconvert.data.model.** { *; }

# Keep essential security classes but obfuscate internals
-keep class com.oxyzenq.currencyconverter.security.UltraSecureApiKeyManager {
    public *;
}
-keep class com.oxyzenq.currencyconverter.security.RASPSecurityManager {
    public *;
}

# Aggressively optimize Room database
-assumenosideeffects class androidx.room.RoomDatabase {
    public abstract void clearAllTables();
}

# Remove unused Compose debugging
-assumenosideeffects class androidx.compose.runtime.ComposerKt {
    boolean isTraceInProgress();
    void traceEventStart(int, int, int, java.lang.String);
    void traceEventEnd();
}

# Optimize Retrofit calls
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Remove BuildConfig debug fields
-assumenosideeffects class **.BuildConfig {
    public static boolean DEBUG;
    public static java.lang.String BUILD_TYPE;
}

# Optimize Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Remove unused resources and classes
-assumenosideeffects class java.lang.System {
    public static long currentTimeMillis();
    public static void gc();
}

# Optimize string operations
-assumenosideeffects class java.lang.String {
    public java.lang.String intern();
}

# Remove reflection usage where possible
-dontwarn java.lang.invoke.**
-dontwarn **$$serializer

# Ultra-aggressive class merging
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizations code/removal/advanced,code/allocation/variable

# Remove unused native methods (keep security ones)
-keepclasseswithmembernames class * {
    native <methods>;
}

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

# Complete silence mode
-verbose
-printmapping mapping.txt
-printseeds seeds.txt
-printusage usage.txt
