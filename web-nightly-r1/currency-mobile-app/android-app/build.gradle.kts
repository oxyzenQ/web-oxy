/*
 * Creativity Authored by oxyzenq 2025
 */

// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Buildscript dependencies are now managed via plugins block

plugins {
    id("com.android.application") version "8.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
}

// Repositories are now managed in settings.gradle.kts
