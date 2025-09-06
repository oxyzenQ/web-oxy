/*
 * Creativity Authored by oxyzenq 2025
 */

import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
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
    namespace = "com.oxyzenq.currencyconverter"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.oxyzenq.kconvert"
        minSdk = 26  // Android 8.0 (API level 26)
        targetSdk = 34
        versionCode = 1
        versionName = "AX-1"

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

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            
            // Performance monitoring flags
            buildConfigField("boolean", "ENABLE_PERFORMANCE_MONITORING", "true")
            buildConfigField("boolean", "ENABLE_ANR_DETECTION", "true")
        }
        
        // Ultra-optimized build variant for maximum performance
        create("ultraRelease") {
            initWith(getByName("release"))
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            
            // Aggressive optimization
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules-aggressive.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            
            // Ultra performance flags
            buildConfigField("boolean", "ENABLE_PERFORMANCE_MONITORING", "false")
            buildConfigField("boolean", "ENABLE_ANALYTICS", "false")
            buildConfigField("boolean", "ENABLE_CRASH_REPORTING", "false")
        }
        debug {
            isMinifyEnabled = false
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
    
    // Comprehensive lint configuration to suppress all warnings
    lint {
        abortOnError = false
        checkReleaseBuilds = false
        warningsAsErrors = false
        quiet = true
        disable += setOf(
            "MissingTranslation", 
            "ExtraTranslation",
            "UnusedResources",
            "IconMissingDensityFolder",
            "IconDensities",
            "IconLocation",
            "Deprecated",
            "ObsoleteLintCustomCheck",
            "GradleDependency",
            "NewerVersionAvailable",
            "AllowBackup",
            "GoogleAppIndexingWarning",
            "UnusedAttribute",
            "ContentDescription",
            "HardcodedText",
            "RtlHardcoded",
            "RtlCompat",
            "VectorDrawableCompat",
            "UnusedIds",
            "UselessParent",
            "InefficientWeight",
            "DisableBaselineAlignment",
            "Overdraw",
            "TooManyViews",
            "TooDeepLayout"
        )
    }
    
    // Custom APK naming will be handled by build tasks
    
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
            "-Xuse-ir",
            "-Xbackend-threads=8",
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-Xsuppress-version-warnings",
            "-Xno-param-assertions",
            "-Xno-call-assertions",
            "-Xno-receiver-assertions"
        )
        
        // Suppress all Kotlin compiler warnings
        allWarningsAsErrors = false
        suppressWarnings = true
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
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

// Custom build tasks for performance analysis
tasks.register("analyzeApkSize") {
    val buildDir = layout.buildDirectory
    doLast {
        val apkDir = buildDir.get().asFile.resolve("outputs/apk/ultraRelease/")
        if (apkDir.exists()) {
            apkDir.listFiles()?.forEach { apkFile ->
                if (apkFile.name.endsWith(".apk")) {
                    val apkSize = apkFile.length()
                    val sizeMB = apkSize / 1024.0 / 1024.0
                    println("📱 ${apkFile.name}: ${String.format("%.2f", sizeMB)} MB")
                    
                    // Alert if APK too large
                    if (apkSize > 15 * 1024 * 1024) { // 15MB limit
                        println("⚠️  APK size exceeded 15MB limit: ${apkFile.name}")
                    }
                }
            }
        } else {
            println("📍 APK directory not found: ${apkDir.absolutePath}")
        }
    }
}

// Performance benchmark task
tasks.register("benchmarkBuild") {
    dependsOn("assembleUltraRelease")
    val buildDir = layout.buildDirectory
    doLast {
        println("🚀 Build completed with performance profiling")
        println("📊 Performance metrics available for analysis")
        val profileDir = buildDir.get().asFile.resolve("reports/profile/")
        if (profileDir.exists()) {
            println("📍 Profile reports: ${profileDir.absolutePath}")
        }
    }
}

// Ultra-performance build task
tasks.register("buildUltraOptimized") {
    dependsOn("assembleUltraRelease")
    finalizedBy("analyzeApkSize")
    
    doLast {
        println("🔥 Ultra-optimized Kconvert APKs built successfully!")
        println("📍 Output: app/build/outputs/apk/ultraRelease/")
    }
}

dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material")
    
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // ViewModel & LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")
    
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // WorkManager for background tasks
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Dependency Injection
    implementation("com.google.dagger:hilt-android:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    ksp("com.google.dagger:hilt-compiler:2.48")
    
    
    // Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // Preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Charts for gauge
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("io.github.bytebeats:compose-charts:0.1.2")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
