/*
 * Creativity Authored by oxyzenq 2025
 */

// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Buildscript dependencies are now managed via plugins block

plugins {
    id("com.android.application") version "8.4.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    id("com.google.devtools.ksp") version "2.0.0-1.0.21" apply false
}

// Repositories are now managed in settings.gradle.kts
