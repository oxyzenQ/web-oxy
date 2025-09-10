/*
 * Creativity Authored by oxyzenq 2025
 */

import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

// Dynamic keystore configuration for local dev and CI builds
val keystorePropertiesFile = rootProject.file("key.properties")
val keystoreProperties = Properties()

// Load keystore properties if file exists (local development)
val useKeystoreFile = keystorePropertiesFile.exists()
if (useKeystoreFile) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "com.oxyzenq.kconvert"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.oxyzenq.kconvert"
        minSdk = 26  // Android 8.0 (API level 26)
        targetSdk = 35
        versionCode = 2
        versionName = "2.dev-3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // NDK configuration for native security - will use splits instead
        
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
                arguments += "-DANDROID_STL=c++_shared"
            }
        }
    }
    
    // Dynamic signing configuration
    signingConfigs {
        create("release") {
            if (useKeystoreFile) {
                // Local development - use key.properties file
                val storeFilePath = keystoreProperties["KCONVERT_STORE_FILE"] as? String
                if (storeFilePath != null) {
                    storeFile = file(storeFilePath)
                    storePassword = keystoreProperties["storePassword"] as String
                    keyAlias = keystoreProperties["keyAlias"] as String
                    keyPassword = keystoreProperties["keyPassword"] as String
                    if (keystoreProperties.containsKey("storeType")) {
                        storeType = keystoreProperties["storeType"] as String
                    }
                } else {
                    // Fallback to debug keystore if main keystore not found
                    storeFile = file("debug.keystore")
                    storePassword = "android"
                    keyAlias = "androiddebugkey"
                    keyPassword = "android"
                    storeType = "JKS"
                }
            } else {
                // CI/CD build - use environment variables
                storeFile = System.getenv("KEYSTORE_FILE")?.let { file(it) }
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: System.getenv("STORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS") ?: System.getenv("EXAMPLE_ALIAS") ?: "oxyzenq"
                keyPassword = System.getenv("KEY_PASSWORD")
                storeType = System.getenv("STORE_TYPE") ?: "PKCS12"
            }
        }
    }
    
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    // ========================================
    // 🏗️ BUILD TYPES CONFIGURATION
    // Available: debug, ultraRelease
    // ========================================
    buildTypes {
        // 🐛 DEBUG BUILD TYPE
        // Command: ./gradlew assembleDebug
        // Purpose: Development and testing
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        
        // 🚀 ULTRA RELEASE BUILD TYPE
        // Command: ./gradlew assembleUltraRelease
        // Purpose: Production optimized builds
        create("ultraRelease") {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    // Split APKs by ABI for optimized builds
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a")
            isUniversalApk = false
        }
    }
    
    // Basic lint configuration - show warnings but don't fail build
    lint {
        abortOnError = false
        warningsAsErrors = false
        // Only disable truly unnecessary checks
        disable += setOf(
            "MissingTranslation" // Multi-language support not required yet
        )
    }
    
    // ========================================
    // 🏷️ APK RENAMING TASK
    // Command: ./gradlew renameApks
    // Purpose: Rename APKs with proper naming convention
    // ========================================
    tasks.register("renameApks") {
        dependsOn("assembleDebug", "assembleUltraRelease")
        
        val buildDir = layout.buildDirectory
        val packageName = "Kconvert"
        val version = "Kconvert.1.rc2"
        
        doLast {
            // Rename debug APKs
            val debugDir = buildDir.get().asFile.resolve("outputs/apk/debug")
            if (debugDir.exists()) {
                debugDir.listFiles()?.forEach { file ->
                    if (file.name.endsWith(".apk")) {
                        val newName = when {
                            file.name.contains("arm64-v8a") -> "$packageName-$version-debug-arm64-v8a.apk"
                            file.name.contains("armeabi-v7a") -> "$packageName-$version-debug-armeabi-v7a.apk"
                            else -> "$packageName-$version-debug.apk"
                        }
                        val newFile = File(debugDir, newName)
                        file.renameTo(newFile)
                        println("Renamed: ${file.name} -> ${newName}")
                    }
                }
            }
            
            // Rename ultraRelease APKs
            val ultraReleaseDir = buildDir.get().asFile.resolve("outputs/apk/ultraRelease")
            if (ultraReleaseDir.exists()) {
                ultraReleaseDir.listFiles()?.forEach { file ->
                    if (file.name.endsWith(".apk")) {
                        val newName = when {
                            file.name.contains("arm64-v8a") -> "$packageName-$version-ultraRelease-arm64-v8a.apk"
                            file.name.contains("armeabi-v7a") -> "$packageName-$version-ultraRelease-armeabi-v7a.apk"
                            else -> "$packageName-$version-ultraRelease.apk"
                        }
                        val newFile = File(ultraReleaseDir, newName)
                        file.renameTo(newFile)
                        println("Renamed: ${file.name} -> ${newName}")
                    }
                }
            }
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
        
        // Ultra-performance Kotlin compiler flags with warning suppression
        freeCompilerArgs += listOf(
            "-Xjsr305=strict",
            "-Xjvm-default=all",
            "-Xbackend-threads=8",
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-Xsuppress-version-warnings",
            "-Xno-param-assertions",
            "-Xno-call-assertions",
            "-Xno-receiver-assertions"
        )
    }

    buildTypes {
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    
    // Resource and APK size optimization
    packaging {
        resources {
            excludes += listOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/DEPENDENCIES",
                "/META-INF/LICENSE",
                "/META-INF/LICENSE.txt",
                "/META-INF/NOTICE",
                "/META-INF/NOTICE.txt",
                "DebugProbesKt.bin",
                "/META-INF/versions/**",
                "/META-INF/*.kotlin_module",
                "**/attach_hotspot_windows.dll",
                "META-INF/licenses/**",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1"
            )
        }
        
        jniLibs {
            useLegacyPackaging = false
        }
    }
    
    // Bundle optimization for Play Store
    bundle {
        language {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }
}

// ========================================
// 📊 APK SIZE ANALYSIS TASK
// Command: ./gradlew analyzeApkSize
// Purpose: Check APK sizes without building
// ========================================
tasks.register("analyzeApkSize") {
    val buildDir = layout.buildDirectory
    doLast {
        val ultraReleaseDir = buildDir.get().asFile.resolve("outputs/apk/ultraRelease/")
        val debugDir = buildDir.get().asFile.resolve("outputs/apk/debug/")
        var foundApks = false
        
        // Check ultraRelease APKs
        if (ultraReleaseDir.exists()) {
            ultraReleaseDir.listFiles()?.forEach { apkFile ->
                if (apkFile.name.endsWith(".apk")) {
                    foundApks = true
                    val apkSize = apkFile.length()
                    val sizeMB = apkSize / 1024.0 / 1024.0
                    println("📱 [UltraRelease] ${apkFile.name}: ${String.format("%.2f", sizeMB)} MB")
                    
                    if (apkSize > 15 * 1024 * 1024) {
                        println("⚠️  APK size exceeded 15MB limit: ${apkFile.name}")
                    }
                }
            }
        }
        
        // Check debug APKs
        if (debugDir.exists()) {
            debugDir.listFiles()?.forEach { apkFile ->
                if (apkFile.name.endsWith(".apk")) {
                    foundApks = true
                    val apkSize = apkFile.length()
                    val sizeMB = apkSize / 1024.0 / 1024.0
                    println("📱 [Debug] ${apkFile.name}: ${String.format("%.2f", sizeMB)} MB")
                }
            }
        }
        
        if (!foundApks) {
            println("⚠️  Sir, you don't have any APKs yet! Build them first with:")
            println("   ./gradlew assembleDebug (for debug APKs)")
            println("   ./gradlew assembleUltraRelease (for release APKs)")
        }
    }
}

// ========================================
// 🚀 BENCHMARK BUILD & CHECK TASK
// Command: ./gradlew benchmark-build-check
// Purpose: Build ultraRelease + performance analysis
// ========================================
tasks.register("benchmark-build-check") {
    dependsOn("assembleUltraRelease")
    dependsOn("renameApks")
    val buildDir = layout.buildDirectory
    doLast {
        println("🚀 UltraRelease build completed with performance profiling")
        println("📊 Performance metrics available for analysis")
        val profileDir = buildDir.get().asFile.resolve("reports/profile/")
        if (profileDir.exists()) {
            println("📍 Profile reports: ${profileDir.absolutePath}")
        }
        
        // Show APK info
        val apkDir = buildDir.get().asFile.resolve("outputs/apk/ultraRelease/")
        if (apkDir.exists()) {
            apkDir.listFiles()?.forEach { apkFile ->
                if (apkFile.name.endsWith(".apk")) {
                    val sizeMB = apkFile.length() / 1024.0 / 1024.0
                    println("📱 Generated: ${apkFile.name} (${String.format("%.2f", sizeMB)} MB)")
                }
            }
        }
    }
}

// ========================================
// 🔍 BUILD CHECK TASK (NO BUILDING)
// Command: ./gradlew build-check
// Purpose: Only check if APKs exist, no building
// ========================================
tasks.register("build-check") {
    val buildDir = layout.buildDirectory
    doLast {
        val ultraReleaseDir = buildDir.get().asFile.resolve("outputs/apk/ultraRelease/")
        val debugDir = buildDir.get().asFile.resolve("outputs/apk/debug/")
        var foundApks = false
        
        println("🔍 Checking for existing APKs...")
        
        // Check ultraRelease APKs
        if (ultraReleaseDir.exists()) {
            ultraReleaseDir.listFiles()?.forEach { apkFile ->
                if (apkFile.name.endsWith(".apk")) {
                    foundApks = true
                    val sizeMB = apkFile.length() / 1024.0 / 1024.0
                    println("✅ [UltraRelease] ${apkFile.name} (${String.format("%.2f", sizeMB)} MB)")
                }
            }
        }
        
        // Check debug APKs
        if (debugDir.exists()) {
            debugDir.listFiles()?.forEach { apkFile ->
                if (apkFile.name.endsWith(".apk")) {
                    foundApks = true
                    val sizeMB = apkFile.length() / 1024.0 / 1024.0
                    println("✅ [Debug] ${apkFile.name} (${String.format("%.2f", sizeMB)} MB)")
                }
            }
        }
        
        if (!foundApks) {
            println("⚠️  Sir, you don't have any APKs yet!")
            println("   Use: ./gradlew assembleDebug (for debug)")
            println("   Use: ./gradlew assembleUltraRelease (for release)")
            println("   Use: ./gradlew benchmark-build-check (build + analyze)")
        } else {
            println("🎉 APK check completed successfully!")
        }
        println("📍 Output: app/build/outputs/apk/ultraRelease/")
    }
}

// ========================================
// 🔬 FULL ANALYZE TASK
// Command: ./gradlew full-analyze
// Purpose: Complete analysis of existing APKs
// ========================================
tasks.register("full-analyze") {
    dependsOn("analyzeApkSize")
    val buildDir = layout.buildDirectory
    doLast {
        println("🔬 Starting full APK analysis...")
        
        val ultraReleaseDir = buildDir.get().asFile.resolve("outputs/apk/ultraRelease/")
        val debugDir = buildDir.get().asFile.resolve("outputs/apk/debug/")
        var totalApks = 0
        var totalSize = 0L
        
        // Analyze ultraRelease APKs
        if (ultraReleaseDir.exists()) {
            ultraReleaseDir.listFiles()?.forEach { apkFile ->
                if (apkFile.name.endsWith(".apk")) {
                    totalApks++
                    totalSize += apkFile.length()
                    val sizeMB = apkFile.length() / 1024.0 / 1024.0
                    println("📊 [UltraRelease] ${apkFile.name}")
                    println("   Size: ${String.format("%.2f", sizeMB)} MB")
                    println("   Path: ${apkFile.absolutePath}")
                    println("   Modified: ${apkFile.lastModified()}")
                }
            }
        }
        
        // Analyze debug APKs
        if (debugDir.exists()) {
            debugDir.listFiles()?.forEach { apkFile ->
                if (apkFile.name.endsWith(".apk")) {
                    totalApks++
                    totalSize += apkFile.length()
                    val sizeMB = apkFile.length() / 1024.0 / 1024.0
                    println("📊 [Debug] ${apkFile.name}")
                    println("   Size: ${String.format("%.2f", sizeMB)} MB")
                    println("   Path: ${apkFile.absolutePath}")
                    println("   Modified: ${apkFile.lastModified()}")
                }
            }
        }
        
        if (totalApks > 0) {
            val totalSizeMB = totalSize / 1024.0 / 1024.0
            println("\n📈 ANALYSIS SUMMARY:")
            println("   Total APKs: $totalApks")
            println("   Total Size: ${String.format("%.2f", totalSizeMB)} MB")
            println("   Average Size: ${String.format("%.2f", totalSizeMB / totalApks)} MB")
        } else {
            println("⚠️  Sir, you don't have any APKs to analyze!")
            println("   Build some APKs first with:")
            println("   ./gradlew assembleDebug")
            println("   ./gradlew assembleUltraRelease")
        }
    }
}

dependencies {
    // Compose BOM - Updated to latest
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3")
    
    // Core Android - Updated versions
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    
    // ViewModel & LiveData - Updated
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.4")
    
    // Navigation - Updated
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // Networking - Updated to latest stable versions
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Room Database - Updated
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // WorkManager - Updated
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    
    // Dependency Injection - Updated to match plugin version
    implementation("com.google.dagger:hilt-android:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    ksp("com.google.dagger:hilt-compiler:2.50")
    
    // Image Loading - Updated
    implementation("io.coil-kt:coil-compose:2.7.0")
    
    // Preferences - Updated
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Jetpack Graphics (lightweight rendering utilities)
    implementation("androidx.graphics:graphics-core:1.0.1")
    implementation("androidx.graphics:graphics-path:1.0.1")
    implementation("androidx.graphics:graphics-shapes:1.0.1")

    // Profile Installer (precompiled runtime profiles for faster startup & smoothness)
    implementation("androidx.profileinstaller:profileinstaller:1.3.1")
    
    // Charts for gauge
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("io.github.bytebeats:compose-charts:0.1.2")
    
    // Coroutines - Updated
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    
    // Testing - Updated
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
