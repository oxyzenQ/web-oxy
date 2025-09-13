# 🔐 CI/CD Signing Configuration for Kconvert

## Environment Variables for CI Builds

The Kconvert currency converter app supports dynamic keystore configuration for both local development and CI/CD pipelines. This guide covers signing setup for the `ultraRelease` build variant.

### Required Environment Variables

```bash
# Keystore file path (absolute path or relative to project root)
export KEYSTORE_FILE="/path/to/your/keystore.p12"

# Keystore password
export KEYSTORE_PASSWORD="your_keystore_password"

# Key password 
export KEY_PASSWORD="your_key_password"

# Key alias
export KEY_ALIAS="your_key_alias"

# Store type (optional, defaults to PKCS12)
export STORE_TYPE="PKCS12"
```

### Alternative Environment Variable Names

The build script supports multiple naming conventions:

- `KEYSTORE_PASSWORD` or `STORE_PASSWORD`
- `KEY_ALIAS` or `EXAMPLE_ALIAS`
- Falls back to default alias if not specified

### Local Development vs CI

**Local Development:**
- Uses `key.properties` file (git-ignored for security)
- Automatically detects keystore files in standard locations
- Uses local environment variables from shell configuration

**CI/CD Pipeline:**
- Uses environment variables when `key.properties` doesn't exist
- Supports GitHub Actions, GitLab CI, Jenkins, etc.
- Secure environment variable injection through CI secrets

### GitHub Actions Example

```yaml
name: Build Release APK
on:
  push:
    tags: ['v*']

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Setup JDK
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        
    - name: Build Release APK
      env:
        KEYSTORE_FILE: ${{ secrets.KEYSTORE_FILE }}
        KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
        KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
      run: ./gradlew assembleUltraRelease
```

### GitLab CI Example

```yaml
build_ultrarelease:
  stage: build
  script:
    - ./gradlew assembleUltraRelease
  variables:
    KEYSTORE_FILE: "$CI_PROJECT_DIR/keystore.p12"
    KEYSTORE_PASSWORD: "$KEYSTORE_PASSWORD"
    KEY_PASSWORD: "$KEY_PASSWORD"
    KEY_ALIAS: "$KEY_ALIAS"
  artifacts:
    paths:
      - app/build/outputs/apk/ultraRelease/
```

## Security Notes

- `key.properties` is added to `.gitignore` for security
- Never commit keystore files or passwords to version control
- Use secure environment variable injection in CI/CD
- Keystore file should be uploaded as CI secret/artifact

## Build Commands

### Local Development
```bash
# Debug build (unsigned)
./gradlew assembleDebug

# Ultra-optimized signed build
./gradlew assembleUltraRelease

# Complete build with APK renaming
./gradlew renameApks
```

### CI/CD Build
```bash
# Set environment variables and build
KEYSTORE_FILE=/path/to/keystore.p12 \
KEYSTORE_PASSWORD=your_password \
KEY_PASSWORD=your_key_password \
KEY_ALIAS=your_alias \
./gradlew assembleUltraRelease
```

### Available Build Tasks
- `assembleDebug` - Debug build (unsigned)
- `assembleUltraRelease` - Production build (signed)
- `renameApks` - Rename APKs with professional naming
- `build-check` - Check if APKs exist without building
- `analyzeApkSize` - Analyze APK sizes
- `benchmark-build-check` - Build and performance analysis
- `full-analyze` - Complete APK analysis with detailed info
