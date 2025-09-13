#!/bin/bash

# Script to add copyright headers to all Kconvert source files
# Creativity Authored by oxyzenq 2025

echo "🔖 Adding copyright headers to all Kconvert source files..."

# Copyright header content
HEADER="/*
 * Creativity Authored by oxyzenq 2025
 */"

# Function to add header to a file if it doesn't already exist
add_header() {
    local file="$1"
    if ! grep -q "Creativity Authored by oxyzenq 2025" "$file"; then
        # Create temporary file with header + original content
        {
            echo "$HEADER"
            echo ""
            cat "$file"
        } > "$file.tmp"
        mv "$file.tmp" "$file"
        echo "✅ Added header to: $file"
    else
        echo "⏭️  Header already exists in: $file"
    fi
}

# Add headers to all Kotlin files
echo "📱 Processing Kotlin files..."
find app/src/main/java -name "*.kt" | while read -r file; do
    add_header "$file"
done

# Add headers to all Java files (if any)
echo "☕ Processing Java files..."
find app/src/main/java -name "*.java" | while read -r file; do
    add_header "$file"
done

# Add headers to C++ files
echo "⚡ Processing C++ files..."
find app/src/main/cpp -name "*.cpp" -o -name "*.h" | while read -r file; do
    add_header "$file"
done

# Add headers to build files
echo "🔧 Processing build configuration files..."
for file in "app/build.gradle.kts" "build.gradle.kts" "settings.gradle.kts"; do
    if [ -f "$file" ]; then
        add_header "$file"
    fi
done

# Add headers to ProGuard files
echo "🛡️  Processing ProGuard files..."
find app -name "proguard-*.pro" | while read -r file; do
    add_header "$file"
done

echo ""
echo "🎉 Copyright header addition complete!"
echo "📝 All Kconvert source files now include: 'Creativity Authored by oxyzenq 2025'"
